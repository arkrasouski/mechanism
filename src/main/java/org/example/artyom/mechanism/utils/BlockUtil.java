package org.example.artyom.mechanism.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.sql.ResultSet;
import java.sql.SQLException;


public class BlockUtil {
    public static boolean isReplaceableBlock(Block block) {
        Material type = block.getType();

        // Список заменяемых блоков (можно дополнить)
        return type == Material.AIR ||
                type == Material.CAVE_AIR ||
                type == Material.VOID_AIR ||
                type == Material.WATER ||
                type == Material.LAVA ||
                type == Material.SHORT_GRASS ||
                type == Material.TALL_GRASS ||
                type == Material.FERN ||
                type == Material.LARGE_FERN ||
                type == Material.DEAD_BUSH ||
                type == Material.VINE ||
                type == Material.SNOW ||
                type == Material.SNOW_BLOCK ||
                type.name().contains("FLOWER") ||
                type.name().contains("MUSHROOM") ||
                type.name().contains("SAPLING") ||
                type.name().endsWith("_CARPET") ||
                type.name().endsWith("_PLANT") ||
                type.name().contains("TORCH") ||
                type == Material.REDSTONE_WIRE ||
                type == Material.TRIPWIRE ||
                type == Material.LEVER ||
                type == Material.STONE_BUTTON ||
                type == Material.OAK_BUTTON ||
                type == Material.REPEATER ||
                type == Material.COMPARATOR;
    }

    public static final BlockFace[] FACES = {
            BlockFace.NORTH, BlockFace.SOUTH,
            BlockFace.EAST, BlockFace.WEST,
            BlockFace.UP, BlockFace.DOWN
    };

    /**
     * Возвращает всех соседей искомой локации
     */
    public static Location[] getSidesByLoc(Location loc){
        // 6 сторон куба
        return new Location[]{
                loc.clone().add(0, 1, 0),   // вверх
                loc.clone().add(0, -1, 0),  // вниз
                loc.clone().add(1, 0, 0),   // восток
                loc.clone().add(-1, 0, 0),  // запад
                loc.clone().add(0, 0, 1),   // юг
                loc.clone().add(0, 0, -1)   // север
        };
    }

    public static Location extractLocation(ResultSet rs) throws SQLException {
        return new Location(
                Bukkit.getServer().getWorld(rs.getString("world_name")),
                rs.getInt("x"),
                rs.getInt("y"),
                rs.getInt("z")
        );
    }


//    public static <T extends BaseMechanism, U extends BaseMechanismManager<T>> boolean validateLocation(Location location, U manager) {
//        if (location == null || location.getWorld() == null) return false;
//        Block block = location.getBlock();
//        // Проверяем, что блок пустой
//        if (block.getType() != Material.AIR) return false;
//
//        // Проверяем, что под блоком есть твердый блок
//        Block below = block.getRelative(BlockFace.DOWN);
//        if (below.isEmpty() || !below.getType().isSolid()) return false;
//        // Проверяем, что здесь еще нет генератора
//        return !manager.hasMechanism(location);
//    }
}
