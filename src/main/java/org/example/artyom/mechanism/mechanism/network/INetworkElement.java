package org.example.artyom.mechanism.mechanism.network;

import org.bukkit.Location;

import java.util.Set;


public interface INetworkElement {
    Location getLocation();              // позиция в мире
    Set<INetworkElement> getConnections(); //получить соседей текущего элемента
    void addConnection(INetworkElement connection);
    void removeConnection(INetworkElement connection);
}
