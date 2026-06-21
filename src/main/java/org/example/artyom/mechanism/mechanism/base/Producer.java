package org.example.artyom.mechanism.mechanism.base;

import org.bukkit.Location;

public abstract class Producer extends Mech implements IProducer {

    private boolean hasFuelItem;
    private final int energy_transfer_per_tick;
    private final int generation_per_tick;
    private final int consume_fuel_per_tick;

    public Producer(Location location,
                    int capacity,
                    int energyTransferPerTick,
                    int generationPerTick,
                    int consumeFuelPerTick
    ) {
        super(location, 0, true, capacity);
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
                    int consumeFuelPerTick
    ) {
        super(location, currentEnergy, isWorking, capacity);
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
        if(hasFuel()) {
            int actualProduced = Math.min(generation_per_tick, getMaxEnergyStorage() - getCurrentEnergy());
            return actualProduced;
        }
        return 0;
    }
}
