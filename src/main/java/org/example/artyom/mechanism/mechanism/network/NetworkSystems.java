package org.example.artyom.mechanism.mechanism.network;

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
    public void mergeNetworksAndAddElement(NetworkElement newElement,
                                            Set<NetworkManager> networks,
                                            Player player) {
        if (networks.size() == 1) {
            NetworkManager network = networks.iterator().next();
            network.addElement(newElement);
            player.sendMessage("Добавлено в существующую сеть!");
        }
        else if (networks.size() > 1) {
            // Выбираем основную сеть (самую большую)
            NetworkManager primaryNetwork = networks.stream()
                    .max(Comparator.comparingInt(n -> n.getElements().size()))
                    .orElse(networks.iterator().next());

            // Переносим элементы из других сетей
            for (NetworkManager secondaryNetwork : networks) {
                if (secondaryNetwork != primaryNetwork) {
                    for (NetworkElement element : secondaryNetwork.getElements()) {
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

}
