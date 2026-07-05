package org.example.artyom.mechanism.records;

import org.bukkit.Chunk;
import org.bukkit.World;

public record ChunkKey(String world, int x, int z) {
    public static ChunkKey of(Chunk chunk) {
        return new ChunkKey(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public static ChunkKey of(World world, int x, int z) {
        return new ChunkKey(world.getName(), x, z);
    }
}
