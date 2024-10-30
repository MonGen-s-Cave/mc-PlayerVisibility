package hu.kxtsoo.playervisibility.commands;

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.SubCommand;
import dev.triumphteam.cmd.core.annotation.Optional;
import dev.triumphteam.cmd.core.annotation.Suggestion;
import hu.kxtsoo.playervisibility.PlayerVisibility;
import hu.kxtsoo.playervisibility.model.VisibilityItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

@Command(value = "mcplayervisibility", alias = {"playervisibility", "pvisibility", "visibility", "mc-playervisibility"})
public class GiveCommand extends BaseCommand {

    @SubCommand("give")
    @Permission("playervisibility.admin.give")
    public void execute(CommandSender sender, @Suggestion("online_players") String playerName, @Suggestion("slot_numbers") @Optional Integer slot) {
        Player target = Bukkit.getPlayer(playerName);

        if (target == null || !target.isOnline()) {
            sender.sendMessage(PlayerVisibility.getInstance().getConfigUtil().getMessage("messages.give-command.player-not-found"));
            return;
        }

        ItemStack visibilityItem;
        if (target.hasPermission("playervisibility.item.use")) {
            visibilityItem = VisibilityItem.createHideItem(PlayerVisibility.getInstance().getConfigUtil());
        } else {
            visibilityItem = VisibilityItem.createNoPermissionItem(PlayerVisibility.getInstance().getConfigUtil());
        }

        int targetSlot = (slot != null) ? slot : PlayerVisibility.getInstance().getConfigUtil().getConfig().getInt("settings.slot", 0);

        if (targetSlot < 0 || targetSlot >= target.getInventory().getSize()) {
            sender.sendMessage(PlayerVisibility.getInstance().getConfigUtil().getMessage("messages.give-command.invalid-slot"));
            return;
        }

        target.getInventory().setItem(targetSlot, visibilityItem);

        sender.sendMessage(PlayerVisibility.getInstance().getConfigUtil().getMessage("messages.give-command.success").replace("%player%", target.getName()));

        String receivedMessage = PlayerVisibility.getInstance().getConfigUtil().getMessage("messages.give-command.received");
        if(!receivedMessage.isEmpty()) {
            target.sendMessage(receivedMessage);
        }
    }
}