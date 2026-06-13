package org.example.artyom.mechanism.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.example.artyom.mechanism.Mechanism;
import org.example.artyom.mechanism.mechanism.MechanismType;
import org.example.artyom.mechanism.utils.ItemsUtil;

import java.util.List;

public class GeneratorItem {
    private final Mechanism plugin;
    //private final MechanismType mechanismType = MechanismType.GENERATOR;
    private final NamespacedKey mechanismKey;

    public GeneratorItem(Mechanism plugin) {
        this.plugin = plugin;
        mechanismKey = new NamespacedKey(plugin, "generator_item");
    }

    public ItemStack createItem(int amount) {
        ItemStack item = ItemsUtil.create(Material.BARREL,
                amount,
                "Мега-генератор!" //сюда лор добавлять
        );
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(mechanismKey, PersistentDataType.BOOLEAN, true);

        // Добавляем эффект свечения (чтобы предмет выглядел особенным)
        meta.addEnchant(Enchantment.FORTUNE, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Проверяет, является ли предмет генератором
     */
    public static boolean isGeneratorItem(Mechanism plugin, ItemStack item, MechanismType mechanismType) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(new NamespacedKey(plugin, "generator_item"), PersistentDataType.BOOLEAN);
    }
}
