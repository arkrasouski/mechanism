package org.example.artyom.mechanism.mechanism.network;

import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class NetworkElement {

    private final Location location;
    // private final NodeType type;
    private final Set<NetworkElement> connections = new HashSet<>();

    public NetworkElement(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public Set<NetworkElement> getConnections() {
        return connections;
    }
}
