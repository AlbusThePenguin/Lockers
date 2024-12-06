/*
 * This file is part of Lockers.
 *
 * Lockers is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Lockers are distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Lockers. If not, see <http://www.gnu.org/licenses/>.
 */
package me.albusthepenguin.lockers.Listener;

import lombok.Getter;
import me.albusthepenguin.lockers.Locker.LockerHandler;
import me.albusthepenguin.lockers.Lockers;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LockerListener implements Listener {

    @Getter
    private final Lockers lockers;

    private final LockerHandler lockerHandler;

    public LockerListener(Lockers lockers, LockerHandler lockerHandler) {
        this.lockers = lockers;
        this.lockerHandler = lockerHandler;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        this.lockerHandler.loadAll(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.lockerHandler.unload(event.getPlayer().getUniqueId());
        this.lockerHandler.getShift().getCooldowns().remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onShift(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if(!this.lockers.getConfiguration().getYamlConfiguration().getBoolean("shift.enabled")) {
            return;
        }

        if (!player.hasPermission("lockers.shift")) {
            return;
        }

        if (player.isSneaking() &&
                (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            this.lockerHandler.getShift().swap(player);
        }
    }

}
