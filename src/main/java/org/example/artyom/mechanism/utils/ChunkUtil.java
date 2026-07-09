package org.example.artyom.mechanism.utils;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.example.artyom.mechanism.Mechanism;
import org.example.artyom.mechanism.database.MechanismRepository;
import org.example.artyom.mechanism.database.TransactionManager;
import org.example.artyom.mechanism.mechanism.MechanismManager;
import org.example.artyom.mechanism.mechanism.MechanismType;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;
import org.example.artyom.mechanism.mechanism.network.NetworkManager;
import org.example.artyom.mechanism.mechanism.network.NetworkSystems;

import java.sql.SQLException;
import java.util.*;

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

    /**
     * Функция загрузки из бд механизмов в память по чанкам
     * Используется в onChunkLoad и при запуске плагина
     */
    public static void restoreMechanismsByChunk(
            TransactionManager transactionManager,
            MechanismRepository mechanismRepository,
            NetworkSystems networkSystems,
            World world,
            int chunkX,
            int chunkZ
    ){
        try {
            List<INetworkElement> mechanisms = transactionManager.execute(connection -> mechanismRepository.findByChunk(
                    connection,
                    world,
                    chunkX,
                    chunkZ
            ));


            int restoredCount = 0;

            for (INetworkElement mechanism : mechanisms) {
                restoredCount++;
                NetworkManager networkManager;
                Map<UUID, List<INetworkElement>> mechanismMap = mechanism.getMechanismType().getMechsByNetwork();
                UUID networkId = mechanism.getNetworkId();
                if(networkSystems.hasNetwork(networkId)) {
                    networkManager = networkSystems.getNetworkManager(networkId);
                    mechanismMap.computeIfAbsent(networkId, id -> new ArrayList<>())
                            .add(mechanism);
                } else {
                    networkManager = new NetworkManager(networkId, mechanism.getLocation().getWorld());
                    networkSystems.addNetworkManager(networkManager);
                    mechanismMap.computeIfAbsent(networkId, id -> new ArrayList<>())
                            .add(mechanism);
                }
                networkManager.addElement(mechanism);
                mechanism.getMechanismType().getMechanismManager().registerMechanism(mechanism, mechanism.getLocation());
            }
            if (restoredCount > 0) LogUtil.info("Restored " + restoredCount + " mechanism from database");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Выгружаем в бд механизмы из памяти, подчищая память
     * Используется в onChunkUnload и onDisable
     */

    public static void unloadMechanismsByChunk(
            TransactionManager transactionManager,
            MechanismRepository mechanismRepository,
            NetworkSystems networkSystems,
            World world,
            int chunkX,
            int chunkZ
    ) {
        // Проверяем, есть ли вообще механизмы в этом чанке
        if (!hasMechanismsInChunk(world, chunkX, chunkZ)) {
            return;
        }
        for (MechanismType type : MechanismType.values()) {
            MechanismManager manager = type.getMechanismManager();
            Map<UUID, List<INetworkElement>> mechanismMap = type.getMechsByNetwork();
            List<INetworkElement> mechanismsToUnload = manager.getMechanismsByChunk(world, chunkX, chunkZ);
            try {
                transactionManager.execute(connection -> {
                    mechanismRepository.synchronizeMechanisms(connection, mechanismsToUnload);
                    return true;
                });
                int unloadCount = 0;

                for (INetworkElement mechanism : mechanismsToUnload) {
                    //Удалить у соседей
                    Set<INetworkElement> neighbors = new HashSet<>(mechanism.getConnections());

                    for (INetworkElement neighbor : neighbors) {
                        neighbor.removeConnection(mechanism);
                    }
                    //Очистить соседей механзма
                    mechanism.getConnections().clear();

                    //Удалить механизм
                    manager.deleteMechanism(mechanism.getLocation());

                    UUID networkId = mechanism.getNetworkId();
                    List<INetworkElement> elements = mechanismMap.get(networkId);
                    if (elements != null) {
                        elements.removeIf(e -> e.getLocation().equals(mechanism.getLocation()));

                        if (elements.isEmpty()) {
                            mechanismMap.remove(networkId);
                        }
                    }

                    NetworkManager networkManager = networkSystems.getNetworkManager(mechanism.getNetworkId());
                    if (networkManager != null) {
                        networkManager.removeElement(mechanism.getLocation());
                        if (networkManager.getElements().isEmpty()) {
                            networkSystems.removeNetworkManager(networkManager);
                        }
                    }
                    unloadCount++;
                }
                LogUtil.info("Unloaded " + unloadCount + " mechanism from database");

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static boolean hasMechanismsInChunk(World world, int chunkX, int chunkZ) {
        boolean hasMechanism = false;
        for(MechanismType mechanismType : MechanismType.values()){
            if (mechanismType.getMechanismManager().getMechanismsInChunk(world, chunkX, chunkZ)){
                hasMechanism = true;}
        }
        return hasMechanism;
    }
}

