package org.example.artyom.mechanism.mechanism;

import org.example.artyom.mechanism.Mechanism;
import org.example.artyom.mechanism.items.BaseItem;

// Нужен для фабрики создания механизмов через mechanismType
@FunctionalInterface
interface IMechanismItemConstructor {
    BaseItem create(Mechanism plugin);
}
