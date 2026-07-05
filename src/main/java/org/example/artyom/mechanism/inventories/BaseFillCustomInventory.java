//package org.example.artyom.mechanism.inventories;
//
//import org.bukkit.Bukkit;
//import org.bukkit.inventory.Inventory;
//import org.bukkit.inventory.ItemStack;
//
//import java.util.List;
//
//public abstract class BaseFillCustomInventory {
//
//    private final int size; //размер инвентаря
//    private final String glif; //глиф текстуры внутри инветаря
//    private final List<Integer> activeSlots; //номера активных слотов
//    protected Inventory inventory;
//    protected MechanismHolder holder;
//
//    protected BaseFillCustomInventory(int size, String glif, List<Integer> activeSlots) {
//        this.size = size; //27
//        this.glif = glif; //"&f:offset_-64::transformer_menu::offset_64:"
//        this.activeSlots = activeSlots; //[9, 10]
//    }
//
//
//    public Inventory createInventory(MechanismHolder holder, double percent) {
//        this.holder = holder;
//        this.inventory = Bukkit.createInventory(holder, this.size, this.glif);
//        this.holder.setInventory(this.inventory);
//        return inventory;
//    }
//
//
//    public abstract void updateEnergyBar();
//
//    protected static double clamp(double v) {
//        return Math.max(0, Math.min(100, v));
//    }
//
//    public int findTargetSlot(Inventory top) {
//
//        if(activeSlots ==null) return -1;
//        for (int slot : activeSlots) {
//            ItemStack cur = top.getItem(slot);
//            if (cur == null || cur.getType().isAir()) return slot;
//        }
//        return -1;
//    }
//    public boolean isBlocked(int slot) {
//        // пример: заблокировать ВСЕ слоты верхнего инвентаря
//        // return true;
//        if(activeSlots ==null) return true;
//        for (int i : activeSlots) {
//            if (slot == i) return false;
//        }
//        return true;
//
//    }
//    public int getSize() {
//        return size;
//    }
//}
