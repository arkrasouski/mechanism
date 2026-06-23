package org.example.artyom.mechanism.utils;

public class ChunkUtil {
    public static int getChunkX(int blockX) {
        return blockX >> 4; // blockX / 16
    }

    public static int getChunkZ(int blockZ) {
        return blockZ >> 4; // blockZ / 16
    }

    public static int getChunkX(org.bukkit.Location location) {
        return getChunkX(location.getBlockX());
    }

    public static int getChunkZ(org.bukkit.Location location) {
        return getChunkZ(location.getBlockZ());
    }
}

