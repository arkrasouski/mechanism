package org.example.artyom.mechanism.utils;

import org.bukkit.Location;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;
import org.example.artyom.mechanism.mechanism.network.NetworkManager;
import org.example.artyom.mechanism.mechanism.network.NetworkSystems;

import java.util.HashSet;
import java.util.Set;

public class NetworkUtil {
    public static Set<NetworkManager> getNetworkManagersByLoc(NetworkSystems networkSystems, Location loc) {
        Set<NetworkManager> connectedNetworks = new HashSet<>();
        // 6 сторон куба
        Location[] sides = BlockUtil.getSidesByLoc(loc);

        for(Location side : sides) {
            for (NetworkManager netManager : networkSystems.getNetworks()) {
                INetworkElement elem = netManager.getElement(side);
                if (elem != null) {
                    connectedNetworks.add(netManager);
                }
            }
        }
        return connectedNetworks;
    }
}
