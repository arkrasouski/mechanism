package org.example.artyom.mechanism.utils;

import org.bukkit.Location;

public class ChunkUtil {
    public static int getChunkX(int blockX) {
        return blockX >> 4; // blockX / 16 То есть координаты блоков просто переводятся из блоков в чанки делением на 16. (номер чанка)
    }

    public static int getChunkZ(int blockZ) {
        return blockZ >> 4; // blockZ / 16
    }

    public static int getChunkX(Location location) {
        return getChunkX(location.getBlockX());
    }

    public static int getChunkZ(Location location) {
        return getChunkZ(location.getBlockZ());
    }
}

