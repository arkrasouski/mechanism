package org.example.artyom.mechanism.mechanism.base;

import org.bukkit.Location;
import org.example.artyom.mechanism.mechanism.MechanismType;

public class Consumer extends Mech implements IConsumer{

    private final int consumption_per_tick;
    private final int charge_per_tick;

    public Consumer(Location location,
                    int capacity,
                    int consumptionPerTick,
                    int chargePerTick,
                    MechanismType mechanismType) {
        super(location, 0, true, capacity, mechanismType);
        consumption_per_tick = consumptionPerTick;
        charge_per_tick = chargePerTick;
    }

    public Consumer(Location location,
                    int currentEnergy,
                    boolean isWorking,
                    int capacity,
                    int consumptionPerTick,
                    int chargePerTick,
                    MechanismType mechanismType) {
        super(location, currentEnergy, isWorking, capacity, mechanismType);
        consumption_per_tick = consumptionPerTick;
        charge_per_tick = chargePerTick;
    }


    @Override
    public int getConsumptionByTick() {
        return consumption_per_tick;
    }

    @Override
    public int getChargeByTick() {
        return charge_per_tick;
    }

    @Override
    public int receiveEnergy(int energy) {
        int canReceive = Math.min(energy, consumption_per_tick);
        return this.addEnergy(canReceive);
    }

    @Override
    public int chargeEnergy() {
        return extractEnergy(charge_per_tick);
    }
}
