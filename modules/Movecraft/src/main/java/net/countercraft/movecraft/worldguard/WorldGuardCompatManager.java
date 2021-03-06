package net.countercraft.movecraft.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.events.CraftRotateEvent;
import net.countercraft.movecraft.events.CraftSinkEvent;
import net.countercraft.movecraft.events.CraftTranslateEvent;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.utils.LegacyUtils;
import net.countercraft.movecraft.utils.WorldguardUtils;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WorldGuardCompatManager implements Listener {
    @EventHandler
    public void onCraftTranslateEvent(CraftTranslateEvent event){
        if(!Settings.WorldGuardBlockMoveOnBuildPerm)
            return;
        if(event.getCraft().getNotificationPlayer() == null)
            return;
        for(MovecraftLocation location : event.getNewHitBox()){
            if(!pilotHasAccessToRegion(event.getCraft().getNotificationPlayer(), location, event.getCraft().getW())){
                event.setCancelled(true);
                event.setFailMessage(String.format( I18nSupport.getInternationalisedString( "Translation - WorldGuard - Not Permitted To Build" )+" @ %d,%d,%d", location.getX(), location.getY(), location.getZ() ) );
                return;
            }

        }
    }

    @EventHandler
    public void onCraftRotateEvent(CraftRotateEvent event){
        if(!Settings.WorldGuardBlockMoveOnBuildPerm)
            return;
        if(event.getCraft().getNotificationPlayer() == null)
            return;
        for(MovecraftLocation location : event.getNewHitBox()){
            if(!pilotHasAccessToRegion(event.getCraft().getNotificationPlayer(), location, event.getCraft().getW())){
                event.setCancelled(true);
                event.setFailMessage(String.format( I18nSupport.getInternationalisedString("Rotation - WorldGuard - Not Permitted To Build" )+" @ %d,%d,%d", location.getX(), location.getY(), location.getZ()));
                return;
            }
        }
    }
    private boolean pilotHasAccessToRegion(Player player, MovecraftLocation location, World world){
        if (Settings.IsLegacy){
            return LegacyUtils.canBuild(Movecraft.getInstance().getWorldGuardPlugin(), world, location, player);
        } else {
            RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
            LocalPlayer lPlayer = Movecraft.getInstance().getWorldGuardPlugin().wrapPlayer(player);
            com.sk89q.worldedit.world.World weWorld = new BukkitWorld(world);
            Location wgLoc = new Location(weWorld, location.getX(), location.getY(), location.getZ());
            return query.getApplicableRegions(wgLoc).isOwnerOfAll(lPlayer) || query.getApplicableRegions(wgLoc).isMemberOfAll(lPlayer) ||
                    player.hasPermission("worldguard.build.*");
        }
    }

    public void onCraftSink(CraftSinkEvent event){
        Craft pcraft = event.getCraft();
        Player notifyP = event.getCraft().getNotificationPlayer();
        if (Movecraft.getInstance().getWorldGuardPlugin() != null){
            WorldGuardPlugin wgPlugin = Movecraft.getInstance().getWorldGuardPlugin();
            ProtectedRegion region = null;
            RegionManager regionManager;
            if (Settings.IsLegacy){
                regionManager = LegacyUtils.getRegionManager(wgPlugin, pcraft.getW());
            } else {
                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                regionManager = container.get(BukkitAdapter.adapt(pcraft.getW()));
            }
            for (MovecraftLocation location : pcraft.getHitBox()){
                ApplicableRegionSet regions;
                if (Settings.IsLegacy)
                    regions = LegacyUtils.getApplicableRegions(regionManager, location.toBukkit(pcraft.getW()));
                else {
                    regions = regionManager.getApplicableRegions(BlockVector3.at(location.getX(), location.getY(), location.getZ()));
                }
                for (ProtectedRegion pr : regions.getRegions()){
                    if (WorldguardUtils.pvpAllowed(pr)){
                        region = pr;
                        break;
                    }
                }
                if (region != null){
                    break;
                }
            }
            if (region != null && Settings.WorldGuardBlockSinkOnPVPPerm && WorldguardUtils.pvpAllowed(region) && notifyP != null){
                notifyP.sendMessage(I18nSupport.getInternationalisedString("Player- Craft should sink but PVP is not allowed in this WorldGuard region"));
                event.setCancelled(true);
            }
        }
    }

}
