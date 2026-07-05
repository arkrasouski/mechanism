//package org.example.artyom.mechanism.inventories;
//
//import dev.lone.itemsadder.api.CustomStack;
//import org.example.artyom.mechanism.mechanism.generator.Generator;
//import org.example.artyom.mechanism.utils.EnergyUtil;
//
//import java.util.Arrays;
//
//public class GeneratorInventory extends BaseFillCustomInventory {
//
//    private final Generator generator;
//
//    public GeneratorInventory(Generator generator) {
//        super(27, "&f:offset_-64::transformer_menu::offset_64:", Arrays.asList(9, 10));
//        this.generator = generator;
//    }
//
//    //TODO: ПРоверить проценты и сегменты прогресса
//    //TODO: Тикать, если не полный бак
//    @Override
//    public void updateEnergyBar() {
//        int currentEnergy = generator.getCurrentEnergy();
//        int maxEnergy = generator.getMaxEnergyStorage();
//        double percent = EnergyUtil.getEnergyPercent(currentEnergy, maxEnergy);
//        int size = inventory.getSize(); // ✅ ТОЧНЫЙ размер целевого инвентаря
//        int start = size - 9, end = size - 1;
//        int segments = end - start + 1;
//        int filled = (int) Math.round(clamp(percent) / 100.0 * segments);
//
//        for (int i = 0; i < segments; i++) {
//            int slot = start + i;
//            if (i >= filled) {
//                inventory.setItem(slot, null);
//                continue;
//            }
//
//            String id = i == 0 ? "mehanisms:transformer_energy_left" :
//                    i == segments - 1 ? "mehanisms:transformer_energy_right" :
//                            "mehanisms:transformer_energy_middle";
//
//            CustomStack cs = CustomStack.getInstance(id);
//            inventory.setItem(slot, cs != null ? cs.getItemStack() : null);
//        }
//
//        holder.setNewEnergyPercent(percent);
//    }
//}
