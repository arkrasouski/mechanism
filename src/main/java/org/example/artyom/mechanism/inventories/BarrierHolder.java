package org.example.artyom.mechanism.inventories;

import org.example.artyom.mechanism.Mechanism;
import org.example.artyom.mechanism.mechanism.MechanismType;
import org.example.artyom.mechanism.mechanism.base.Mech;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;
import org.example.artyom.mechanism.mechanism.network.NetworkManager;

import java.util.List;
import java.util.UUID;

public class BarrierHolder extends MechanismHolder{
    private int page;
    private BarrierActionInventory screen;

    public BarrierHolder(Mech mechanism) {
        super(mechanism, MechanismType.BARRIER, 36, "barrier_glif", null);
        this.page = 1;
        UUID networkId = mechanism.getNetworkId();
        NetworkManager network = Mechanism.getNetworkSystems().getNetworkManager(networkId);

        if(network.getOwner() == null){
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
