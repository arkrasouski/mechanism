package org.example.artyom.mechanism.mechanism.network;

import org.bukkit.Location;
import org.example.artyom.mechanism.utils.LogUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkSystems {
    private final Map<UUID, NetworkManager> networks = new ConcurrentHashMap<>();

    /**
     * Вернуть все сети
     */
    public Collection<NetworkManager> getNetworks() {
        return networks.values();
    }

    /**
     * Получить сеть по ID
     */
    public NetworkManager getNetworkManager(UUID networkId) {
        return networks.get(networkId);
    }

    /**
     * Есть ли сеть в системе
     */
    public boolean hasNetwork(UUID networkId) {
        return networks.containsKey(networkId);
    }

    /**
     * Найти сеть по локации
     */
    public NetworkManager getNetworkManager(Location location) {
        for(NetworkManager networkManager : getNetworks()) {
            if(networkManager.getElement(location) != null) {
                return networkManager;
            }
        }
        return null;
    }

    /**
     * Добавить новую сеть
     */
    public NetworkManager createDetachedNetwork(Location mechanismLoc) {
        UUID networkId = UUID.randomUUID();
        return new NetworkManager(networkId, mechanismLoc.getWorld());
    }

    /**
     * Добавить сеть из БД по id
     */
    public void addNetworkManager(NetworkManager networkManager) {
        networks.put(networkManager.getNetworkId(), networkManager);
    }

    /**
     * Удалить сеть по id
     */
    public void removeNetworkManager(UUID networkId) {
        networks.remove(networkId);
    }

    /**
     * Удалить сеть из сетей и бд
     */
    public void removeNetworkManager(NetworkManager networkManager) {
        // NetworkRepository.deleteNetwork(networkManager.getNetworkId().toString());
        networks.remove(networkManager.getNetworkId());
    }

    /**
     * BFS
     */
    public Set<INetworkElement> collectComponent(INetworkElement start) {
        Set<INetworkElement> visited = new HashSet<>();
        Queue<INetworkElement> queue = new ArrayDeque<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            INetworkElement current = queue.poll();
            for (INetworkElement neighbor : current.getConnections()) {
                if (visited.add(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }

        return visited;
    }

}
