package org.example.artyom.mechanism.mechanism;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.example.artyom.mechanism.Mechanism;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;
import org.example.artyom.mechanism.utils.LogUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

    public boolean getMechanismsInChunk(Chunk chunk) {
        List<INetworkElement> result = new ArrayList<>();

        // Получаем границы чанка
        int minX = chunk.getX() * 16;
        int minZ = chunk.getZ() * 16;
        int maxX = minX + 15;
        int maxZ = minZ + 15;
        String worldName = chunk.getWorld().getName();

        boolean isMechanism = false;
        // Перебираем все механизмы
        for (Map.Entry<Location, INetworkElement> entry : allMechanisms.entrySet()) {
            Location loc = entry.getKey();

            // Проверяем, принадлежит ли локация этому чанку
            if (loc.getWorld().getName().equals(worldName) &&
                    loc.getBlockX() >= minX && loc.getBlockX() <= maxX &&
                    loc.getBlockZ() >= minZ && loc.getBlockZ() <= maxZ) {
                isMechanism = true;
            }
        }

        return isMechanism;
    }
}
