package org.example.artyom.mechanism.mechanism.encoder;

import org.bukkit.Location;
import org.example.artyom.mechanism.mechanism.MechanismType;
import org.example.artyom.mechanism.mechanism.base.Consumer;

public class Encoder extends Consumer {
    private static final int CAPACITY = 500;
    private static final int CONSUMPTION_PER_TICK = 10;
    private static final int CHARGE_PER_TICK = -1;
    private static final MechanismType MECHANISM_TYPE = MechanismType.ENCODER;

    /**
     * Конструктор для создания пустого работающего шифратора
     */
    public Encoder(Location location) {
        super(location,
                0,
                false,
                CAPACITY,
                CONSUMPTION_PER_TICK,
                CHARGE_PER_TICK,
                MECHANISM_TYPE);
    }

    /**
     * Конструктор создания шифратора с готовыми значениями энергии и флага работы
     */
    public Encoder(Location location, int currentEnergy) {
        super(location,
                currentEnergy,
                false,
                CAPACITY,
                CONSUMPTION_PER_TICK,
                CHARGE_PER_TICK,
                MECHANISM_TYPE);
    }
}
