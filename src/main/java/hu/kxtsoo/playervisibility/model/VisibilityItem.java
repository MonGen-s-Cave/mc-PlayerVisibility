package hu.kxtsoo.playervisibility.model;

import hu.kxtsoo.playervisibility.utils.ChatUtil;
import hu.kxtsoo.playervisibility.utils.ConfigUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class VisibilityItem {

    public static ItemStack createHideItem(ConfigUtil configUtil) {
        return createItem(configUtil, "items.hide");
    }

    public static ItemStack createShowItem(ConfigUtil configUtil) {
        return createItem(configUtil, "items.show");
    }

    public static ItemStack createNoPermissionItem(ConfigUtil configUtil) {
        return createItem(configUtil, "items.no-permission");
    }

    private static ItemStack createItem(ConfigUtil configUtil, String path) {
        String materialName = configUtil.getConfig().getString(path + ".material", "STONE");
        Material material = Material.getMaterial(materialName.toUpperCase());
        if (material == null) material = Material.STONE;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatUtil.colorizeHex(configUtil.getConfig().getString(path + ".name", "§fItem")));
            meta.setLore(ChatUtil.colorizeHex(configUtil.getConfig().getStringList(path + ".lore")));

            if (configUtil.getConfig().contains(path + ".custom-model-data")) {
                meta.setCustomModelData(configUtil.getConfig().getInt(path + ".custom-model-data"));
            }

            item.setItemMeta(meta);
        }
        return item;
    }
}