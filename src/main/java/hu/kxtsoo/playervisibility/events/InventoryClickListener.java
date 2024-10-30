package hu.kxtsoo.playervisibility.events;

import hu.kxtsoo.playervisibility.PlayerVisibility;
import hu.kxtsoo.playervisibility.model.VisibilityItem;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class InventoryClickListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        ItemStack hideItem = VisibilityItem.createHideItem(PlayerVisibility.getInstance().getConfigUtil());
        ItemStack showItem = VisibilityItem.createShowItem(PlayerVisibility.getInstance().getConfigUtil());
        ItemStack noPermissionItem = VisibilityItem.createNoPermissionItem(PlayerVisibility.getInstance().getConfigUtil());

        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) return;

        if (isInPlayerOrCraftingInventory(event) && clickedItem != null &&
                (clickedItem.isSimilar(hideItem) || clickedItem.isSimilar(showItem) || clickedItem.isSimilar(noPermissionItem))) {
            event.setCancelled(true);
        }

        if (event.getClick().equals(ClickType.NUMBER_KEY)) {
            ItemStack hotbarItem = player.getInventory().getItem(event.getHotbarButton());
            if (hotbarItem != null &&
                    (hotbarItem.isSimilar(hideItem) || hotbarItem.isSimilar(showItem) || hotbarItem.isSimilar(noPermissionItem))) {
                event.setCancelled(true);
            }
        }
    }

    private boolean isInPlayerOrCraftingInventory(InventoryClickEvent event) {
        return event.getClickedInventory().getType() == InventoryType.CRAFTING ||
                event.getClickedInventory().getType() == InventoryType.PLAYER;
    }
}