package org.example.artyom.mechanism.mechanism.network;

import org.bukkit.Location;

import java.util.Set;
import java.util.UUID;


public interface INetworkElement {
    Location getLocation();              // позиция в мире
    Set<INetworkElement> getConnections(); //получить соседей текущего элемента
    void addConnection(INetworkElement connection);
    void removeConnection(INetworkElement connection);
    void setNetworkId(UUID networkId);
    UUID getNetworkId();
}
