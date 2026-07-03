package org.example.artyom.mechanism.mechanism.network;

import org.bukkit.Location;
import org.example.artyom.mechanism.mechanism.MechanismType;
import org.example.artyom.mechanism.utils.BlockUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkManager {
    private final UUID networkId;
    private final Map<Location, INetworkElement> elements = new ConcurrentHashMap<>();

    public NetworkManager(UUID networkId) {
        this.networkId = networkId;
    }

    /**
     * Добавить элемент в сеть (без бд)
     */
    public void addElement(INetworkElement newElement) {
        Location loc = newElement.getLocation();

        // Если элемент уже есть, не добавляем
        if (elements.containsKey(loc)) return;

        // Добавляем элемент
        elements.put(loc, newElement);

        // Ищем соседей для нового элемента
        Set<INetworkElement> neighbors = findNeighborsInNetwork(loc);

        // Устанавливаем двусторонние связи
        for (INetworkElement neighbor : neighbors) {

            newElement.addConnection(neighbor);
            neighbor.addConnection(newElement);
        }

        newElement.setNetworkId(networkId);
    }



    /**
     * Удалить элемент из сети
     */
    public void removeElement(Location loc) {
        INetworkElement element = elements.remove(loc);
        if (element != null) {
            //Удалить связи у соседей
            for(INetworkElement connection : element.getConnections()){
                connection.removeConnection(element);
            }
        }
    }

    /**
     * Получить элемент сети
     */
    public INetworkElement getElement(Location loc) {
        return elements.get(loc);
    }

    /**
     * Получить все элементы сети
     */
    public Collection<INetworkElement> getElements() {
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
    private Set<INetworkElement> findNeighborsInNetwork(Location loc) {
        Set<INetworkElement> neighbors = new HashSet<>();
        Location[] sides = BlockUtil.getSidesByLoc(loc);

        for (Location side : sides) {
            //текущая сеть содержит соседа - значит его добавляем
            INetworkElement elem = elements.get(side);
            if (elem != null) {
                neighbors.add(elem);
            }
        }
        return neighbors;
    }
}
