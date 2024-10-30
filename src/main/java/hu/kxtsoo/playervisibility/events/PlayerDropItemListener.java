package hu.kxtsoo.playervisibility.events;

import hu.kxtsoo.playervisibility.PlayerVisibility;
import hu.kxtsoo.playervisibility.model.VisibilityItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerDropItemListener implements Listener {

    private final ItemStack hideItem;
    private final ItemStack showItem;
    private final ItemStack noPermissionItem;

    public PlayerDropItemListener() {
        this.hideItem = VisibilityItem.createHideItem(PlayerVisibility.getInstance().getConfigUtil());
        this.showItem = VisibilityItem.createShowItem(PlayerVisibility.getInstance().getConfigUtil());
        this.noPermissionItem = VisibilityItem.createNoPermissionItem(PlayerVisibility.getInstance().getConfigUtil());
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack droppedItem = event.getItemDrop().getItemStack();

        if (droppedItem.isSimilar(hideItem) || droppedItem.isSimilar(showItem) || droppedItem.isSimilar(noPermissionItem)) {
            event.setCancelled(true);
        }
    }
}
