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

        if (configUtil.getConfig().getBoolean("update-checker.enabled", true)) {
            new UpdateChecker(this, configUtil, 6684);
        }

        String purple = "\u001B[38;2;136;71;212m";
        String reset = "\u001B[0m";
        String yellow = "\u001B[33m";
        String software = getServer().getName();
        String version = getServer().getVersion();

        System.out.println(" ");
        System.out.println(purple + "    ____  _        _ __   _______ ______     _____ ____ ___ _     ___ _______   __" + reset);
        System.out.println(purple + "   |  _ \\| |      / \\\\ \\ / / ____|  _ \\ \\   / /_ _| __ )_ _| |   |_ _|_   _\\ \\ / /" + reset);
        System.out.println(purple + "   | |_) | |     / _ \\\\ V /|  _| | |_) \\ \\ / / | ||  _ \\| || |    | |  | |  \\ V /" + reset);
        System.out.println(purple + "   |  __/| |___ / ___ \\| | | |___|  _ < \\ V /  | || |_) | || |___ | |  | |   | |" + reset);
        System.out.println(purple + "   |_|   |_____/_/   \\_\\_| |_____|_| \\_\\ \\_/  |___|____/___|_____|___| |_|   |_| " + reset);
        System.out.println(" ");
        System.out.println(purple + "   The plugin successfully started." + reset);
        System.out.println(purple + "   mc-PlayerVisibility " + software + " " + version + reset);
        System.out.println(yellow + "   Discord @ dc.mongenscave.com" + reset);
        System.out.println(" ");
        getLogger().info("\u001B[33m   [Database] Selected database type: " + DatabaseManager.getDatabaseType() + "\u001B[0m" );
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
