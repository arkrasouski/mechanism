package org.example.artyom.mechanism.records;

import org.example.artyom.mechanism.mechanism.network.INetworkElement;
import org.example.artyom.mechanism.mechanism.network.NetworkManager;

import java.util.List;
import java.util.Set;
import java.util.UUID;
/**
 * Результат операции разделения сети
 */
public record NetworkSplitResult(
        UUID oldNetworkId,
        NetworkManager oldNetwork,
        Set<INetworkElement> neighbors,
        List<NetworkManager> newManagers,
        List<Set<INetworkElement>> components,
        List<NetworkComponentData> componentData
) {}
