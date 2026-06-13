package org.example.artyom.mechanism.mechanism.generator;

public interface IEnergyGenerator {
    int getGenerationPerTick();           // сколько энергии генерирует за tick (например 10.0)
    int getFuelPerTick();             // расход топлива за tick (если есть топливо)
    boolean isWorking();                 // работает ли сейчас (не выключен, есть топливо)
    boolean hasFuel();                   // есть ли топливо (для генераторов с топливом)
//    int getFuelAmount();              // текущее количество топлива
//    int getMaxFuelAmount();           // Max топлива
//    void setFuel(int amount);         // установить топливо (для GUI/reload)
//    void consumeFuel(int amount);     // расходовать топливо
    int produceEnergy();              // 🔄 ВЫЗОВЕТСЯ каждый tick → генерирует энергию
    // возвращает сколько энергии реально произведено (может быть меньше если буфер полный)

}
