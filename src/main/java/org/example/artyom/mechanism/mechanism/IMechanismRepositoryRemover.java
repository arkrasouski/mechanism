package org.example.artyom.mechanism.mechanism;

import java.util.UUID;

@FunctionalInterface
public interface IMechanismRepositoryRemover {
    boolean remove(String networkId);
}
