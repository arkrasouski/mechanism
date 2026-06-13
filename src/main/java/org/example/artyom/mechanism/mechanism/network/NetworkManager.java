package org.example.artyom.mechanism.mechanism.network;

import org.bukkit.Location;

import java.util.Map;
import java.util.UUID;
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
    public void addElement(NetworkElement element) {
        elements.put(element.getLocation(), element);
        //TODO: пересмотреть связи
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
}
