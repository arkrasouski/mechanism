package org.example.artyom.mechanism.mechanism.barrier;

import org.bukkit.Location;
import org.example.artyom.mechanism.Mechanism;
import org.example.artyom.mechanism.mechanism.MechanismType;
import org.example.artyom.mechanism.mechanism.base.Consumer;

public class Barrier extends Consumer {

    private static final int CAPACITY = 700;
    private static final int CONSUMPTION_PER_TICK = 10;
    private static final int CHARGE_PER_TICK = 7;
    private static final MechanismType MECHANISM_TYPE = MechanismType.BARRIER;

    /**
     * Конструктор для создания пустого работающего барьера
     */
    public Barrier(Location location) {
        super(location,
                CAPACITY,
                CONSUMPTION_PER_TICK,
                CHARGE_PER_TICK,
                MECHANISM_TYPE);
    }

    /**
     * Конструктор создания барьера с готовыми значениями энергии и флага работы
     */
    public Barrier(Location location, int currentEnergy, boolean isWorking) {
        super(location,
                currentEnergy,
                isWorking,
                CAPACITY,
                CONSUMPTION_PER_TICK,
                CHARGE_PER_TICK,
                MECHANISM_TYPE);
    }
}
