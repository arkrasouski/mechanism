package org.example.artyom.mechanism.mechanism.network;

import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;

public abstract class NetworkElement implements INetworkElement {

    //Местоположение
    private final Location location;
    //Сетевые соединения
    private final Set<INetworkElement> connections = new HashSet<>();

    public NetworkElement(Location location) {
        this.location = location;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public Set<INetworkElement> getConnections() {
        return connections;
    }

    @Override
    public void addConnection(INetworkElement element) {
        connections.add(element);
    }

    @Override
    public void removeConnection(INetworkElement element) {
        connections.remove(element);
    }
}
