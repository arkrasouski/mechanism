package org.example.artyom.mechanism.mechanism.functional_interfaces;

import org.bukkit.entity.Player;
import org.example.artyom.mechanism.Mechanism;
import org.example.artyom.mechanism.inventories.MechanismHolder;
import org.example.artyom.mechanism.items.BaseItem;
import org.example.artyom.mechanism.mechanism.base.Mech;


// Нужен для фабрики создания холдеров инвентаря через тип механизма
@FunctionalInterface
public interface IMechanismHolderCreate {
    MechanismHolder getHolder(Mech mechanism, Player player);
}

