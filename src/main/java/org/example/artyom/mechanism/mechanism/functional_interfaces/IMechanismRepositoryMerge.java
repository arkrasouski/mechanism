package org.example.artyom.mechanism.mechanism.functional_interfaces;


import org.example.artyom.mechanism.mechanism.network.INetworkElement;

import java.util.List;

@FunctionalInterface
public interface IMechanismRepositoryMerge {
    List<INetworkElement> get(String networkId);
}