package org.example.artyom.mechanism.mechanism.base;

public interface IConsumer {

    int getConsumptionByTick();
    int getChargeByTick();
    int receiveEnergy(int energy);
    int chargeEnergy();
}
