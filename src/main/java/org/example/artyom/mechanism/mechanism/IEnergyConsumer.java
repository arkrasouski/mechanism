package org.example.artyom.mechanism.mechanism;

import org.bukkit.Location;

public interface IEnergyConsumer {
    /**
     * Потребить энергию (использовать для функционала)
     */
    void consumeEnergy(int amount);

    /**
     * Получить скорость потребления
     */
    int getConsumptionRate();

    /**
     * Проверить, нужна ли энергия сейчас
     */
    boolean needsEnergy();

    boolean canReceive(Location loc);

    int receiveEnergy(int extracted);
}
