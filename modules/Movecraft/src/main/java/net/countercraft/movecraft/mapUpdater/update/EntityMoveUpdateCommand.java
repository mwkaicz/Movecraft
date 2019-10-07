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

package net.countercraft.movecraft.mapUpdater.update;

import net.countercraft.movecraft.utils.TeleportUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Objects;

/**
 * Class that stores the data about a single blocks changes to the map in an unspecified world. The world is retrieved contextually from the submitting craft.
 */
public class EntityMoveUpdateCommand extends UpdateCommand {
    private final Entity entity;
    private final double x;
    private double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    public EntityMoveUpdateCommand(Entity entity, double x, double y, double z, float yaw, float pitch) {
        this.entity = entity;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Entity getEntity() {
        return entity;
    }

    @Override
    public void doUpdate() {
        Location playerLoc = entity.getLocation();
        if (!(entity instanceof Player) || Math.abs(yaw) > .01 || pitch > .01) {
            entity.teleport(new Location(entity.getWorld(), x + playerLoc.getX(), y + playerLoc.getY(), z + playerLoc.getZ(), yaw + playerLoc.getYaw(), pitch + playerLoc.getPitch()), PlayerTeleportEvent.TeleportCause.UNKNOWN);
            return;
        }
        //Movecraft.getInstance().getWorldHandler().addPlayerLocation((Player) entity,x,y,z,yaw,pitch);
        TeleportUtils.teleport((Player) entity, new Location(entity.getWorld(), playerLoc.getX() + x, playerLoc.getY() + y, playerLoc.getZ() + z));
    }

    public void setY(int y){
        this.y = y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(entity.getUniqueId(), x, y, z, pitch, yaw);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof EntityMoveUpdateCommand)){
            return false;
        }
        EntityMoveUpdateCommand other = (EntityMoveUpdateCommand) obj;
        return this.x == other.x &&
                this.y == other.y &&
                this.z == other.z &&
                this.pitch == other.pitch &&
                this.yaw == other.yaw &&
                this.entity.equals(other.entity);
    }
}

