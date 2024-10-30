package hu.kxtsoo.playervisibility.database.impl;

import hu.kxtsoo.playervisibility.PlayerVisibility;
import hu.kxtsoo.playervisibility.database.DatabaseInterface;

import java.io.File;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SQLite implements DatabaseInterface {

    private final PlayerVisibility plugin;
    private Connection connection;

    public SQLite(PlayerVisibility plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() throws SQLException {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        String url = "jdbc:sqlite:" + new File(dataFolder, "database.db").getAbsolutePath();
        connection = DriverManager.getConnection(url);

        createTables();
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void createTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS playervisibility_users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "uuid TEXT NOT NULL UNIQUE, " +
                    "players_hided BOOLEAN DEFAULT FALSE" +
                    ");");
        }
    }

    @Override
    public void setPlayersHidden(UUID playerUuid, boolean hidden) throws SQLException {
        String sql = "INSERT OR REPLACE INTO playervisibility_users (uuid, players_hided) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerUuid.toString());
            statement.setBoolean(2, hidden);
            statement.executeUpdate();
        }
    }

    @Override
    public CompletableFuture<Boolean> isPlayersHidden(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT players_hided FROM playervisibility_users WHERE uuid = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, playerUuid.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getBoolean("players_hided");
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Error checking player visibility for SQLite: " + e.getMessage());
            }
            return false;
        });
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
