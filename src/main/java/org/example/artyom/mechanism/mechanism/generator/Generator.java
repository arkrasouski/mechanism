package org.example.artyom.mechanism.mechanism.generator;


import org.bukkit.Location;
import org.example.artyom.mechanism.mechanism.MechanismType;
import org.example.artyom.mechanism.mechanism.base.Producer;

public class Generator extends Producer
{
    private static final int CAPACITY = 1000;
    private static final int ENERGY_TRANSFER_PER_TICK = 10;
    private static final int GENERATION_PER_TICK = 5;
    private static final int CONSUME_FUEL_PER_TICK = 7;
    private static final MechanismType MECHANISM_TYPE = MechanismType.GENERATOR;

    /**
     * Конструктор для создания пустого работающего генератора
     */
    public Generator(Location location) {
        super(location,
                CAPACITY,
                ENERGY_TRANSFER_PER_TICK,
                GENERATION_PER_TICK,
                CONSUME_FUEL_PER_TICK,
                MECHANISM_TYPE
        );
    }

    public Generator(Location location, int currentEnergy, boolean isWorking) {
        super(location,
                currentEnergy,
                isWorking,
                CAPACITY,
                ENERGY_TRANSFER_PER_TICK,
                GENERATION_PER_TICK,
                CONSUME_FUEL_PER_TICK,
                MECHANISM_TYPE
        );
    }
}
