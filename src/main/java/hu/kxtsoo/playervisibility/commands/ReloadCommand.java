package hu.kxtsoo.playervisibility.commands;

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.SubCommand;
import hu.kxtsoo.playervisibility.PlayerVisibility;
import hu.kxtsoo.playervisibility.database.DatabaseManager;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;

@Command(value = "mcplayervisibility", alias = {"playervisibility", "pvisibility", "visibility", "mc-playervisibility"})
public class ReloadCommand extends BaseCommand {

    @SubCommand("reload")
    @Permission("playervisibility.admin.reload")
    public void executor(CommandSender sender) {
        PlayerVisibility.getInstance().getConfigUtil().reloadConfig();
        sender.sendMessage(PlayerVisibility.getInstance().getConfigUtil().getMessage("messages.reload-command.success"));

        try {
            DatabaseManager.initialize(PlayerVisibility.getInstance().getConfigUtil(), PlayerVisibility.getInstance());
            sender.sendMessage(PlayerVisibility.getInstance().getConfigUtil().getMessage("messages.reload-command.db-success"));
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage(PlayerVisibility.getInstance().getConfigUtil().getMessage("messages.database-error"));
        }
    }
}
