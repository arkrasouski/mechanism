package org.example.artyom.mechanism.mechanism.functional_interfaces;

import org.example.artyom.mechanism.Mechanism;
import org.example.artyom.mechanism.items.BaseItem;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;

import java.util.List;
import java.util.Map;
import java.util.UUID;


// Нужен для фабрики создания соотношения механизмов к сети через mechanismType
@FunctionalInterface
public interface IMechanismMechsByNetwork {
    Map<UUID, List<INetworkElement>> getMechanismByNetwork();
}

