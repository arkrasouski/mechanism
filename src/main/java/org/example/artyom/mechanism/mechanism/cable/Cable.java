package org.example.artyom.mechanism.mechanism.cable;

import org.bukkit.Location;
import org.example.artyom.mechanism.mechanism.MechanismType;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;
import org.example.artyom.mechanism.mechanism.network.NetworkElement;

import java.util.HashSet;
import java.util.Set;

public class Cable extends NetworkElement {

    private static final MechanismType mechanismType = MechanismType.CABLE;

    public Cable(Location location) {
        super(location, mechanismType);
    }
}
