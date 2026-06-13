package org.example.artyom.mechanism.mechanism.generator;


import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.example.artyom.mechanism.mechanism.IEnergyStorage;
import org.example.artyom.mechanism.mechanism.network.INetworkNode;
import org.example.artyom.mechanism.mechanism.MechanismType;

import java.util.List;
import java.util.UUID;

public class Generator // extends BaseMechanism
        implements
        IEnergyStorage,
        IEnergyGenerator,
        INetworkNode
        //, IEnergyConnector
{
    private Location loc;
    private int currentEnergy;
    private boolean isWorking;
    private boolean hasFuelItem;  // есть ли предмет топлива НА БУДУЩЕЕ
    // Соединения

    // Константы
    private static final int ENERGY_TRANSGER_PER_TICK = 10;
    private final int GENERATION_PER_TICK = 5;
    private final int CONSUME_FUEL_PER_TICK = 7;
    private final int CAPACITY = 1000;
    
    private static final BlockFace[] FACES = {
            BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN
    };

    Generator(
        Location loc,
        int currentEnergy,
        boolean isWorking
    ) {
        this.loc = loc;
        this.currentEnergy = currentEnergy;
        this.isWorking = isWorking;
        this.hasFuelItem = false;
    }
    //Storage
    @Override
    public int getCurrentEnergy() {
        return currentEnergy;
    }

    @Override
    public int getMaxEnergyStorage() {
        return CAPACITY;
    }

    @Override
    public void setCurrentEnergy(int energy) {
        this.currentEnergy = energy;
    }

    @Override
    public int addEnergy(int amount) {
        int actualAdded = Math.min(amount, CAPACITY - currentEnergy);
        currentEnergy += actualAdded;
        return actualAdded; // 0 если не добавилось ничего
    }

    @Override
    public int extractEnergy(int amount) {
        int actualExtracted = Math.min(amount, currentEnergy);
        currentEnergy -= actualExtracted;
        return actualExtracted; // вернёт 0..amount
    }

    //Generator

    @Override
    public int getGenerationPerTick() {
        return GENERATION_PER_TICK;
    }

    @Override
    public int getFuelPerTick() {
        return CONSUME_FUEL_PER_TICK;
    }

    @Override
    public boolean isWorking() {
        return isWorking;
    }

    @Override
    public boolean hasFuel() {
        return hasFuelItem;
    }

    @Override
    public int produceEnergy() {
        if(hasFuel()) {
            int actualProduced = Math.min(GENERATION_PER_TICK, CAPACITY - currentEnergy);
            return actualProduced;
        }
        return 0;
    }

    // Network Node

    @Override
    public UUID getUuid() {
        return null;
    }

    @Override
    public MechanismType getType() {
        return null;
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public List<UUID> getConnectedNodes() {
        return List.of();
    }

    @Override
    public void addConnection(UUID nodeUuid) {

    }

    @Override
    public void removeConnection(UUID nodeUuid) {

    }

    @Override
    public boolean hasConnection(UUID nodeUuid) {
        return false;
    }
}
