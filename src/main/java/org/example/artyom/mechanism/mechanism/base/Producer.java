package org.example.artyom.mechanism.mechanism.base;

import org.bukkit.Location;
import org.example.artyom.mechanism.Mechanism;
import org.example.artyom.mechanism.mechanism.MechanismType;

public abstract class Producer extends Mech implements IProducer {

    private boolean hasFuelItem;
    private final int energy_transfer_per_tick;
    private final int generation_per_tick;
    private final int consume_fuel_per_tick;

    public Producer(Location location,
                    int capacity,
                    int energyTransferPerTick,
                    int generationPerTick,
                    int consumeFuelPerTick,
                    MechanismType mechanismType
    ) {
        super(location, 0, true, capacity, mechanismType);
        energy_transfer_per_tick = energyTransferPerTick;
        generation_per_tick = generationPerTick;
        consume_fuel_per_tick = consumeFuelPerTick;
    }

    public Producer(Location location,
                    int currentEnergy,
                    boolean isWorking,
                    int capacity,
                    int energyTransferPerTick,
                    int generationPerTick,
                    int consumeFuelPerTick,
                    MechanismType mechanismType
    ) {
        super(location, currentEnergy, isWorking, capacity, mechanismType);
        energy_transfer_per_tick = energyTransferPerTick;
        generation_per_tick = generationPerTick;
        consume_fuel_per_tick = consumeFuelPerTick;
    }

    @Override
    public int getGenerationPerTick() {
        return generation_per_tick;
    }

    @Override
    public int getFuelPerTick() {
        return consume_fuel_per_tick;
    }

    @Override
    public boolean hasFuel() {
        return hasFuelItem;
    }

    @Override
    public int produceEnergy() {
        if (true && isWorking() && (getCurrentEnergy() < getMaxEnergyStorage())) {  //if(hasFuel()) {
            int actualProduced = Math.min(generation_per_tick, getMaxEnergyStorage() - getCurrentEnergy());
            return actualProduced;
        }
        return 0;
    }

    public int getEnergyTransferPerTick() {return energy_transfer_per_tick;}
}
