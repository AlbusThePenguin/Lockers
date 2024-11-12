package me.albusthepenguin.lockers.Locker;

import me.albusthepenguin.lockers.Lockers;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LockerListener implements Listener {

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
        this.lockerHandler.getCooldowns().remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onShift(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if(!this.lockers.getConfiguration().getYamlConfiguration().getBoolean("shift.enabled")) {
            this.lockers.getLogger().severe("use_shift_swap is disabled.");
            return;
        }

        if (!player.hasPermission("lockers.shift")) {
            this.lockers.getLogger().severe("The player does not have the valid permission.");
            return;
        }

        if(player.isSneaking() && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            this.lockerHandler.shiftSwap(player);
        }
    }

}
