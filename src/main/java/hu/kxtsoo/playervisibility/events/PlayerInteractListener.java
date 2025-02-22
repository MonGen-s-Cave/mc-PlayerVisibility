package hu.kxtsoo.playervisibility.events;

import hu.kxtsoo.playervisibility.PlayerVisibility;
import hu.kxtsoo.playervisibility.database.DatabaseManager;
import hu.kxtsoo.playervisibility.manager.CooldownManager;
import hu.kxtsoo.playervisibility.manager.SchedulerManager;
import hu.kxtsoo.playervisibility.model.VisibilityItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PlayerInteractListener implements Listener {

    private final PlayerVisibility plugin;
    private final CooldownManager cooldownManager;

    public PlayerInteractListener(PlayerVisibility plugin) {
        this.plugin = plugin;
        this.cooldownManager = new CooldownManager(plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        int configSlot = plugin.getConfigUtil().getConfig().getInt("settings.slot", 0);
        Set<Integer> allowedSlots = IntStream.of(configSlot).boxed().collect(Collectors.toSet());

        SchedulerManager.run(() -> {
            for (int i = 0; i < player.getInventory().getSize(); i++) {
                if (!allowedSlots.contains(i)) {
                    ItemStack item = player.getInventory().getItem(i);
                    if (item != null && (item.isSimilar(VisibilityItem.createHideItem(plugin.getConfigUtil())) ||
                            item.isSimilar(VisibilityItem.createShowItem(plugin.getConfigUtil())))) {
                        player.getInventory().setItem(i, null);
                    }
                }
            }
        });

        if (player.hasPermission("playervisibility.item.use")) {
            int slot = plugin.getConfigUtil().getConfig().getInt("settings.slot", 0);

            if (player.getInventory().getItemInMainHand().isSimilar(VisibilityItem.createHideItem(plugin.getConfigUtil())) ||
                    player.getInventory().getItemInMainHand().isSimilar(VisibilityItem.createShowItem(plugin.getConfigUtil()))) {

                if (cooldownManager.hasCooldown(player)) {
                    if (cooldownManager.shouldSendCooldownMessage(player)) {
                        int remainingCooldown = cooldownManager.getRemainingCooldown(player);

                        String cooldownMessage = plugin.getConfigUtil().getMessage("messages.hide-item.cooldown").replace("%cooldown%", String.valueOf(remainingCooldown));
                        if (!cooldownMessage.isEmpty()) {
                            player.sendMessage(cooldownMessage);
                        }
                    }
                    event.setCancelled(true);
                    return;
                }

                SchedulerManager.run(() -> {
                    plugin.getVisibilityManager().togglePlayerVisibility(player);
                    cooldownManager.startCooldown(player);

                    boolean isHidden = plugin.getVisibilityManager().isPlayerHidden(player);

                    SchedulerManager.runAsync(() -> {
                        try {
                            DatabaseManager.setPlayersHidden(player.getUniqueId(), isHidden);
                        } catch (Exception e) {
                            plugin.getLogger().severe("Failed to save player visibility: " + e.getMessage());
                        }
                    });

                    if (isHidden) {
                        player.getInventory().setItem(slot, VisibilityItem.createShowItem(plugin.getConfigUtil()));
                    } else {
                        player.getInventory().setItem(slot, VisibilityItem.createHideItem(plugin.getConfigUtil()));
                    }
                });

                event.setCancelled(true);
            }
        } else if (player.getInventory().getItemInMainHand().isSimilar(VisibilityItem.createNoPermissionItem(plugin.getConfigUtil()))) {
            player.sendMessage(plugin.getConfigUtil().getMessage("messages.hide-item.no-permission"));
            event.setCancelled(true);
        }
    }
}