package org.example.artyom.mechanism.mechanism.base;

public interface IMech {
    int getCurrentEnergy();
    int getMaxEnergyStorage();
    void setCurrentEnergy(int energy);
    int addEnergy(int amount);
    int extractEnergy(int amount);
    boolean isWorking();
    boolean hasSpace();
    int freeSpace();
    boolean isFull();
}
