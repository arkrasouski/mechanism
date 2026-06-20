package org.example.artyom.mechanism.items;

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

public class BaseItem {
    private final MechanismType mechanismType;
    private final NamespacedKey mechanismKey;

    public BaseItem(Mechanism plugin, MechanismType mechanismType) {
        this.mechanismType = mechanismType;
        mechanismKey = new NamespacedKey(plugin, mechanismType.name() + "_item");
    }

    public ItemStack createItem(int amount) {
        ItemStack item = ItemsUtil.create(mechanismType.getMaterial(),
                amount,
                mechanismType.getDisplayName(),
                List.of(mechanismType.getGuiLore())
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
        return pdc.has(new NamespacedKey(plugin, mechanismType.name() + "_item"), PersistentDataType.BOOLEAN);
    }
}
