package org.example.artyom.mechanism.utils;

public class EnergyUtil {
    public static double getEnergyPercent(int currentEnergy, int maxEnergy) {
        return (double) (currentEnergy * 100) / maxEnergy;
    }
}
