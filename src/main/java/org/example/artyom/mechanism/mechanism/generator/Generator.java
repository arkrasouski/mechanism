package org.example.artyom.mechanism.mechanism.generator;


import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.example.artyom.mechanism.mechanism.IEnergyStorage;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;
import org.example.artyom.mechanism.mechanism.network.INetworkProducer;

import java.util.HashSet;
import java.util.Set;

public class Generator // extends BaseMechanism
        implements
        IEnergyStorage,
        INetworkProducer
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

    //Сеть
    private final Set<INetworkElement> connections = new HashSet<>();
    
    private static final BlockFace[] FACES = {
            BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN
    };

    public Generator(
        Location loc,
        int currentEnergy,
        boolean isWorking
    ) {
        this.loc = loc;
        this.currentEnergy = currentEnergy;
        this.isWorking = isWorking;
        this.hasFuelItem = false;
    }

    public Generator(Location location) {
        this(location, 0, true);
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

    //Generator (producer)

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

    // Network Element

    @Override
    public Location getLocation() {
        return this.loc;
    }

    @Override
    public Set<INetworkElement> getConnections() {
        return this.connections;
    }

    @Override
    public void addConnection(INetworkElement element) {
        connections.add(element);
    }

    @Override
    public void removeConnection(INetworkElement element) {
        connections.remove(element);
    }

    public static Generator getBaseGenerator(Location loc) {
        return new Generator(loc, 0, true);
    }

}
