package org.example.artyom.mechanism.mechanism;

import org.bukkit.Location;

public interface IEnergyStorage {
    /**
     * Получить текущий уровень энергии
     */
    int getCurrentEnergy();
    int getMaxEnergyStorage();
    void setCurrentEnergy(int energy);
    int addEnergy(int amount);     // добавить энергию (возвращает false если overflow)
    int extractEnergy(int amount); // извлечь энергию (возвращает false если недостаточно)
}

