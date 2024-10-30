package hu.kxtsoo.playervisibility;

import hu.kxtsoo.playervisibility.database.DatabaseManager;
import hu.kxtsoo.playervisibility.events.*;
import hu.kxtsoo.playervisibility.manager.CommandManager;
import hu.kxtsoo.playervisibility.manager.VisibilityManager;
import hu.kxtsoo.playervisibility.utils.ConfigUtil;
import hu.kxtsoo.playervisibility.utils.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.logging.Level;

public final class PlayerVisibility extends JavaPlugin {

    private static PlayerVisibility instance;
    private ConfigUtil configUtil;
    private VisibilityManager visibilityManager;

    @Override
    public void onEnable() {
        instance = this;

        int pluginId = 23762;
        new Metrics(this, pluginId);

        this.configUtil = new ConfigUtil(this);
        ConfigUtil.configUtil = this.configUtil;

        try {
            DatabaseManager.initialize(configUtil, this);
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to initialize database", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        CommandManager commandManager = new CommandManager(this);
        commandManager.registerSuggestions();
        commandManager.registerCommands();

        this.visibilityManager = new VisibilityManager(this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDropItemListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerSwapHandItemsListener(), this);

        if (getConfig().getBoolean("update-checker.enabled", true)) {
            new UpdateChecker(this, configUtil, 5557);
        }
    }

    @Override
    public void onDisable() {
        try {
            DatabaseManager.close();
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to close database", e);
        }
    }

    public static PlayerVisibility getInstance() {
        return instance;
    }

    public ConfigUtil getConfigUtil() {
        return configUtil;
    }

    public VisibilityManager getVisibilityManager() {
        return visibilityManager;
    }
}
