package me.albusthepenguin.lockers.Utils;

import me.albusthepenguin.lockers.Lockers;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MenuListener implements Listener {

    private final Lockers lockers;

    public MenuListener(Lockers lockers) {
        this.lockers = lockers;
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof Menu menu) {
            menu.onMenuClick(event);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if(event.getInventory().getHolder() instanceof Menu menu) menu.onMenuClose(event);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.lockers.getPlayerMenus().remove(event.getPlayer().getUniqueId());
    }
}