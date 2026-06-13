package org.example.artyom.mechanism.mechanism.network;

import org.bukkit.Location;

import java.util.Set;
import java.util.UUID;

public interface IEnergyNetwork {


    /**
     * Передать энергию через сеть
     * @param amount количество энергии
     * @param fromGenerator источник (генератор)
     * @return сколько энергии было передано
     */
    int transferEnergy(Location fromGenerator, Location toConsumer, int amount);

    /**
     * Проверить, находится ли локация в этой сети
     */
    boolean isInNetwork(Location loc);

    /**
     * Получить все соединения в сети
     */
    Set<Location> getAllConnections();

    /**
     * Проверить валидность сети (связность, наличие генераторов и потребителей)
     */
    boolean isValid();

    /**
     * Получить уникальный ID сети
     */
    UUID getNetworkId();
}
