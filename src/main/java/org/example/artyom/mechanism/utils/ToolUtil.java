package org.example.artyom.mechanism.utils;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ToolUtil {
    public static boolean isPickaxe(ItemStack item) {
        if (item == null) return false;
        if (item.getType() == Material.AIR) return false;

        return switch (item.getType()) {
            case WOODEN_PICKAXE,
                 STONE_PICKAXE,
                 IRON_PICKAXE,
                 GOLDEN_PICKAXE,
                 DIAMOND_PICKAXE,
                 NETHERITE_PICKAXE -> true;
            default -> false;
        };
    }
    public static boolean canBreakWithTool(Player player, ItemStack item) {
        if (player.getGameMode() == GameMode.CREATIVE) return true; // [web:385]
        return isPickaxe(item);
    }
}
