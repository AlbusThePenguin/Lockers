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
package me.albusthepenguin.lockers;

import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import me.albusthepenguin.lockers.Commands.LockerCommand;
import me.albusthepenguin.lockers.Configs.Configuration;
import me.albusthepenguin.lockers.Locker.LockerHandler;
import me.albusthepenguin.lockers.Listener.LockerListener;
import me.albusthepenguin.lockers.Utils.DBConnection;
import me.albusthepenguin.lockers.Utils.Menu.MenuListener;
import me.albusthepenguin.lockers.Utils.Menu.MenuUtilities;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public final class Lockers extends JavaPlugin {

    private Configuration configuration;

    private DBConnection dbConnection;

    private String commandLabel;

    private LockerHandler lockerHandler;

    private final Map<UUID, MenuUtilities> playerMenus = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);

        this.configuration = new Configuration(this);

        ConfigurationSection dbSection = this.configuration.getYamlConfiguration().getConfigurationSection("Database");
        if(dbSection == null) {
            throw new IllegalArgumentException("Could not establish database connection because 'Database' section in config.yml is null.");
        }

        this.dbConnection = new DBConnection(this, dbSection);

        this.lockerHandler = new LockerHandler(this, this.dbConnection);

        this.buildInGameCommand();

        Bukkit.getPluginManager().registerEvents(new MenuListener(this), this);
        Bukkit.getPluginManager().registerEvents(new LockerListener(this, this.lockerHandler), this);

    }

    @Override
    public void onDisable() {
        if(this.dbConnection != null) {
            this.dbConnection.stop();
        }
    }

    private void buildInGameCommand() {
        ConfigurationSection section = this.configuration.getYamlConfiguration().getConfigurationSection("Command");

        if (section == null) {
            throw new IllegalArgumentException("Could not find Commands section in config.yml. Cannot load default commands.");
        }

        this.commandLabel = section.getString("name");
        if (this.commandLabel == null || this.commandLabel.isEmpty()) {
            throw new IllegalArgumentException("The 'name' field is missing or empty in the Commands section of config.yml.");
        }

        String description = section.getString("description");
        if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("The 'description' field is missing or empty in the Commands section of config.yml.");
        }

        String usageMessage = section.getString("usage");
        if (usageMessage == null || usageMessage.isEmpty()) {
            throw new IllegalArgumentException("The 'usage' field is missing or empty in the Commands section of config.yml.");
        }

        String permission = section.getString("permission");
        if(permission == null || permission.isEmpty()) {
            throw new IllegalArgumentException("The 'permission' field is missing or empty in the Commands section of config.yml");
        }

        List<String> aliases = section.getStringList("aliases");

        ConfigurationSection subCommandsSection = section.getConfigurationSection("subcommands");
        if(subCommandsSection == null) {
            throw new IllegalArgumentException("The sub commands in config.yml is incorrectly setup. Can't find sub commands section.");
        }

        new LockerCommand(
                this, this.commandLabel, permission, description, usageMessage, aliases, subCommandsSection
        );
    }
}