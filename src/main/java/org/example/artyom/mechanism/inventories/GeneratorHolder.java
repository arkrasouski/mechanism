package org.example.artyom.mechanism.inventories;

import org.example.artyom.mechanism.mechanism.MechanismType;
import org.example.artyom.mechanism.mechanism.base.Mech;

import java.util.Arrays;

public class GeneratorHolder extends MechanismHolder {
    public GeneratorHolder(Mech mechanism) {
        super(mechanism, MechanismType.GENERATOR, 27, "&f:offset_-64::transformer_menu::offset_64:", Arrays.asList(9, 10));
    }
}
