package org.example.artyom.mechanism.mechanism;

import org.bukkit.Location;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;


// Нужен для фабрики создания механизмов через mechanismType
@FunctionalInterface
interface IMechanismConstructor {
    INetworkElement create(Location loc);
}
