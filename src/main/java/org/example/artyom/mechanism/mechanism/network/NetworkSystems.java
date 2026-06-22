package org.example.artyom.mechanism.mechanism.network;

import org.bukkit.Location;
import org.bukkit.entity.Player;

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
    public NetworkManager addNetworkManager() {
        UUID networkId = UUID.randomUUID();
        NetworkManager networkManager = new NetworkManager(networkId);
        networks.put(networkId, networkManager);
        return networkManager;
    }

    /**
     * Удалить сеть по id
     */
    public void removeNetworkManager(UUID networkId) {
        networks.remove(networkId);
    }

    /**
     * Удалить сеть
     */
    public void removeNetworkManager(NetworkManager networkManager) {
        networks.remove(networkManager.getNetworkId());
    }

    /**
     * Объединение множества сетей с добавлением узла
     */
    public void mergeNetworksAndAddElement(INetworkElement newElement,
                                            Set<NetworkManager> networks,
                                            Player player) {
        if (networks.size() == 1) {
            NetworkManager network = networks.iterator().next();
            network.addElement(newElement);
            player.sendMessage("Добавлено в существующую сеть!" + newElement.getNetworkId());
        }
        else if (networks.size() > 1) {
            // Выбираем основную сеть (самую большую)
            NetworkManager primaryNetwork = networks.stream()
                    .max(Comparator.comparingInt(n -> n.getElements().size()))
                    .orElse(networks.iterator().next());

            // Переносим элементы из других сетей
            for (NetworkManager secondaryNetwork : networks) {
                if (secondaryNetwork != primaryNetwork) {
                    for (INetworkElement element : secondaryNetwork.getElements()) {
                        primaryNetwork.addElement(element); // Связи добавляем только новые! старые остаются
                    }
                    removeNetworkManager(secondaryNetwork);
                }
            }

            // Добавляем новый элемент
            primaryNetwork.addElement(newElement);

            player.sendMessage(String.format(
                    "✓ Объединено %d сетей! Всего элементов: %d",
                    networks.size(),
                    primaryNetwork.getElements().size()
            ));
        }
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
