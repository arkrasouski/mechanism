package org.example.artyom.mechanism.inventories;

import org.example.artyom.mechanism.Mechanism;
import org.example.artyom.mechanism.mechanism.MechanismType;
import org.example.artyom.mechanism.mechanism.base.Mech;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;

import java.util.List;
import java.util.UUID;

public class BarrierHolder extends MechanismHolder{
    private int page;
    private BarrierActionInventory screen;

    public BarrierHolder(Mech mechanism) {
        super(mechanism, MechanismType.BARRIER, 36, "barrier_glif", null);
        this.page = 1;
        UUID networkId = mechanism.getNetworkId();
        List<INetworkElement> barriersByNet = Mechanism.getBarriersByNetwork().get(networkId);

        if(barriersByNet.size() == 1){
            this.screen = BarrierActionInventory.SET_PASSWORD;
        }
        else {
            this.screen = BarrierActionInventory.MAIN_MENU;
        }
        this.inventory = BarrierMenuFactory.create(this, size, glif); //переопределяю чтобы был инвентарь усо screen
    }


    public int getPage() {
        return page;
    }

    public void setPage(int page){
        this.page = page;
    }

    public BarrierActionInventory getScreen() {
        return screen;
    }

    public void setScreen(BarrierActionInventory screen) {
        this.screen = screen;
    }
}
