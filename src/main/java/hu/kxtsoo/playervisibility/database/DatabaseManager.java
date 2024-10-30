package hu.kxtsoo.playervisibility.database;

import hu.kxtsoo.playervisibility.PlayerVisibility;
import hu.kxtsoo.playervisibility.database.impl.H2;
import hu.kxtsoo.playervisibility.database.impl.MySQL;
import hu.kxtsoo.playervisibility.database.impl.SQLite;
import hu.kxtsoo.playervisibility.utils.ConfigUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {

    private static DatabaseInterface database;

    public static void initialize(ConfigUtil configUtil, PlayerVisibility plugin) throws SQLException {
        String driver = configUtil.getConfig().getString("storage.driver", "h2");
        switch (driver.toLowerCase()) {
            case "sqlite":
                database = new SQLite(plugin);
                database.initialize();
                break;
            case "mysql":
                database = new MySQL(configUtil, plugin);
                database.initialize();
                break;
            case "h2":
                database = new H2(plugin);
                database.initialize();
                break;
            default:
                throw new IllegalArgumentException("Unsupported database driver: " + driver);
        }

        database.createTables();
    }

    public static void setPlayersHidden(UUID playerUuid, boolean hidden) {
        CompletableFuture.runAsync(() -> {
            try {
                database.setPlayersHidden(playerUuid, hidden);
            } catch (SQLException e) {
                PlayerVisibility.getInstance().getLogger().severe("Error saving player visibility: " + e.getMessage());
            }
        });
    }

    public static CompletableFuture<Boolean> isPlayersHidden(UUID playerUuid) {
        return database.isPlayersHidden(playerUuid);
    }

    public static Connection getConnection() throws SQLException {
        if (database != null) {
            return database.getConnection();
        }
        throw new SQLException("Database is not initialized.");
    }

    public static void close() throws SQLException {
        if (database != null) {
            database.close();
        }
    }
}
