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
package me.albusthepenguin.lockers.Menu;

import me.albusthepenguin.lockers.Configs.Configuration;
import me.albusthepenguin.lockers.Locker.LockerHandler;
import me.albusthepenguin.lockers.Locker.LockerKey;
import me.albusthepenguin.lockers.Lockers;
import me.albusthepenguin.lockers.Utils.Menu.Menu;
import me.albusthepenguin.lockers.Utils.Menu.MenuUtilities;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class LockerMenu extends Menu {

    private final LockerHandler lockerHandler;

    private final ItemStack unused;

    private final ItemStack filler;

    private final ItemStack close;

    private final ItemStack allow_equip;

    private final ItemStack deny_equip;

    private final ItemStack nextPage;

    private final ItemStack previousPage;

    private final Player player;

    private final UUID uuid;

    private final OfflinePlayer offlinePlayer;

    private final int maxPages;

    private final Configuration configuration;


    public LockerMenu(Lockers lockers, MenuUtilities menuUtilities, UUID uuid) {
        super(lockers, menuUtilities);

        this.lockerHandler = lockers.getLockerHandler(); //lockers.get();

        this.uuid = uuid;
        this.configuration = this.lockers.getConfiguration();

        this.player = menuUtilities.getPlayer();

        if (this.player.getUniqueId().equals(this.uuid)) {
            this.offlinePlayer = null;
        } else {
            this.offlinePlayer = Bukkit.getOfflinePlayer(this.uuid);
        }

        YamlConfiguration cfg = configuration.getYamlConfiguration();

        this.maxPages = cfg.getInt("Gui.settings.max_pages");

        this.unused = buildFromConfig(cfg.getConfigurationSection("Gui.unused"));

        this.filler = buildFromConfig(cfg.getConfigurationSection("Gui.filler"));
        this.close = buildFromConfig(cfg.getConfigurationSection("Gui.close"));

        this.allow_equip = buildFromConfig(cfg.getConfigurationSection("Gui.equip"));
        this.deny_equip = buildFromConfig(cfg.getConfigurationSection("Gui.unequip"));

        this.nextPage = buildFromConfig(cfg.getConfigurationSection("Gui.next"));
        this.previousPage = buildFromConfig(cfg.getConfigurationSection("Gui.back"));
    }

    @Override
    public String getMenuName() {
        return "&b&lLockers";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void onMenuClick(InventoryClickEvent event) {

        event.setCancelled(true);

        int row = event.getSlot();

        if(row == 53) {
            this.player.getOpenInventory().close();
        } else if(row == 0) {
            if(this.page == 0) {
                this.player.sendMessage(this.configuration.get("gui_back_error", true));
            } else {
                this.save(this.inventory);
                this.player.playSound(this.player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1, 1);
                this.page -= 1;
                this.getLockers().getLogger().severe("Switching to page is " + this.page);
                this.open();
            }
        } else if(row == 8) {
            if(this.page != this.maxPages) {
                this.save(this.inventory);
                this.player.playSound(this.player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1, 1);
                this.page += 1;
                this.getLockers().getLogger().severe("Switching to page is " + this.page);
                this.open();
            } else {
                this.player.sendMessage(this.configuration.get("gui_last_error", true));
            }
        } else {
            int[] armorSlots = getSlotsFromButton(row);
            if(armorSlots != null) {
                if(hasRowPermission(row)) {
                    this.switchArmor(this.player, armorSlots);
                    this.player.getOpenInventory().close();
                } else {
                    this.player.sendMessage(this.configuration.get("error_row_permission", true));
                }
            }
        }
    }

    @Override
    public void onMenuClose(InventoryCloseEvent event) {
        this.save(event.getInventory());
    }

    @Override
    public void setMenuItems() {
        Map<Integer, ItemStack[]> pageItems = getPlayerInventory();

        ItemStack[] items = pageItems.get(this.page);
        if(items != null) {
            this.inventory.setContents(items);
        }

        this.inventory.setItem(0, this.previousPage);
        this.inventory.setItem(8, this.nextPage);
        this.inventory.setItem(53, this.close);

        int[] switchButtons = new int[]{46, 47, 48, 49, 50, 51, 52};
        for (int buttonSlot : switchButtons) {
            this.inventory.setItem(buttonSlot, this.hasRowPermission(buttonSlot) ? this.allow_equip : this.deny_equip);
            int[] armorSlots = getSlotsFromButton(buttonSlot);
            if(armorSlots != null) {
                for (int slot : armorSlots) {
                    if(this.inventory.getItem(slot) == null) {
                        this.inventory.setItem(slot, this.unused);
                    }
                }
            }
        }

        this.setFillers(this.filler);
    }

    public Map<Integer, ItemStack[]> getPlayerInventory() {
        Map<Integer, ItemStack[]> inventory = new HashMap<>();

        if (!this.player.getUniqueId().equals(this.uuid)) {
            if (this.offlinePlayer == null || !this.offlinePlayer.hasPlayedBefore()) return null;
            this.lockerHandler.loadAll(this.uuid);
        }

        // Iterate through each page and check for items using LockerKey
        for (int row = 0; row < this.maxPages; row++) {
            LockerKey key = new LockerKey(this.uuid, row); // Create a LockerKey instance for the current page
            ItemStack[] items = this.lockerHandler.getPlayerLockers().get(key);
            if (items != null) {
                inventory.put(row, items);
            }
        }

        return inventory;
    }

    public boolean hasRowPermission(int id) {
        int highest = getHighestPermission();

        int rowInPage = id - 46 + 1;
        int lookingFor = (this.page * 7) + rowInPage;

        return highest >= lookingFor;
    }

    public int getHighestPermission() {
        int currentMax = 0;
        boolean matched = false;
        final Set<PermissionAttachmentInfo> finalEffectivePermissions = this.player.getEffectivePermissions();
        for (PermissionAttachmentInfo effectivePermission : finalEffectivePermissions) {
            String permission = effectivePermission.getPermission();
            if (!permission.startsWith("lockers.row.")) continue;

            String numberStr = permission.substring("lockers.row.".length());
            try {
                int rowNumber = Integer.parseInt(numberStr);

                if (currentMax < rowNumber) {
                    currentMax = rowNumber;
                    matched = true;
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }

        return matched ? currentMax : 0;
    }

    private int[] getSlotsFromButton(int id) {
        return switch (id) {
            case 46 -> new int[]{10, 19, 28, 37};
            case 47 -> new int[]{11, 20, 29, 38};
            case 48 -> new int[]{12, 21, 30, 39};
            case 49 -> new int[]{13, 22, 31, 40};
            case 50 -> new int[]{14, 23, 32, 41};
            case 51 -> new int[]{15, 24, 33, 42};
            case 52 -> new int[]{16, 25, 34, 43};
            default -> null;
        };
    }

    public void save(Inventory inventory) {
        Map<Integer, ItemStack[]> items = new HashMap<>();

        items.put(this.page, inventory.getContents());

        if(this.offlinePlayer == null || this.offlinePlayer.isOnline()) {
            this.lockerHandler.update(this.uuid, items);
        } else {
            this.lockerHandler.saveAll(this.uuid);
        }
    }

    public ItemStack buildFromConfig(ConfigurationSection section) {
        if(section == null) {
            throw new IllegalArgumentException("Section is null, could not 'buildFromConfig'.");
        }

        String materialName = section.getString("material");

        if(materialName == null) {
            throw new IllegalArgumentException(section.getName() + " has no material. Please add 'material:'");
        }

        Material material = Material.getMaterial(materialName);
        if(material == null) {
            throw new IllegalArgumentException(section.getName() + ".material is invalid. Please update the material to a valid material.");
        }

        ItemStack itemStack = new ItemStack(material);

        String base64 = section.getString("base64");
        if(base64 != null && material == Material.PLAYER_HEAD) {
            itemStack = this.getSkull(base64);
        }

        ItemMeta meta = itemStack.getItemMeta();
        assert meta != null;

        String displayName = section.getString("display");
        if(displayName == null) {
            displayName = "";
        }

        meta.setDisplayName(this.color(displayName));
        meta.setLore(section.getStringList("lore").stream().map(this::color).collect(Collectors.toList()));

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(this.getKey(), PersistentDataType.STRING, section.getName());

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public void switchArmor(Player player, int[] slots) {
        PlayerInventory playerInventory = player.getInventory();
        EquipmentSlot[] armorSlots = {
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        };

        for (int i = 0; i < armorSlots.length; i++) {
            ItemStack dresserItem = this.inventory.getItem(slots[i]);
            ItemStack playerArmorItem = getArmorPiece(playerInventory, armorSlots[i]);

            if (dresserItem != null && dresserItem.getType() != Material.AIR) {
                ItemMeta meta = dresserItem.getItemMeta();
                if (meta == null || !meta.getPersistentDataContainer().has(this.key, PersistentDataType.STRING)) {
                    setArmorPiece(playerInventory, armorSlots[i], dresserItem);
                } else {
                    setArmorPiece(playerInventory, armorSlots[i], null);
                }
            }

            if (playerArmorItem != null && playerArmorItem.getType() != Material.AIR) {
                this.inventory.setItem(slots[i], playerArmorItem);
            } else {
                this.inventory.setItem(slots[i], unused);
            }
        }

        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1.0f, 0.3f);
    }

    private ItemStack getArmorPiece(PlayerInventory inventory, EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> inventory.getHelmet();
            case CHEST -> inventory.getChestplate();
            case LEGS -> inventory.getLeggings();
            case FEET -> inventory.getBoots();
            default -> null;
        };
    }

    private void setArmorPiece(PlayerInventory inventory, EquipmentSlot slot, ItemStack item) {
        switch (slot) {
            case HEAD:
                inventory.setHelmet(item);
                break;
            case CHEST:
                inventory.setChestplate(item);
                break;
            case LEGS:
                inventory.setLeggings(item);
                break;
            case FEET:
                inventory.setBoots(item);
                break;
        }
    }
}
