package hu.kxtsoo.playervisibility.manager;

import hu.kxtsoo.playervisibility.PlayerVisibility;
import hu.kxtsoo.playervisibility.database.DatabaseManager;
import hu.kxtsoo.playervisibility.model.VisibilityItem;
import org.bukkit.entity.Player;
import org.h2.engine.Database;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VisibilityManager {

    private final Set<Player> hiddenPlayers = new HashSet<>();
    private final PlayerVisibility plugin;

    public VisibilityManager(PlayerVisibility plugin) {
        this.plugin = plugin;
    }

    public void togglePlayerVisibility(Player player) {
        UUID playerUuid = player.getUniqueId();
        int slot = plugin.getConfigUtil().getConfig().getInt("settings.slot", 0);

        SchedulerManager.run(() -> {
            boolean isHidden = hiddenPlayers.contains(player);

            if (isHidden) {
                showAllPlayers(player);
                hiddenPlayers.remove(player);
                player.getInventory().setItem(slot, VisibilityItem.createHideItem(plugin.getConfigUtil()));
            } else {
                hideAllPlayers(player);
                hiddenPlayers.add(player);
                player.getInventory().setItem(slot, VisibilityItem.createShowItem(plugin.getConfigUtil()));
            }

            SchedulerManager.runAsync(() -> {
                try {
                    DatabaseManager.setPlayersHidden(playerUuid, !isHidden);
                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to save player visibility: " + e.getMessage());
                }
            });
        });
    }

    public void loadPlayerVisibility(Player player) {
        UUID playerUuid = player.getUniqueId();
        int slot = plugin.getConfigUtil().getConfig().getInt("settings.slot", 0);

        DatabaseManager.isPlayersHidden(playerUuid).thenAccept(isHidden -> {
            SchedulerManager.run(() -> {
                if (isHidden) {
                    hideAllPlayers(player);
                    hiddenPlayers.add(player);
                    player.getInventory().setItem(slot, VisibilityItem.createShowItem(plugin.getConfigUtil()));
                } else {
                    showAllPlayers(player);
                    hiddenPlayers.remove(player);
                    player.getInventory().setItem(slot, VisibilityItem.createHideItem(plugin.getConfigUtil()));
                }
            });
        });
    }

    private void hideAllPlayers(Player player) {
        player.getServer().getOnlinePlayers().forEach(other -> player.hidePlayer(plugin, other));
        String hideMessage = plugin.getConfigUtil().getMessage("messages.hide-item.players-hidden");
        if(!hideMessage.isEmpty()) {
            player.sendMessage(hideMessage);
        }
    }

    private void showAllPlayers(Player player) {
        player.getServer().getOnlinePlayers().forEach(other -> player.showPlayer(plugin, other));
        String showMessage = plugin.getConfigUtil().getMessage("messages.hide-item.players-visible");
        if(!showMessage.isEmpty()) {
            player.sendMessage(showMessage);
        }
    }

    public boolean isPlayerHidden(Player player) {
        return hiddenPlayers.contains(player);
    }
}