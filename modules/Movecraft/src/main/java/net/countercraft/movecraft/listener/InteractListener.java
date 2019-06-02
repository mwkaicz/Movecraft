/*
 * This file is part of Movecraft.
 *
 *     Movecraft is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Movecraft is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Movecraft.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.countercraft.movecraft.listener;

import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.utils.LegacyUtils;
import net.countercraft.movecraft.utils.MathUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Button;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class InteractListener implements Listener {
    private static final Map<Player, Long> timeMap = new HashMap<>();
    final Material[] buttons = !Settings.IsLegacy ? new Material[] {Material.STONE_BUTTON, Material.BIRCH_BUTTON, Material.ACACIA_BUTTON, Material.DARK_OAK_BUTTON, Material.JUNGLE_BUTTON, Material.OAK_BUTTON, Material.SPRUCE_BUTTON}: new Material[]{};
    @EventHandler
    public final void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        Material m = event.getClickedBlock().getType();
        if (Settings.IsLegacy ? (!m.equals(LegacyUtils.WOOD_BUTTON) && !m.equals(Material.STONE_BUTTON)) : Arrays.binarySearch(buttons, m) < 0) {
            return;
        }
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        } // if they left click a button which is pressed, unpress it
        if (Settings.IsLegacy) {
            if (event.getClickedBlock().getData() >= 8) {
                LegacyUtils.setData(event.getClickedBlock(), (byte) (event.getClickedBlock().getData() - 8));
            }
        } else {
            if (event.getClickedBlock().getState() instanceof Button){
                Button button = (Button) event.getClickedBlock().getState();
                if (button.isPowered()){
                    button.setPowered(false);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteractStick(PlayerInteractEvent event) {

        Craft c = CraftManager.getInstance().getCraftByPlayer(event.getPlayer());
        // if not in command of craft, don't process pilot tool clicks
        if (c == null)
            return;
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Craft craft = CraftManager.getInstance().getCraftByPlayer(event.getPlayer());

            if (event.getItem() == null || event.getItem().getType() != Settings.PilotTool) {
                return;
            }
            event.setCancelled(true);
            if (craft == null) {
                return;
            }
            Long time = timeMap.get(event.getPlayer());
            if (time != null) {
                long ticksElapsed = (System.currentTimeMillis() - time) / 50;

                // if the craft should go slower underwater, make time
                // pass more slowly there
                if (craft.getType().getHalfSpeedUnderwater() && craft.getHitBox().getMinY() < craft.getW().getSeaLevel())
                    ticksElapsed = ticksElapsed >> 1;

                if (Math.abs(ticksElapsed) < craft.getType().getTickCooldown()) {
                    return;
                }
            }

            if (!MathUtils.locationNearHitBox(craft.getHitBox(),event.getPlayer().getLocation(),2)) {
                return;
            }

            if (!event.getPlayer().hasPermission("movecraft." + craft.getType().getCraftName() + ".move")) {
                event.getPlayer().sendMessage(
                        I18nSupport.getInternationalisedString("Insufficient Permissions"));
                return;
            }
            if (craft.getPilotLocked()) {
                // right click moves up or down if using direct
                // control
                if (craft.getCruising()){
                    craft.setClimbing(true);
                    return;
                }
                int DY = 1;
                if (event.getPlayer().isSneaking())
                    DY = -1;
                craft.translate(0, DY, 0);
                timeMap.put(event.getPlayer(), System.currentTimeMillis());
                craft.setLastCruisUpdate(System.currentTimeMillis());
                return;
            }
            // Player is onboard craft and right clicking
            float rotation = (float) Math.PI * event.getPlayer().getLocation().getYaw() / 180f;

            float nx = -(float) Math.sin(rotation);
            float nz = (float) Math.cos(rotation);

            int dx = (Math.abs(nx) >= 0.5 ? 1 : 0) * (int) Math.signum(nx);
            int dz = (Math.abs(nz) > 0.5 ? 1 : 0) * (int) Math.signum(nz);
            int dy;

            float p = event.getPlayer().getLocation().getPitch();

            dy = -(Math.abs(p) >= 25 ? 1 : 0) * (int) Math.signum(p);

            if (Math.abs(event.getPlayer().getLocation().getPitch()) >= 75) {
                dx = 0;
                dz = 0;
            }

            craft.translate(dx, dy, dz);
            timeMap.put(event.getPlayer(), System.currentTimeMillis());
            craft.setLastCruisUpdate(System.currentTimeMillis());
            return;
        }
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (event.getItem() == null || event.getItem().getType() != Settings.PilotTool) {
                return;
            }
            Craft craft = CraftManager.getInstance().getCraftByPlayer(event.getPlayer());
            if (craft == null) {
                return;
            }
            if (craft.getPilotLocked()) {
                craft.setPilotLocked(false);
                event.getPlayer().sendMessage(
                        I18nSupport.getInternationalisedString("Leaving Direct Control Mode"));
                event.setCancelled(true);
                return;
            }
            if (!event.getPlayer().hasPermission("movecraft." + craft.getType().getCraftName() + ".move")
                    || !craft.getType().getCanDirectControl()) {
                        event.getPlayer().sendMessage(
                                I18nSupport.getInternationalisedString("Insufficient Permissions"));
                        return;
            }
            craft.setPilotLocked(true);
            craft.setPilotLockedX(event.getPlayer().getLocation().getBlockX() + 0.5);
            craft.setPilotLockedY(event.getPlayer().getLocation().getY());
            craft.setPilotLockedZ(event.getPlayer().getLocation().getBlockZ() + 0.5);
            event.getPlayer().sendMessage(I18nSupport.getInternationalisedString("Entering Direct Control Mode"));
            event.setCancelled(true);
        }

    }

}
