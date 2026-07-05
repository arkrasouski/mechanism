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
import org.example.artyom.mechanism.utils.ChunkUtil;


public class ChunkListener implements Listener {

    private final TransactionManager transactionManager;
    private final MechanismRepository mechanismRepository;
    private final NetworkSystems networkSystems;

    public ChunkListener(TransactionManager transactionManager, MechanismRepository mechanismRepository, NetworkSystems networkSystems) {
        this.transactionManager = transactionManager;
        this.mechanismRepository = mechanismRepository;
        this.networkSystems = networkSystems;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        World world = chunk.getWorld();

        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        ChunkUtil.restoreMechanismsByChunk(transactionManager, mechanismRepository, networkSystems, world, chunkX, chunkZ);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        World world = chunk.getWorld();

        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        ChunkUtil.unloadMechanismsByChunk(transactionManager, mechanismRepository, networkSystems, world, chunkX, chunkZ);
    }


}
