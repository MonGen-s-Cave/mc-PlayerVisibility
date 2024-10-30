package hu.kxtsoo.playervisibility.manager;

import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import dev.triumphteam.cmd.core.suggestion.SuggestionKey;
import hu.kxtsoo.playervisibility.commands.GiveCommand;
import hu.kxtsoo.playervisibility.commands.ReloadCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;


public class CommandManager {

    private final BukkitCommandManager<CommandSender> commandManager;

    public CommandManager(JavaPlugin plugin) {
        this.commandManager = BukkitCommandManager.create(plugin);
    }

    public void registerSuggestions() {
        commandManager.registerSuggestion(SuggestionKey.of("online_players"), (sender, context) -> {
            if (sender instanceof Player) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .toList();
            }
            return List.of();
        });

        commandManager.registerSuggestion(SuggestionKey.of("slot_numbers"), (sender, context) -> List.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17",
                "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33",
                "34", "35"));
    }

    public void registerCommands() {
        commandManager.registerCommand(new ReloadCommand());
        commandManager.registerCommand(new GiveCommand());
    }
}
