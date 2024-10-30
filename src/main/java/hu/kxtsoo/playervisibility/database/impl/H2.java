package hu.kxtsoo.playervisibility.database.impl;

import hu.kxtsoo.playervisibility.PlayerVisibility;
import hu.kxtsoo.playervisibility.database.DatabaseInterface;
import org.h2.jdbc.JdbcConnection;

import java.io.File;
import java.sql.*;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class H2 implements DatabaseInterface {

    private final PlayerVisibility plugin;
    private Connection connection;

    public H2(PlayerVisibility plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() throws SQLException {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            String url = "jdbc:h2:" + new File(dataFolder, "data").getAbsolutePath() + ";mode=MySQL";
            Properties props = new Properties();
            connection = new JdbcConnection(url, props, null, null, false);

            connection.setAutoCommit(true);

            createTables();
        } catch (SQLException e) {
            throw new RuntimeException("Could not connect to the H2 database", e);
        }
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void createTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS playervisibility_users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "uuid VARCHAR(36) NOT NULL UNIQUE, " +
                    "players_hided BOOLEAN DEFAULT FALSE" +
                    ");");
        }
    }

    @Override
    public void setPlayersHidden(UUID playerUuid, boolean hidden) throws SQLException {
        String sql = "MERGE INTO playervisibility_users (uuid, players_hided) KEY (uuid) VALUES (?, ?)";
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
                plugin.getLogger().severe("Error checking player visibility for H2: " + e.getMessage());
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
