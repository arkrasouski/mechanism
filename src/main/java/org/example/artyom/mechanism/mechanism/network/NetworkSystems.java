package org.example.artyom.mechanism.mechanism.network;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
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
     * Найти сеть по id
     */
    public NetworkManager getNetworkManager(UUID uuid) {
        return networks.get(uuid);
    }

    /**
     * Добавить новую сеть
     */
    public NetworkManager addNetworkManager() {
        UUID networkId = UUID.randomUUID();
        NetworkManager networkManager = new NetworkManager(networkId);
        networks.put(networkId, networkManager);
        return networkManager;
    }

    /**
     * Удалить сеть
     */
    public void removeNetworkManager(UUID networkId) {
        networks.remove(networkId);
    }
}
