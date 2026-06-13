package org.example.artyom.mechanism.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

public final class ItemStackArrayCodec {

    private ItemStackArrayCodec() {}

    public static String toBase64(ItemStack[] items) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (BukkitObjectOutputStream dataOut = new BukkitObjectOutputStream(out)) {
                dataOut.writeInt(items.length);
                for (ItemStack item : items) {
                    dataOut.writeObject(item == null ? null : item.serialize()); // [web:162]
                }
            }
            return Base64Coder.encodeLines(out.toByteArray()); // [web:165]
        } catch (Exception e) {
            throw new IllegalStateException("Unable to serialize ItemStack[]", e);
        }
    }

    public static ItemStack[] fromBase64(String base64) throws IOException {
        try {
            byte[] bytes = Base64Coder.decodeLines(base64); // [web:165]
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);

            try (BukkitObjectInputStream dataIn = new BukkitObjectInputStream(in)) {
                int size = dataIn.readInt();
                ItemStack[] items = new ItemStack[size];

                for (int i = 0; i < size; i++) {
                    Map<String, Object> stackBytes = (Map<String, Object>) dataIn.readObject();
                    items[i] = (stackBytes == null) ? null : ItemStack.deserialize(stackBytes); // [web:162]
                }
                return items;
            }
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to deserialize ItemStack[]", e);
        }
    }
}