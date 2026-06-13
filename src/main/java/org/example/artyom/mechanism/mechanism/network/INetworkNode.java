package org.example.artyom.mechanism.mechanism.network;

import org.bukkit.Location;
import org.example.artyom.mechanism.mechanism.MechanismType;

import java.util.List;
import java.util.UUID;

public interface INetworkNode {
    UUID getUuid();                      // уникальный ID узла
    MechanismType getType();                  // GENERATOR
    Location getLocation();              // позиция в мире

    List<UUID> getConnectedNodes();      // список UUID соседних узлов (кабелей/cube)
    void addConnection(UUID nodeUuid);   // добавить соединение
    void removeConnection(UUID nodeUuid); // удалить соединение
    boolean hasConnection(UUID nodeUuid); // есть ли соединение

//    int getTransferRatePerTick();     // макс. передача энергии за tick (лимит выхода)
//    int calculateEnergyToSend();      // сколько энергии можно отправить в сеть сейчас
//    void sendEnergyToNetwork(int amount); // 🔄 отправить энергию в сеть
//
//    boolean canReceiveEnergy();          // может ли получать энергию (обычно false для генератора)
//    boolean canSendEnergy();             // может ли отдавать энергию (true для генератора)
}
