package org.example.artyom.mechanism.mechanism;

import org.bukkit.Location;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;

import java.util.UUID;

// Нужен для фабрики обращения к нужному классу репозитория бд
@FunctionalInterface
interface IMechanismRepositoryConstructor {
    boolean add(INetworkElement mechanism);
}

