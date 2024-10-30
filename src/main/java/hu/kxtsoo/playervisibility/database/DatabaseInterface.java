package hu.kxtsoo.playervisibility.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface DatabaseInterface {
    void initialize() throws SQLException;

    void createTables() throws SQLException;

    void setPlayersHidden(UUID playerUuid, boolean hidden) throws SQLException;

    CompletableFuture<Boolean> isPlayersHidden(UUID playerUuid);

    Connection getConnection() throws SQLException;

    void close() throws SQLException;
}
