package org.example.artyom.mechanism.inventories;

import org.bukkit.entity.Player;
import org.example.artyom.mechanism.mechanism.MechanismType;
import org.example.artyom.mechanism.mechanism.base.Mech;

import java.util.Arrays;

public class GeneratorHolder extends MechanismHolder {
    public GeneratorHolder(Mech mechanism, Player player) {
        super(mechanism, MechanismType.GENERATOR, 27, "&f:offset_-64::transformer_menu::offset_64:", Arrays.asList(9, 10));
    }
}
