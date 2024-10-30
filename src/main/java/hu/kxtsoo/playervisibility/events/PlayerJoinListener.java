package hu.kxtsoo.playervisibility.events;

import hu.kxtsoo.playervisibility.PlayerVisibility;
import hu.kxtsoo.playervisibility.manager.SchedulerManager;
import hu.kxtsoo.playervisibility.model.VisibilityItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final PlayerVisibility plugin;

    public PlayerJoinListener(PlayerVisibility plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        boolean giveOnJoin = plugin.getConfigUtil().getConfig().getBoolean("settings.give-on-join", true);
        int slot = plugin.getConfigUtil().getConfig().getInt("settings.slot", 0);

        if (giveOnJoin) {
            if (player.hasPermission("playervisibility.item.use")) {
                plugin.getVisibilityManager().loadPlayerVisibility(player);
            } else {
                SchedulerManager.run(() -> player.getInventory().setItem(slot, VisibilityItem.createNoPermissionItem(plugin.getConfigUtil())));
            }
        }

        Bukkit.getOnlinePlayers().forEach(existingPlayer -> {
            if (plugin.getVisibilityManager().isPlayerHidden(existingPlayer)) {
                SchedulerManager.run(() -> existingPlayer.hidePlayer(plugin, player));
            }
        });

    }
}
