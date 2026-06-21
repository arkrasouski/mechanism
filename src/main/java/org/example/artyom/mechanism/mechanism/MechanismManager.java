package org.example.artyom.mechanism.mechanism;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.example.artyom.mechanism.Mechanism;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;
import org.example.artyom.mechanism.utils.LogUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MechanismManager {

    private final Mechanism plugin;

    private final Map<Location, INetworkElement> allMechanisms = new ConcurrentHashMap<>();

    public MechanismManager(Mechanism plugin) {
        this.plugin = plugin;
    }
    /**
     * Создать генератор
     */
    public void registerMechanism(INetworkElement mechanism, Location location) {
        allMechanisms.put(location, mechanism);
        LogUtil.info("Создан новый Генератор на " + location);
    }

    /**
     * Удалить генератор
     */
    public void deleteMechanism(Location location) {
        allMechanisms.remove(location);
        LogUtil.info("Удален генератор с " + location);
    }

    /**
     * Получает механизм по локации
     */
    public INetworkElement getMechanism(Location location) {
        return allMechanisms.get(location);
    }

    /**
     * Получает механизм по блоку
     */
    public INetworkElement getMechanism(Block block) {
        return getMechanism(block.getLocation());
    }

    /**
     * Проверяет, является ли блок механизмом данного типа
     */
    public boolean isMechanism(Block block) {
        return allMechanisms.containsKey(block.getLocation());
    }
}
