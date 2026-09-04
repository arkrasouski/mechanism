package org.example.artyom.mechanism.inventories;

import org.bukkit.Location;
import org.example.artyom.mechanism.Mechanism;
import org.example.artyom.mechanism.mechanism.MechanismType;
import org.example.artyom.mechanism.mechanism.base.Mech;
import org.example.artyom.mechanism.mechanism.network.NetworkManager;
import org.example.artyom.mechanism.utils.BlockUtil;
import org.example.artyom.mechanism.utils.NetworkUtil;

import java.util.HashSet;
import java.util.Set;

public class EncoderHolder extends MechanismHolder {

    private EncoderActionInventory screen;

    public EncoderHolder(Mech mechanism) {
        super(mechanism, MechanismType.ENCODER, 36, "glif", null);

        Location loc = mechanism.getLocation();
        //не более 6 штук получится
        Set<NetworkManager> connectedNetworks = NetworkUtil.getNetworkManagersByLoc(Mechanism.getNetworkSystems(), loc);

        this.screen = EncoderActionInventory.MAIN_MENU;
        this.inventory = EncoderMenuFactory.create(this, size, glif, connectedNetworks);


    }

    public EncoderActionInventory getScreen() {
        return screen;
    }

    public void setScreen(EncoderActionInventory screen) {
        this.screen = screen;
    }
}
