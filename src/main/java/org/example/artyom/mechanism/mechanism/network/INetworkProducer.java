package org.example.artyom.mechanism.mechanism.network;

public interface INetworkProducer extends INetworkElement {
    int getGenerationPerTick();           // сколько энергии генерирует за tick (например 10.0)
    int getFuelPerTick();             // расход топлива за tick (если есть топливо)
    boolean isWorking();                 // работает ли сейчас (не выключен, есть топливо)
    boolean hasFuel();                   // есть ли топливо (для генераторов с топливом)
    int produceEnergy();              // 🔄 ВЫЗОВЕТСЯ каждый tick → генерирует энергию
    // возвращает сколько энергии реально произведено (может быть меньше если буфер полный)
}
