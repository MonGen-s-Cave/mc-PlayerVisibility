package hu.kxtsoo.playervisibility.events;

import hu.kxtsoo.playervisibility.PlayerVisibility;
import hu.kxtsoo.playervisibility.model.VisibilityItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerSwapHandItemsListener implements Listener {

    private final ItemStack hideItem;
    private final ItemStack showItem;
    private final ItemStack noPermissionItem;

    public PlayerSwapHandItemsListener() {
        this.hideItem = VisibilityItem.createHideItem(PlayerVisibility.getInstance().getConfigUtil());
        this.showItem = VisibilityItem.createShowItem(PlayerVisibility.getInstance().getConfigUtil());
        this.noPermissionItem = VisibilityItem.createNoPermissionItem(PlayerVisibility.getInstance().getConfigUtil());
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        ItemStack offHandItem = event.getOffHandItem();

        if (offHandItem != null &&
                (offHandItem.isSimilar(hideItem) || offHandItem.isSimilar(showItem) || offHandItem.isSimilar(noPermissionItem))) {
            event.setCancelled(true);
        }
    }
}
