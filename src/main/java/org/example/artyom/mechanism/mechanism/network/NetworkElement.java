package org.example.artyom.mechanism.mechanism.network;

import org.bukkit.Location;
import org.example.artyom.mechanism.mechanism.MechanismType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class NetworkElement implements INetworkElement {

    //Тип механизма
    MechanismType mechanismType;
    //Местоположение
    private final Location location;
    //Сеть
    private UUID networkId;
    //Сетевые соединения
    private final Set<INetworkElement> connections = new HashSet<>();

    public NetworkElement(Location location, MechanismType mechanismType) {
        this.location = location;
        this.mechanismType = mechanismType;
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

    @Override
    public UUID getNetworkId() { return networkId; }

    @Override
    public void setNetworkId(UUID networkId) { this.networkId = networkId; }

    @Override
    public MechanismType getMechanismType() { return mechanismType; }
}
