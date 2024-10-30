package hu.kxtsoo.playervisibility.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hu.kxtsoo.playervisibility.PlayerVisibility;
import hu.kxtsoo.playervisibility.database.DatabaseInterface;
import hu.kxtsoo.playervisibility.utils.ConfigUtil;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MySQL implements DatabaseInterface {

    private final ConfigUtil configUtil;
    private final PlayerVisibility plugin;
    private HikariDataSource dataSource;

    public MySQL(ConfigUtil configUtil, PlayerVisibility plugin) {
        this.configUtil = configUtil;
        this.plugin = plugin;
    }

    @Override
    public void initialize() throws SQLException {
        HikariConfig hikariConfig = new HikariConfig();

        String host = configUtil.getConfig().getString("storage.host", "localhost");
        String port = configUtil.getConfig().getString("storage.port", "3306");
        String database = configUtil.getConfig().getString("storage.name", "database_name");
        String username = configUtil.getConfig().getString("storage.username", "root");
        String password = configUtil.getConfig().getString("storage.password", "");

        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false";
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        hikariConfig.setMaximumPoolSize(configUtil.getConfig().getInt("storage.pool.maximumPoolSize", 10));
        hikariConfig.setMinimumIdle(configUtil.getConfig().getInt("storage.pool.minimumIdle", 5));
        hikariConfig.setConnectionTimeout(configUtil.getConfig().getInt("storage.pool.connectionTimeout", 30000));
        hikariConfig.setMaxLifetime(configUtil.getConfig().getInt("storage.pool.maxLifetime", 1800000));
        hikariConfig.setIdleTimeout(configUtil.getConfig().getInt("storage.pool.idleTimeout", 600000));

        dataSource = new HikariDataSource(hikariConfig);
        createTables();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void createTables() throws SQLException {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS playervisibility_users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "uuid VARCHAR(36) NOT NULL UNIQUE, " +
                    "players_hided BOOLEAN DEFAULT FALSE" +
                    ");");
        }
    }

    @Override
    public void setPlayersHidden(UUID playerUuid, boolean hidden) throws SQLException {
        String sql = "INSERT INTO playervisibility_users (uuid, players_hided) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE players_hided = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerUuid.toString());
            statement.setBoolean(2, hidden);
            statement.setBoolean(3, hidden);
            statement.executeUpdate();
        }
    }

    @Override
    public CompletableFuture<Boolean> isPlayersHidden(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                String sql = "SELECT players_hided FROM playervisibility_users WHERE uuid = ?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, playerUuid.toString());
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            return resultSet.getBoolean("players_hided");
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Error checking player visibility for MySQL: " + e.getMessage());
            }
            return false;
        });
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}