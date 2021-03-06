package net.countercraft.movecraft.mapUpdater.update;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.Rotation;
import net.countercraft.movecraft.WorldHandler;
import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.events.SignTranslateEvent;
import net.countercraft.movecraft.mapUpdater.MapUpdateManager;
import net.countercraft.movecraft.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Logger;


public class CraftRotateCommand extends UpdateCommand {
    @NotNull
    private final Craft craft;
    @NotNull
    private final Rotation rotation;
    @NotNull
    private final MovecraftLocation originLocation;


    public CraftRotateCommand(@NotNull final Craft craft, @NotNull final MovecraftLocation originLocation, @NotNull final Rotation rotation) {
        this.craft = craft;
        this.rotation = rotation;
        this.originLocation = originLocation;
    }

    @Override
    public void doUpdate() {
        final Logger logger = Movecraft.getInstance().getLogger();
        if (craft.getHitBox().isEmpty()) {
            logger.warning("Attempted to move craft with empty HashHitBox!");
            CraftManager.getInstance().removeCraft(craft);
            return;
        }
        long time = System.nanoTime();
        final Set<Material> passthroughBlocks = new HashSet<>(craft.getType().getPassthroughBlocks());
        final Set<UpdateCommand> toRotate = new HashSet<>();
        MutableHitBox originalLocations = new HashHitBox();
        final Rotation counterRotation = rotation == Rotation.CLOCKWISE ? Rotation.ANTICLOCKWISE : Rotation.CLOCKWISE;
        for (MovecraftLocation movecraftLocation : craft.getHitBox()) {
            originalLocations.add(MathUtils.rotateVec(counterRotation, movecraftLocation.subtract(originLocation)).add(originLocation));
        }
        if(craft.getSinking()){

            passthroughBlocks.add(Material.WATER);
            if (Settings.IsLegacy){ //use pre-1.13 values if running on 1.12.2 or lower
                passthroughBlocks.add(LegacyUtils.STATIONARY_WATER);
                passthroughBlocks.add(LegacyUtils.LEAVES);
                passthroughBlocks.add(LegacyUtils.LEAVES_2);
                passthroughBlocks.add(LegacyUtils.LONG_GRASS);
                passthroughBlocks.add(LegacyUtils.DOUBLE_PLANT);
            } else {//otherwise, use 1.13+ types
                //Leaves
                passthroughBlocks.add(Material.ACACIA_LEAVES);
                passthroughBlocks.add(Material.BIRCH_LEAVES);
                passthroughBlocks.add(Material.DARK_OAK_LEAVES);
                passthroughBlocks.add(Material.JUNGLE_LEAVES);
                passthroughBlocks.add(Material.OAK_LEAVES);
                passthroughBlocks.add(Material.SPRUCE_LEAVES);
                //Grass
                passthroughBlocks.add(Material.GRASS);
                //Double plants
                passthroughBlocks.add(Material.ROSE_BUSH);
                passthroughBlocks.add(Material.SUNFLOWER);
                passthroughBlocks.add(Material.LILAC);
                passthroughBlocks.add(Material.PEONY);

            }
        } else if (craft.getType().getMoveEntities()) {
            Location tOP = originLocation.toBukkit(craft.getW());
            tOP.setX(tOP.getBlockX() + 0.5);
            tOP.setZ(tOP.getBlockZ() + 0.5);
            Location midpoint = originalLocations.getMidPoint().toBukkit(craft.getW());
            for(Entity entity : craft.getW().getNearbyEntities(midpoint, originalLocations.getXLength()/2.0 + 1, originalLocations.getYLength()/2.0 + 2, originalLocations.getZLength()/2.0 + 1)){

                // Player is onboard this craft

                Location adjustedPLoc = entity.getLocation().subtract(tOP);

                double[] rotatedCoords = MathUtils.rotateVecNoRound(rotation, adjustedPLoc.getX(), adjustedPLoc.getZ());
                float newYaw = rotation == Rotation.CLOCKWISE ? 90F : -90F;
                if ((entity.getType() != EntityType.PLAYER && craft.getType().getOnlyMovePlayers()) || craft.getSinking()) {
                    continue;
                }
                EntityUpdateCommand eUp = new EntityUpdateCommand(entity, rotatedCoords[0] + tOP.getX() - entity.getLocation().getX(), 0, rotatedCoords[1] + tOP.getZ() - entity.getLocation().getZ(), newYaw, 0);
                toRotate.add(eUp);
            }
        }
        if (!passthroughBlocks.isEmpty()) {


            final HitBox to = CollectionUtils.filter(craft.getHitBox(), originalLocations);

            for (MovecraftLocation location : to) {
                Material material = location.toBukkit(craft.getW()).getBlock().getType();
                if (passthroughBlocks.contains(material)) {
                    craft.getPhaseBlocks().put(location, material);
                }
            }
            //The subtraction of the set of coordinates in the HitBox cube and the HitBox itself
            final HitBox invertedHitBox = CollectionUtils.filter(craft.getHitBox().boundingHitBox(), craft.getHitBox());
            //A set of locations that are confirmed to be "exterior" locations
            final MutableHitBox exterior = new HashHitBox();
            final MutableHitBox interior = new HashHitBox();

            //place phased blocks
            final Set<MovecraftLocation> overlap = new HashSet<>(craft.getPhaseBlocks().keySet());
            overlap.retainAll(craft.getHitBox().asSet());
            final int minX = craft.getHitBox().getMinX();
            final int maxX = craft.getHitBox().getMaxX();
            final int minY = craft.getHitBox().getMinY();
            final int maxY = overlap.isEmpty() ? craft.getHitBox().getMaxY() : Collections.max(overlap, Comparator.comparingInt(MovecraftLocation::getY)).getY();
            final int minZ = craft.getHitBox().getMinZ();
            final int maxZ = craft.getHitBox().getMaxZ();
            final HitBox[] surfaces = {
                    new SolidHitBox(new MovecraftLocation(minX, minY, minZ), new MovecraftLocation(minX, maxY, maxZ)),
                    new SolidHitBox(new MovecraftLocation(minX, minY, minZ), new MovecraftLocation(maxX, maxY, minZ)),
                    new SolidHitBox(new MovecraftLocation(maxX, minY, maxZ), new MovecraftLocation(minX, maxY, maxZ)),
                    new SolidHitBox(new MovecraftLocation(maxX, minY, maxZ), new MovecraftLocation(maxX, maxY, minZ)),
                    new SolidHitBox(new MovecraftLocation(minX, minY, minZ), new MovecraftLocation(maxX, minY, maxZ))};
            //Valid exterior starts as the 6 surface planes of the HitBox with the locations that lie in the HitBox removed
            final Set<MovecraftLocation> validExterior = new HashSet<>();
            for (HitBox hitBox : surfaces) {
                validExterior.addAll(CollectionUtils.filter(hitBox, craft.getHitBox()).asSet());
            }
            //Check to see which locations in the from set are actually outside of the craft
            for (MovecraftLocation location :validExterior ) {
                if (craft.getHitBox().contains(location) || exterior.contains(location)) {
                    continue;
                }
                //use a modified BFS for multiple origin elements
                Set<MovecraftLocation> visited = new HashSet<>();
                Queue<MovecraftLocation> queue = new LinkedList<>();
                queue.add(location);
                while (!queue.isEmpty()) {
                    MovecraftLocation node = queue.poll();
                    //If the node is already a valid member of the exterior of the HitBox, continued search is unitary.
                    for (MovecraftLocation neighbor : CollectionUtils.neighbors(invertedHitBox, node)) {
                        if (visited.contains(neighbor)) {
                            continue;
                        }
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
                exterior.addAll(visited);
            }
            interior.addAll(CollectionUtils.filter(invertedHitBox, exterior));

            final WorldHandler handler = Movecraft.getInstance().getWorldHandler();
            for (MovecraftLocation location : CollectionUtils.filter(invertedHitBox, exterior)) {
                Material material = location.toBukkit(craft.getW()).getBlock().getType();
                if (!passthroughBlocks.contains(material)) {
                    continue;
                }
                craft.getPhaseBlocks().put(location, material);
            }

            //translate the craft

            handler.rotateCraft(craft, originLocation, rotation);
            //trigger sign events
            sendSignEvents();

            //place confirmed blocks if they have been un-phased
            for (MovecraftLocation location : exterior) {
                if (!craft.getPhaseBlocks().containsKey(location)) {
                    continue;
                }
                handler.setBlockFast(location.toBukkit(craft.getW()), craft.getPhaseBlocks().get(location), (byte) 0);
                craft.getPhaseBlocks().remove(location);
            }

            for(MovecraftLocation location : originalLocations.boundingHitBox()){
                if(!craft.getHitBox().inBounds(location) && craft.getPhaseBlocks().containsKey(location)){
                    handler.setBlockFast(location.toBukkit(craft.getW()), craft.getPhaseBlocks().remove(location), (byte) 0);
                }
            }

            for (MovecraftLocation location : interior) {
                final Material material = location.toBukkit(craft.getW()).getBlock().getType();
                if (passthroughBlocks.contains(material)) {
                    craft.getPhaseBlocks().put(location, material);
                    handler.setBlockFast(location.toBukkit(craft.getW()), Material.AIR, (byte) 0);

                }
            }
        }else{
            //translate the craft

            Movecraft.getInstance().getWorldHandler().rotateCraft(craft, originLocation, rotation);
            //trigger sign events
            sendSignEvents();
        }
        MapUpdateManager.getInstance().scheduleUpdates(toRotate);
        if (!craft.isNotProcessing())
            craft.setProcessing(false);
        time = System.nanoTime() - time;
        if (Settings.Debug)
            logger.info("Total time: " + (time / 1e9) + " seconds. Moving with cooldown of " + craft.getTickCooldown() + ". Speed of: " + String.format("%.2f", craft.getSpeed()));
        craft.addMoveTime(time / 1e9f);
    }

    private void sendSignEvents(){
        Map<String[], List<MovecraftLocation>> signs = new HashMap<>();
        for (MovecraftLocation location : craft.getHitBox()) {
            Block block = location.toBukkit(craft.getW()).getBlock();
            if (block.getState() instanceof Sign) {
                Sign sign = (Sign) block.getState();
                if(!signs.containsKey(sign.getLines()))
                    signs.put(sign.getLines(), new ArrayList<>());
                signs.get(sign.getLines()).add(location);
            }
        }
        for(Map.Entry<String[], List<MovecraftLocation>> entry : signs.entrySet()){
            Bukkit.getServer().getPluginManager().callEvent(new SignTranslateEvent(craft, entry.getKey(), entry.getValue()));
            for(MovecraftLocation loc : entry.getValue()){
                Block block = loc.toBukkit(craft.getW()).getBlock();
                if (!(block.getState() instanceof Sign)) {
                    continue;
                }
                Sign sign = (Sign) block.getState();
                for(int i = 0; i<4; i++){
                    sign.setLine(i, entry.getKey()[i]);
                }
                sign.update();
            }
        }
    }

    @NotNull
    public Craft getCraft() {
        return craft;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CraftRotateCommand)) {
            return false;
        }
        CraftRotateCommand other = (CraftRotateCommand) obj;
        return other.craft.equals(this.craft) &&
                other.rotation == this.rotation &&
                other.originLocation.equals(this.originLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(craft, rotation, originLocation);
    }
}
