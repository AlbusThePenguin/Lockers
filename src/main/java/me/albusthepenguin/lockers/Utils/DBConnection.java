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
        String type = section.getString("type", "SQLite"); // Database type (MySQL or SQLite)

        String databaseName = section.getString("database", "lockers");

        this.db_prefix = section.getString("prefix", "lr_");

        HikariConfig config = new HikariConfig();
        String url;

        switch (type.toUpperCase()) {
            case "MYSQL":
                url = "jdbc:mysql://" + section.getString("address", "localhost") + "/" + databaseName + section.getString("properties", "?useSSL=false&requireSSL=false&verifyServerCertificate=false");
                config.setUsername(section.getString("username", "root"));
                config.setPassword(section.getString("password", ""));
                this.usingMySQL = true;
                break;
            case "SQLITE":
                url = "jdbc:sqlite:" + new File(plugin.getDataFolder(), databaseName + ".db").getAbsolutePath();
                break;

            default:
                throw new RuntimeException("Unsupported database type: " + type);
        }

        config.setJdbcUrl(url);

        configureHikariSettings(config, section);

        this.hikariDataSource = new HikariDataSource(config);
    }

    private void configureHikariSettings(HikariConfig config, ConfigurationSection section) {
        config.setMaximumPoolSize(section.getInt("pool-settings.maximum-pool-size", 10));
        config.setMinimumIdle(section.getInt("pool-settings.maximum-idle", 10));
        config.setMaxLifetime(section.getLong("pool-settings.maximum-lifetime", 1800000));
        config.setIdleTimeout(section.getLong("pool-settings.keeplive-time", 0));
        config.setConnectionTimeout(section.getLong("pool-settings.connection-timeout", 5000));

        config.addDataSourceProperty("cachePrepStmts", section.getBoolean("statement-cache-settings.cachePrepStmts", true));
        config.addDataSourceProperty("prepStmtCacheSize", section.getInt("statement-cache-settings.prepStmtCacheSize", 250));
        config.addDataSourceProperty("prepStmtCacheSqlLimit", section.getInt("statement-cache-settings.prepStmtCacheSqlLimit", 2048));
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