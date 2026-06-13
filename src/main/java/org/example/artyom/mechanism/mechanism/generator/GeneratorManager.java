package org.example.artyom.mechanism.mechanism.generator;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.example.artyom.mechanism.Mechanism;
import org.example.artyom.mechanism.utils.LogUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GeneratorManager {
    private final Mechanism plugin;

    private final Map<Location, Generator> allGenerators = new ConcurrentHashMap<>();

    public GeneratorManager(Mechanism plugin) {
        this.plugin = plugin;
    }
    /**
     * Создать генератор
     */
    public Generator createGenerator(Location location, Player owner) {
        Generator generator = new Generator(location, 0, true);
        allGenerators.put(location, generator);
        LogUtil.info("Создан новый Генератор на " + location);
        return generator;
    }

    /**
     * Удалить генератор
     */
    public void deleteGenerator(Location location) {
        allGenerators.remove(location);
        LogUtil.info("Удален генератор с " + location);
    }

    /**
     * Получает механизм по локации
     */
    public Generator getGenerator(Location location) {
        return allGenerators.get(location);
    }

    /**
     * Получает механизм по блоку
     */
    public Generator getGenerator(Block block) {
        return getGenerator(block.getLocation());
    }

    /**
     * Проверяет, является ли блок механизмом данного типа
     */
    public boolean isGenerator(Block block) {
        return allGenerators.containsKey(block.getLocation());
    }


}
