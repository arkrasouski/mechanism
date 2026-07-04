package org.example.artyom.mechanism.mechanism.network;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.example.artyom.mechanism.database.NetworkRepository;
import org.example.artyom.mechanism.mechanism.MechanismType;
import org.example.artyom.mechanism.mechanism.base.Mech;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkSystems {
    private final Map<UUID, NetworkManager> networks = new ConcurrentHashMap<>();
    private final NetworkRepository networkRepository;

    public NetworkSystems(NetworkRepository networkRepository) {
        this.networkRepository = networkRepository;
    }

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
     * Объединение множества сетей с добавлением узла
     */
    public void mergeNetworksAndAddElement(INetworkElement newElement,
                                            Set<NetworkManager> networks,
                                            Player player) {
//        if (networks.size() == 1) { // Добавляем в единственную сеть
//            NetworkManager network = networks.iterator().next();
//            network.addElement(newElement.getMechanismType(), newElement);
//            player.sendMessage("Добавлено в существующую сеть!" + newElement.getNetworkId());
//        }
//        else if (networks.size() > 1) {
//            // Выбираем основную сеть (самую большую)
//            NetworkManager primaryNetwork = networks.stream()
//                    .max(Comparator.comparingInt(n -> n.getElements().size()))
//                    .orElse(networks.iterator().next());
//
//            // Переносим элементы из других сетей
//            for (NetworkManager secondaryNetwork : networks) {
//                if (secondaryNetwork != primaryNetwork) {
//                    for (INetworkElement element : secondaryNetwork.getElements()) {
//                        //добавляю новые сети в бд и в память
//                        primaryNetwork.addElement(element.getMechanismType(), element); // Связи добавляем только новые! старые остаются
//                    }
//                    //Удаляю сеть в памяти
//                    removeNetworkManager(secondaryNetwork);
//                    //Удаляю из бд все механизмы старой сети
//                    for(MechanismType mechanismType : MechanismType.values()) {
//                        mechanismType.removeFromPreviousNetwork(secondaryNetwork.getNetworkId());
//                    }
//                    //Удаляю сеть из бд
//                    NetworkRepository.deleteNetwork(secondaryNetwork.getNetworkId().toString());
//                }
//            }
//
//            // Добавляем новый элемент
//            primaryNetwork.addElement(newElement.getMechanismType(), newElement);
//
//            player.sendMessage(String.format(
//                    "✓ Объединено %d сетей! Всего элементов: %d",
//                    networks.size(),
//                    primaryNetwork.getElements().size()
//            ));
//        }
        if (networks.isEmpty()) return;

        NetworkManager primaryNetwork = networks.stream()
                .max(Comparator.comparingInt(n -> n.getElements().size()))
                .orElseThrow();

        List<NetworkManager> secondaryNetworks = networks.stream()
                .filter(n -> n != primaryNetwork)
                .toList();

        List<INetworkElement> movedElements = new ArrayList<>();
        for (NetworkManager secondary : secondaryNetworks) {
            movedElements.addAll(secondary.getElements());
        }

        primaryNetwork.addElement(newElement);
        for (INetworkElement element : movedElements) {
            primaryNetwork.addElement(element);
        }

        try {
            networkRepository.mergeNetworks(
                    primaryNetwork.getNetworkId(),
                    secondaryNetworks.stream().map(NetworkManager::getNetworkId).toList(),
                    newElement
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for (NetworkManager secondary : secondaryNetworks) {
            removeNetworkManager(secondary);
        }

        player.sendMessage("✓ Объединено " + networks.size() + " сетей");
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
