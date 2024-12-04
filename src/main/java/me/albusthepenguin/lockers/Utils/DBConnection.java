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
package me.albusthepenguin.lockers.Utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class DBConnection {

    private final HikariDataSource hikariDataSource;
    @Getter
    private final String db_prefix;
    @Getter
    private boolean usingMySQL = false;

    public DBConnection(Plugin plugin, @NonNull ConfigurationSection section) {
        String type = section.getString("type"); // Added to read database type
        String databasePort = section.getString("port");
        String databaseAddress = section.getString("address");
        String databaseName = section.getString("database");
        String databaseUsername = section.getString("username");
        String databasePassword = section.getString("password");

        this.db_prefix = section.getString("prefix");

        if (this.db_prefix == null) {
            throw new RuntimeException("Prefix cannot be null.");
        }

        HikariConfig config = new HikariConfig();

        String url;
        if ("MySQL".equalsIgnoreCase(type)) {
            url = "jdbc:mysql://" + databaseAddress + ":" + databasePort + "/" + databaseName;
            config.setUsername(databaseUsername);
            config.setPassword(databasePassword);
            this.usingMySQL = true;
        } else if ("SQLite".equalsIgnoreCase(type)) {
            url = "jdbc:sqlite:" + new File(plugin.getDataFolder(), "storage.db").getAbsolutePath();
        } else {
            throw new RuntimeException("Unsupported database type: " + type);
        }
        config.setJdbcUrl(url);

        config.setJdbcUrl(url);

        // Shared HikariCP settings for both MySQL and SQLite
        config.setMinimumIdle(5);
        config.setMaximumPoolSize(10);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.hikariDataSource = new HikariDataSource(config);
    }

    public Connection get() throws SQLException {
        return hikariDataSource.getConnection();
    }

    public void stop() {
        if (this.hikariDataSource != null) {
            this.hikariDataSource.close();
        }
    }
}