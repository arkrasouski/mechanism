package org.example.artyom.mechanism.mechanism.base;

import org.bukkit.Location;
import org.example.artyom.mechanism.mechanism.MechanismType;
import org.example.artyom.mechanism.mechanism.network.NetworkElement;

public abstract class Mech extends NetworkElement implements IMech {

    private int currentEnergy;
    private final boolean isWorking;
    private final int capacity;

    public Mech(Location location, int currentEnergy, boolean isWorking, int capacity, MechanismType mechanismType) {
        super(location, mechanismType);
        this.currentEnergy = currentEnergy;
        this.isWorking = isWorking;
        this.capacity = capacity;
    }

    @Override
    public int getCurrentEnergy() {
        return currentEnergy;
    }

    @Override
    public int getMaxEnergyStorage() {
        return capacity;
    }

    @Override
    public void setCurrentEnergy(int energy) {
        this.currentEnergy = energy;
    }

    @Override
    public int addEnergy(int amount) {
        int actualAdded = Math.min(amount, capacity - currentEnergy);
        currentEnergy += actualAdded;
        return actualAdded; // 0 если не добавилось ничего
    }

    @Override
    public int extractEnergy(int amount) {
        int actualExtracted = Math.min(amount, currentEnergy);
        currentEnergy -= actualExtracted;
        return actualExtracted; // вернёт 0..amount
    }

    @Override
    public boolean isWorking() {
        return isWorking;
    }

    @Override
    public boolean hasSpace() {return currentEnergy < capacity; }

    @Override
    public int freeSpace() { return capacity - currentEnergy; }

    @Override
    public boolean isFull() {return capacity == currentEnergy; }
}
