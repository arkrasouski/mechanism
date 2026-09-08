package org.example.artyom.mechanism.records;

import org.example.artyom.mechanism.mechanism.network.INetworkElement;

import java.util.Set;
import java.util.UUID;

/**
 * Данные о компоненте сети после разлома
 */
public record NetworkComponentData(
        Set<INetworkElement> component,
        UUID newOwnerId,
        boolean hasOwner,
        int password
) {}
