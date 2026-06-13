package org.example.artyom.mechanism.mechanism;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.Set;

public interface IEnergyConnector {

    /**
     * Проверить, может ли этот блок соединиться с другим блоком
     * @param otherLocation локация другого блока
     * @return true если соединение возможно
     */
    boolean canConnect(Location otherLocation);

    /**
     * Получить все текущие соединения данного блока
     * @return неизменяемый набор локаций, с которыми есть соединение
     */
    Set<Location> getConnections();

    /**
     * Обновить соединения - просканировать соседние блоки
     * @param block блок, от которого сканируем (обычно this.location.getBlock())
     */
    void scanConnections(Block block);

    /**
     * Получить тип соединителя (CABLE, GENERATOR, CONSUMER, STORAGE)
     * @return тип соединителя
     */
    //ConnectorType getConnectorType();


//    /**
//     * Получить приоритет соединения (для маршрутизации)
//     * @param otherLocation локация другого блока
//     * @return приоритет (выше число = выше приоритет)
//     */
//    int getConnectionPriority(Location otherLocation);

}
