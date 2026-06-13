package org.example.artyom.mechanism.utils;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.example.artyom.mechanism.Mechanism;


import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class ItemsUtil {
   // private static final Mechanism plugin = Mechanism.getInstance();

//    public enum ItemKey {
//        INVENTORY_ITEM("inventory_item");
//
//        private final NamespacedKey key;
//
//        ItemKey(String key) {
//            this.key = new NamespacedKey(plugin, key);
//        }
//
//        public NamespacedKey getKey() {
//            return key;
//        }
//    }

    public static ItemStack create(Material material, int amount, String displayName, @Nullable List<String> lore) {
        return createItem(item -> {
            item.setType(material);
            item.setAmount(amount);

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(displayName);

                if (lore != null && !lore.isEmpty()) {
                    meta.setLore(lore);
                }

                item.setItemMeta(meta);
            }
        });
    }

    public static ItemStack create(Material material, int amount, String displayName) {
        return create(material, amount, displayName, null);
    }

    //Consumer получает ничего, отдает тип указанный
    private static ItemStack createItem(Consumer<ItemStack> itemConfigurator) {
        ItemStack item = new ItemStack(Material.AIR);
        itemConfigurator.accept(item);
        return item;
    }
}
