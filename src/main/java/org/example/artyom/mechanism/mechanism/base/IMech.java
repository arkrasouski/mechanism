package org.example.artyom.mechanism.mechanism.base;

public interface IMech {
    public int getCurrentEnergy();
    public int getMaxEnergyStorage();
    public void setCurrentEnergy(int energy);
    public int addEnergy(int amount);
    public int extractEnergy(int amount);
    public boolean isWorking();

}
