package org.example.artyom.mechanism.mechanism.functional_interfaces;

import org.example.artyom.mechanism.Mechanism;
import org.example.artyom.mechanism.items.BaseItem;

// Нужен для фабрики создания механизмов через mechanismType
@FunctionalInterface
public interface IMechanismItemConstructor {
    BaseItem create(Mechanism plugin);
}
