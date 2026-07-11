package org.example.artyom.mechanism.inventories;

import org.example.artyom.mechanism.mechanism.MechanismType;
import org.example.artyom.mechanism.mechanism.base.Mech;

import java.util.List;

public class BarrierHolder extends MechanismHolder{
    private final int page;
    private final BarrierActionInventory screen;

    public BarrierHolder(Mech mechanism) {
        super(mechanism, MechanismType.BARRIER, 36, "barrier_glif", null);
        this.page = 1;
        this.screen = BarrierActionInventory.MAIN_MENU;
    }

    public int getPage() {
        return page;
    }
}
