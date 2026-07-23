package org.example.artyom.mechanism.inventories;

import org.bukkit.inventory.InventoryHolder;
import org.example.artyom.mechanism.mechanism.MechanismType;
import org.example.artyom.mechanism.mechanism.base.Mech;

import java.util.List;

public class EncoderHolder extends MechanismHolder {
    public EncoderHolder(Mech mechanism) {
        super(mechanism, MechanismType.ENCODER, 36, "glif", null);
    }
}
