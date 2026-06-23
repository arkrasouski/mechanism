package org.example.artyom.mechanism.mechanism.functional_interfaces;

import org.example.artyom.mechanism.mechanism.network.INetworkElement;

// Нужен для фабрики обращения к нужному классу репозитория бд
@FunctionalInterface
public interface IMechanismRepositoryConstructor {
    boolean add(INetworkElement mechanism);
}

