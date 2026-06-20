package org.example.artyom.mechanism.mechanism;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.example.artyom.mechanism.Mechanism;
import org.example.artyom.mechanism.mechanism.generator.Generator;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;
import org.example.artyom.mechanism.utils.LogUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MechanismManager<T extends INetworkElement> {

    private final Mechanism plugin;

    private final Map<Location, T> allMechanisms = new ConcurrentHashMap<>();

    public MechanismManager(Mechanism plugin) {
        this.plugin = plugin;
    }
    /**
     * Создать генератор
     */
    public void registerGenerator(T mechanism, Location location) {
        allMechanisms.put(location, mechanism);
        LogUtil.info("Создан новый Генератор на " + location);
    }

    /**
     * Удалить генератор
     */
    public void deleteGenerator(Location location) {
        allMechanisms.remove(location);
        LogUtil.info("Удален генератор с " + location);
    }

    /**
     * Получает механизм по локации
     */
    public T getMechanism(Location location) {
        return allMechanisms.get(location);
    }

    /**
     * Получает механизм по блоку
     */
    public T getGenerator(Block block) {
        return getMechanism(block.getLocation());
    }

    /**
     * Проверяет, является ли блок механизмом данного типа
     */
    public boolean isMechanism(Block block) {
        return allMechanisms.containsKey(block.getLocation());
    }
}
