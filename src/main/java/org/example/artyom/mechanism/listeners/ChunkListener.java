package org.example.artyom.mechanism.listeners;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.example.artyom.mechanism.database.MechanismRepository;
import org.example.artyom.mechanism.database.TransactionManager;
import org.example.artyom.mechanism.mechanism.network.NetworkSystems;
import org.example.artyom.mechanism.records.ChunkKey;
import org.example.artyom.mechanism.utils.ChunkUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ChunkListener implements Listener {

    private final TransactionManager transactionManager;
    private final MechanismRepository mechanismRepository;
    private final NetworkSystems networkSystems;
    private final Set<ChunkKey> processedChunks;


    public ChunkListener(TransactionManager transactionManager,
                         MechanismRepository mechanismRepository,
                         NetworkSystems networkSystems,
                         Set<ChunkKey> processedChunks) {
        this.transactionManager = transactionManager;
        this.mechanismRepository = mechanismRepository;
        this.networkSystems = networkSystems;
        this.processedChunks = processedChunks;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        World world = chunk.getWorld();

        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        ChunkKey key = ChunkKey.of(chunk);
        if (!processedChunks.add(key)) { //add() возвращает false, если такой ключ уже есть, поэтому это одновременно и проверка, и добавление.
            return;
        }

        ChunkUtil.restoreMechanismsByChunk(transactionManager, mechanismRepository, networkSystems, world, chunkX, chunkZ);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        World world = chunk.getWorld();

        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        ChunkKey key = ChunkKey.of(chunk);

        try {
            ChunkUtil.unloadMechanismsByChunk(transactionManager, mechanismRepository, networkSystems, world, chunkX, chunkZ);
        } finally {
            processedChunks.remove(key);
        }
    }


}
