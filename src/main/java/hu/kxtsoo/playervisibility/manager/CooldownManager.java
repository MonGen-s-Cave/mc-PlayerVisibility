package hu.kxtsoo.playervisibility.manager;

import hu.kxtsoo.playervisibility.PlayerVisibility;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class CooldownManager {

    private final Map<Player, Long> cooldowns = new HashMap<>();
    private final Map<Player, Long> lastCooldownMessage = new HashMap<>();
    private final PlayerVisibility plugin;

    public CooldownManager(PlayerVisibility plugin) {
        this.plugin = plugin;
    }

    public boolean hasCooldown(Player player) {
        long currentTime = System.currentTimeMillis();
        return cooldowns.containsKey(player) && cooldowns.get(player) > currentTime;
    }

    public void startCooldown(Player player) {
        int cooldownSeconds = plugin.getConfigUtil().getConfig().getInt("options.cooldown", 3);
        long cooldownEnd = System.currentTimeMillis() + (cooldownSeconds * 1000L);
        cooldowns.put(player, cooldownEnd);
        lastCooldownMessage.put(player, 0L);
    }

    public int getRemainingCooldown(Player player) {
        if (hasCooldown(player)) {
            long remainingMillis = cooldowns.get(player) - System.currentTimeMillis();
            return (int) (remainingMillis / 1000);
        }
        return 0;
    }

    public boolean shouldSendCooldownMessage(Player player) {
        long currentTime = System.currentTimeMillis() / 1000;
        if (!lastCooldownMessage.containsKey(player) || currentTime - lastCooldownMessage.get(player) >= 1) {
            lastCooldownMessage.put(player, currentTime);
            return true;
        }
        return false;
    }

    public void clearCooldown(Player player) {
        cooldowns.remove(player);
        lastCooldownMessage.remove(player);
    }
}