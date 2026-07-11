package org.example.artyom.mechanism.inventories;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.example.artyom.mechanism.mechanism.MechanismType;
import org.example.artyom.mechanism.mechanism.base.Mech;
import org.example.artyom.mechanism.utils.EnergyUtil;
import org.example.artyom.mechanism.utils.LogUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MechanismHolder implements InventoryHolder {

    private final Mech mechanism;
    private final Inventory inventory;        // сюда положим созданный GUI
    private final MechanismType mechanismType;

    private final int size; //размер инвентаря
    private final List<Integer> activeSlots; //номера активных слотов

    public MechanismHolder(Mech mechanism, MechanismType mechanismType, int size, String glif, List<Integer> activeSlots) {
        this.mechanism = mechanism;
        this.mechanismType = mechanismType;
        if(mechanismType == MechanismType.BARRIER) {
            this.inventory = BarrierMenuFactory.create(this, size, glif);
        }
        else {
            this.inventory = Bukkit.createInventory(this, size, glif);
        }

        this.size = size;
        this.activeSlots = activeSlots;
        updateEnergyBar();
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void updateEnergyBar() {
        int currentEnergy = mechanism.getCurrentEnergy();
        int maxEnergy = mechanism.getMaxEnergyStorage();
        double percent = EnergyUtil.getEnergyPercent(currentEnergy, maxEnergy);

        int start = size - 9, end = size - 1;
        int segments = end - start + 1;
        int filled = (int) percent * 9 / 100;
        LogUtil.warn(percent + " / " + currentEnergy + " / " + maxEnergy + " / " + filled);
        for (int i = 0; i < segments; i++) {
            int slot = start + i;
            if (i >= filled) {
                inventory.setItem(slot, null);
                continue;
            }

            String id = i == 0 ? "mehanisms:transformer_energy_left" :
                    i == segments - 1 ? "mehanisms:transformer_energy_right" :
                            "mehanisms:transformer_energy_middle";

            CustomStack cs = CustomStack.getInstance(id);
            inventory.setItem(slot, cs != null ? cs.getItemStack() : null);
        }

    }

    /**
     * Возвращает номер слота, в который можно положить предмет (он не занят)
     */
    public int findTargetSlot(Inventory top) {

        if(activeSlots == null) return -1;
        for (int slot : activeSlots) {
            ItemStack cur = top.getItem(slot);
            if (cur == null || cur.getType().isAir()) return slot;
        }
        return -1;
    }

    /**
     * Проверяет, не заблокирован ли слот инвентаря
     */
    public boolean isBlocked(int slot) {
        // пример: заблокировать ВСЕ слоты верхнего инвентаря
        // return true;
        if(activeSlots == null) return true;
        for (int i : activeSlots) {
            if (slot == i) return false;
        }
        return true;

    }
    private static double clamp(double v) {
        return Math.max(0, Math.min(100, v));
    }

    public Location getLocation() {
        return mechanism.getLocation();
    }
    public MechanismType getMechanismType() { return mechanismType; }

    public int getSize(){return size;}
}
