package org.example.artyom.mechanism.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.block.TileState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;

public class MechanismStorageUtil {

    /**
    * Восстанавливает из PDC предметы
     */
    public static void loadItems(TileState tile, Inventory inv, NamespacedKey key) {
        String encoded = tile.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (encoded == null || encoded.isBlank()) return;

        try {
            var items = ItemStackArrayCodec.fromBase64(encoded);
            // подгоняем под размер GUI
            ItemStack[] trimmed = new ItemStack[inv.getSize()];
            System.arraycopy(items, 0, trimmed, 0, Math.min(items.length, trimmed.length));
            inv.setContents(trimmed);
        } catch (IOException ex) {
            // если данные битые — можно очистить, чтобы не падало постоянно
            tile.getPersistentDataContainer().remove(key);
            tile.update(); // применить remove [web:69]
        }

    }

    /**
     * Сохраняет в PDC предметы
     */
    public static void saveItems(TileState tile, Inventory inv, NamespacedKey key) {
        String encoded = ItemStackArrayCodec.toBase64(inv.getContents());
        tile.getPersistentDataContainer()
                .set(key, PersistentDataType.STRING, encoded); // [web:34]

        // важно: применить изменения TileState к реальному блоку
        tile.update(); // без этого PDC останется только в snapshot [web:33]
    }
}
