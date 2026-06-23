package org.example.artyom.mechanism.mechanism.functional_interfaces;

import org.bukkit.Location;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;


// Нужен для фабрики создания механизмов через mechanismType
@FunctionalInterface
public interface IMechanismConstructor {
    INetworkElement create(Location loc);
}
