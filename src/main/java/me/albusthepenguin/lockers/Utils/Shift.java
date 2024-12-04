package me.albusthepenguin.lockers.Utils;

import lombok.Getter;
import me.albusthepenguin.lockers.Locker.LockerHandler;
import me.albusthepenguin.lockers.Locker.LockerKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Shift {

    private final LockerHandler lockerHandler;

    @Getter
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public Shift(LockerHandler lockerHandler) {
        this.lockerHandler = lockerHandler;
    }

    public void swap(Player player) {
        if (!checkAndApplyCooldown(player)) {
            return;
        }

        // Load in the items for page 0
        LockerKey lockerKey = new LockerKey(player.getUniqueId(), 0);
        ItemStack[] contents = this.lockerHandler.getPlayerLockers().get(lockerKey);
        if (contents == null || contents.length == 0) {
            contents = new ItemStack[54];
        }

        // Define item slot mappings
        Map<EquipmentSlot, Integer> locationItems = Map.of(
                EquipmentSlot.HEAD, 10,
                EquipmentSlot.CHEST, 19,
                EquipmentSlot.LEGS, 28,
                EquipmentSlot.FEET, 37
        );

        for (Map.Entry<EquipmentSlot, Integer> entry : locationItems.entrySet()) {
            EquipmentSlot equipmentSlot = entry.getKey();
            int lockerIndex = entry.getValue();

            ItemStack lockerItem = contents[lockerIndex];
            ItemStack playerItem = player.getInventory().getItem(equipmentSlot);

            if (hasPersistentData(lockerItem)) lockerItem = null;

            player.getInventory().setItem(equipmentSlot, lockerItem);

            contents[lockerIndex] = playerItem;
        }

        this.lockerHandler.getPlayerLockers().put(lockerKey, contents);
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1.0f, 0.3f);
    }

    private boolean checkAndApplyCooldown(Player player) {
        UUID playerUUID = player.getUniqueId();
        long length = this.lockerHandler.getLockers().getConfiguration().getYamlConfiguration().getLong("cooldown");

        if (length < 1) length = 1;
        length *= 1000;

        long currentTime = System.currentTimeMillis();

        if (this.cooldowns.containsKey(playerUUID)) {
            long cooldownEndTime = this.cooldowns.get(playerUUID);

            if (currentTime < cooldownEndTime) {
                return false;
            } else {
                this.cooldowns.remove(playerUUID);
            }
        }

        this.cooldowns.put(playerUUID, currentTime + length);
        return true;
    }

    private boolean hasPersistentData(@Nullable ItemStack item) {
        if(item == null) return false;
        ItemMeta itemMeta = item.getItemMeta();
        return itemMeta != null && itemMeta.getPersistentDataContainer().has(this.lockerHandler.getNamespacedKey(), PersistentDataType.STRING);
    }
}
