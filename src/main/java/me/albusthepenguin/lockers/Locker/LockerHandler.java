/*
 * This file is part of Lockers.
 *
 * Lockers is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Lockers is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Lockers. If not, see <http://www.gnu.org/licenses/>.
 */
package me.albusthepenguin.lockers.Locker;

import lombok.Getter;
import me.albusthepenguin.lockers.Lockers;
import me.albusthepenguin.lockers.Utils.DBConnection;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LockerHandler {

    private final Lockers lockers;

    private final DBConnection dbConnection;

    private final String table;

    @Getter
    private final Map<LockerKey, ItemStack[]> playerLockers = new HashMap<>();
    @Getter
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    private final NamespacedKey namespacedKey;

    public LockerHandler(Lockers lockers, DBConnection dbConnection) {
        this.lockers = lockers;
        this.dbConnection = dbConnection;
        this.table = dbConnection.getDb_prefix() + "locker";

        this.namespacedKey = new NamespacedKey(lockers, "clicked");

        String query = "CREATE TABLE IF NOT EXISTS " + this.table + " ("
                + "`uuid` VARCHAR(36) NOT NULL,"
                + "`page` INT NOT NULL,"
                + "`locker` BLOB NOT NULL,"
                + "PRIMARY KEY (`uuid`, `page`)"
                + ");";

        try (Connection connection = dbConnection.get();
             Statement statement = connection.createStatement()) {
            statement.execute(query);
        } catch (SQLException e) {
            throw new RuntimeException("Could not generate the tables: " + e.getSQLState() + ": " + e.getMessage());
        }
    }

    public void loadAll(UUID uuid) {
        String query = "SELECT `page`, `locker` FROM " + this.table + " WHERE `uuid` = ?";

        try (Connection connection = dbConnection.get();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, uuid.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                Map<Integer, ItemStack[]> userLockers = new HashMap<>();
                while (resultSet.next()) {
                    int page = resultSet.getInt("page");
                    ItemStack[] items = deserialize(resultSet.getBytes("locker"));
                    userLockers.put(page, items);
                }

                for (Map.Entry<Integer, ItemStack[]> entry : userLockers.entrySet()) {
                    playerLockers.put(new LockerKey(uuid, entry.getKey()), entry.getValue());
                }

                if (userLockers.isEmpty()) {
                    playerLockers.put(new LockerKey(uuid, 0), new ItemStack[0]);
                }
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException("Could not load lockers for UUID " + uuid + ": " + e.getSQLState() + ": " + e.getMessage());
        }
    }

    public void update(UUID uuid, Map<Integer, ItemStack[]> pages) {
        for (Map.Entry<Integer, ItemStack[]> entry : pages.entrySet()) {
            playerLockers.put(new LockerKey(uuid, entry.getKey()), entry.getValue());
        }
    }

    public void saveAll(UUID uuid) {
        if (!playerLockers.containsKey(new LockerKey(uuid, 0))) return;

        String query = dbConnection.isUsingMySQL() ?
                "INSERT INTO " + this.table + " (`uuid`, `page`, `locker`) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE `locker` = VALUES(`locker`)" :
                "INSERT OR REPLACE INTO " + this.table + " (`uuid`, `page`, `locker`) VALUES (?, ?, ?)";

        try (Connection connection = dbConnection.get();
             PreparedStatement statement = connection.prepareStatement(query)) {

            for (Map.Entry<LockerKey, ItemStack[]> entry : playerLockers.entrySet()) {
                LockerKey key = entry.getKey();
                ItemStack[] items = entry.getValue();

                statement.setString(1, key.getUuid().toString());
                statement.setInt(2, key.getPage());
                statement.setBytes(3, serialize(items));
                statement.addBatch();  // Add to batch
            }

            statement.executeBatch();

            playerLockers.clear();

        } catch (SQLException e) {
            throw new IllegalArgumentException("Could not save all pages for " + uuid + ": " + e.getSQLState() + ": " + e.getMessage());
        }
    }

    public void shiftSwap(Player player) {
        if (!checkAndApplyCooldown(player)) {
            return;
        }

        // Load in the items for page 0
        LockerKey lockerKey = new LockerKey(player.getUniqueId(), 0);
        ItemStack[] contents = this.playerLockers.get(lockerKey);
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

        this.playerLockers.put(lockerKey, contents);
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1.0f, 0.3f);
    }

    private boolean checkAndApplyCooldown(Player player) {
        UUID playerUUID = player.getUniqueId();
        long length = this.lockers.getConfiguration().getYamlConfiguration().getLong("cooldown");

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
        return itemMeta != null && itemMeta.getPersistentDataContainer().has(this.namespacedKey, PersistentDataType.STRING);
    }

    // Unload all data for a player, saving pages as needed
    public void unload(UUID uuid) {
        saveAll(uuid);
    }

    private byte[] serialize(ItemStack[] itemStackArray) {
        try {
            ByteArrayOutputStream io = new ByteArrayOutputStream();
            BukkitObjectOutputStream os = new BukkitObjectOutputStream(io);
            os.writeObject(itemStackArray);
            os.flush();
            return io.toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not serialize inventory: " + e.getMessage());
        }
    }

    private ItemStack[] deserialize(byte[] serializedItemStackArray) {
        if (serializedItemStackArray == null) {
            return null;
        }

        try {
            ByteArrayInputStream in = new ByteArrayInputStream(serializedItemStackArray);
            BukkitObjectInputStream is = new BukkitObjectInputStream(in);
            return (ItemStack[]) is.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalArgumentException("Could not deserialize inventory: " + e.getMessage());
        }
    }
}