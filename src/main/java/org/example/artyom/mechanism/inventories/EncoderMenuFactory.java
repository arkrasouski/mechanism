package org.example.artyom.mechanism.inventories;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.example.artyom.mechanism.mechanism.network.NetworkManager;
import org.example.artyom.mechanism.utils.ItemsUtil;

import java.util.Iterator;
import java.util.Set;

public class EncoderMenuFactory {
    public static Inventory create(
            MechanismHolder holder,
            int size,
            String glif,
            Set<NetworkManager> connectedNetworks
    ) {
        Inventory inv = Bukkit.createInventory(holder, size, glif);
        EncoderHolder encoderHolder = (EncoderHolder) holder;
        return fillPageContent(inv, encoderHolder.getScreen(), connectedNetworks);
    }

    public static Inventory fillPageContent(
            Inventory inv,
            EncoderActionInventory screen,
            Set<NetworkManager> connectedNetworks
    ) {
        switch (screen) {
//            case ENTER_PASSWORD:
//                return fillSetPassword(inv);
            default:
                return fillMainMenu(inv, connectedNetworks);
        }
    }
    private static Inventory fillMainMenu(Inventory inv, Set<NetworkManager> connectedNetworks) {
        Iterator<NetworkManager> netManagerIterator = connectedNetworks.iterator();
        for (int i = 0; i < connectedNetworks.size(); i++) {
            NetworkManager networkManager = netManagerIterator.next();
            ItemStack item = ItemsUtil.create(
                    Material.PURPLE_BANNER,
                    1,
                    networkManager.getNetworkId().toString()
            );

            inv.setItem(i, item);
        }
        return inv;
    }
}
