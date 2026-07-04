package org.example.artyom.mechanism.listeners;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.example.artyom.mechanism.database.MechanismRepository;
import org.example.artyom.mechanism.database.TransactionManager;
import org.example.artyom.mechanism.mechanism.MechanismType;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;
import org.example.artyom.mechanism.mechanism.network.NetworkManager;
import org.example.artyom.mechanism.mechanism.network.NetworkSystems;
import org.example.artyom.mechanism.utils.LogUtil;

import java.sql.SQLException;
import java.util.*;

public class ChunkListener implements Listener {

    private final TransactionManager transactionManager;
    private final MechanismRepository mechanismRepository;
    private final NetworkSystems networkSystems;

    public ChunkListener(TransactionManager transactionManager, MechanismRepository mechanismRepository) {
        this.transactionManager = transactionManager;
        this.mechanismRepository = mechanismRepository;
        this.networkSystems = new NetworkSystems();
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        World world = chunk.getWorld();

        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();


        try {
            List<INetworkElement> mechanisms = transactionManager.execute(connection -> mechanismRepository.findByChunk(
                    connection,
                    world,
                    chunkX,
                    chunkZ
            ));
            //Получаю уникальные сети механизмов
//            Set<NetworkMechInfo> networkLocIds = mechanisms.stream()
//                    .filter(m -> m.getNetworkId() != null && m.getLocation() != null && m.getLocation().getWorld() != null)
//                    .map(m -> new NetworkMechInfo(
//                            m.getNetworkId(),
//                            m.getLocation()
//                    ))
//                    .collect(Collectors.toSet());
//
//            for (NetworkMechInfo networkLoc : networkLocIds) {
//                NetworkManager networkManager = new NetworkManager(networkLoc.networkId(), networkLoc.location().getWorld());
//                networkSystems.addNetworkManager(networkManager);
//            }

            int restoredCount = 0;

            for (INetworkElement mechanism : mechanisms) {
                restoredCount++;
                NetworkManager networkManager;
                if(networkSystems.hasNetwork(mechanism.getNetworkId())) {
                    networkManager = networkSystems.getNetworkManager(mechanism.getNetworkId());
                } else {
                    networkManager = new NetworkManager(mechanism.getNetworkId(), mechanism.getLocation().getWorld());
                    networkSystems.addNetworkManager(networkManager);
                }
                networkManager.addElement(mechanism);
                mechanism.getMechanismType().getMechanismManager().registerMechanism(mechanism, mechanism.getLocation());
            }
            LogUtil.info("Restored " + restoredCount + " mechanism from database");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        World world = chunk.getWorld();

        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();
        // Проверяем, есть ли вообще механизмы в этом чанке
        if (!hasMechanismsInChunk(event.getChunk())) {
            return;
        }
        try {
            List<INetworkElement> mechanisms = transactionManager.execute(connection -> {
                List<INetworkElement> mechs = mechanismRepository.findByChunk(connection, world, chunkX, chunkZ);

                for (INetworkElement mechanism : mechs) {
                    mechanismRepository.updateMechanismState(connection, mechanism);
                }

                return mechs;
            });
            int unloadCount = 0;

            for (INetworkElement mechanism : mechanisms) {
                //Удалить у соседей
                Set<INetworkElement> neighbors = new HashSet<>(mechanism.getConnections());

                for (INetworkElement neighbor : neighbors) {
                    neighbor.removeConnection(mechanism);
                }
                //Очистить соседей механзма
                mechanism.getConnections().clear();

                //Удалить механизм
                mechanism.getMechanismType().getMechanismManager().deleteMechanism(mechanism.getLocation());

                NetworkManager networkManager = networkSystems.getNetworkManager(mechanism.getNetworkId());
                if(networkManager != null) {
                    networkManager.removeElement(mechanism.getLocation());
                    if(networkManager.getElements().isEmpty()) {
                        networkSystems.removeNetworkManager(networkManager);
                    }
                }
                unloadCount++;
            }
            LogUtil.info("Unloaded " + unloadCount + " mechanism from database");

        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean hasMechanismsInChunk(Chunk chunk) {
        boolean hasMechanism = false;
        for(MechanismType mechanismType : MechanismType.values()){
            if (mechanismType.getMechanismManager().getMechanismsInChunk(chunk)){
            hasMechanism = true;}
        }
        return hasMechanism;
    }
}
