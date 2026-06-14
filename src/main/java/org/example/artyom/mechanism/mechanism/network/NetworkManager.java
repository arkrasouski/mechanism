package org.example.artyom.mechanism.mechanism.network;

import org.bukkit.Location;
import org.example.artyom.mechanism.utils.BlockUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkManager {
    private final UUID networkId;
    private final Map<Location, NetworkElement> elements = new ConcurrentHashMap<>();

    public NetworkManager(UUID networkId) {
        this.networkId = networkId;
    }

    /**
     * Добавить элемент в сеть
     */
    public void addElement(NetworkElement newElement) {
        Location loc = newElement.getLocation();

        // Если элемент уже есть, не добавляем
        if (elements.containsKey(loc)) return;

        // Добавляем элемент
        elements.put(loc, newElement);

        // Ищем соседей для нового элемента
        Set<NetworkElement> neighbors = findNeighborsInNetwork(loc);

        // Устанавливаем двусторонние связи
        for (NetworkElement neighbor : neighbors) {
            newElement.getConnections().add(neighbor);
            neighbor.getConnections().add(newElement);
        }
    }

    /**
     * Удалить элемент из сети
     */
    public void removeElement(Location loc) {
        NetworkElement element = elements.remove(loc);
        if (element != null) {
            //Удалить связи у соседей
            for(NetworkElement connection : element.getConnections()){
                connection.getConnections().remove(element);
            }
        }
    }

    /**
     * Получить элемент сети
     */
    public NetworkElement getElement(Location loc) {
        return elements.get(loc);
    }

    /**
     * Получить все элементы сети
     */
    public Collection<NetworkElement> getElements() {
        return elements.values();
    }

    /**
     * Получить id сети
     */
    public UUID getNetworkId() {
        return networkId;
    }

    /**
     * Найти всех соседей элемента в текущей сети
     */
    private Set<NetworkElement> findNeighborsInNetwork(Location loc) {
        Set<NetworkElement> neighbors = new HashSet<>();
        Location[] sides = BlockUtil.getSidesByLoc(loc);

        for (Location side : sides) {
            //текущая сеть содержит соседа - значит его добавляем
            NetworkElement elem = elements.get(side);
            if (elem != null) {
                neighbors.add(elem);
            }
        }
        return neighbors;
    }
}
