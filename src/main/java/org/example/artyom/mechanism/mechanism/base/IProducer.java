package org.example.artyom.mechanism.mechanism.base;

public interface IProducer {
    int getGenerationPerTick();           // сколько энергии генерирует за tick (например 10.0)
    int getFuelPerTick();             // расход топлива за tick (если есть топливо)
    boolean hasFuel();                   // есть ли топливо (для генераторов с топливом)
    int produceEnergy();              // 🔄 ВЫЗОВЕТСЯ каждый tick → генерирует энергию
    // возвращает сколько энергии реально произведено (может быть меньше если буфер полный)
}
