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
package me.albusthepenguin.lockers.Configs;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class Configuration {

    private final Plugin plugin;

    private final File file;
    @Getter
    private YamlConfiguration yamlConfiguration;

    public Configuration(Plugin plugin) {
        this.plugin = plugin;

        this.file = getFile();

        this.yamlConfiguration = loadYamlConfiguration();
    }

    public File getFile() {
        return new File(this.plugin.getDataFolder(), "config.yml");
    }

    //This should be used to load and reload.
    public YamlConfiguration loadYamlConfiguration() {
        this.yamlConfiguration = YamlConfiguration.loadConfiguration(this.file);
        return this.yamlConfiguration;
    }
    @SuppressWarnings("unused")
    public void save() {
        try {
            this.yamlConfiguration.save(this.file);
        } catch (IOException e) {
            throw new RuntimeException("Could not save config.yml because " + e);
        }
    }

    private String getPath(String path) {
        return this.yamlConfiguration.getString("misc_messages." + path);
    }

    public String get(String path, boolean colored) {
        String message = getPath(path);
        if(message == null) {
            this.plugin.getLogger().warning("Could not find the message '" + path + "' in the loaded language file.");
            message = colored ? "&c&lError, please check console for more information." : "Error, please check console for more information.";
        }

        return colored ? color(message) : message;
    }

    public String getReplace(String path, boolean colored, Map<String, String> replacements) {
        String objective = get(path, colored);

        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            objective = objective.replace(entry.getKey(), entry.getValue());
        }
        return objective;
    }

    public String color(String text) {
        String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";
        String[] texts = text.split(String.format(WITH_DELIMITER, "&"));

        StringBuilder finalText = new StringBuilder();

        for (int i = 0; i < texts.length; i++) {
            if (texts[i].equalsIgnoreCase("&")) {
                i++;
                if (texts[i].charAt(0) == '#') {
                    finalText.append(ChatColor.of(texts[i].substring(0, 7))).append(texts[i].substring(7));
                } else {
                    finalText.append(ChatColor.translateAlternateColorCodes('&', "&" + texts[i]));
                }
            } else {
                finalText.append(texts[i]);
            }
        }
        return finalText.toString();
    }
}