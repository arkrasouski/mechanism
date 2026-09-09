package org.example.artyom.mechanism.inventories.encoder;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.example.artyom.mechanism.inventories.MechanismHolder;
import org.example.artyom.mechanism.mechanism.network.NetworkManager;
import org.example.artyom.mechanism.utils.ItemsUtil;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class EncoderMenuFactory {
    public static Inventory create(
            MechanismHolder holder,
            int size,
            String glif,
            Set<NetworkManager> connectedNetworks,
            Player player
    ) {
        Inventory inv = Bukkit.createInventory(holder, size, glif);
        EncoderHolder encoderHolder = (EncoderHolder) holder;
        return fillPageContent(inv, encoderHolder.getScreen(), connectedNetworks, player);
    }

    public static Inventory fillPageContent(
            Inventory inv,
            EncoderActionInventory screen,
            Set<NetworkManager> connectedNetworks,
            Player player
    ) {
        switch (screen) {
//            case ENTER_PASSWORD:
//                return fillSetPassword(inv);
            default:
                return fillMainMenu(inv, connectedNetworks, player);
        }
    }
    private static Inventory fillMainMenu(Inventory inv, Set<NetworkManager> connectedNetworks, Player player) {
        List<ItemStack> items = connectedNetworks.stream()
                .filter(network -> {
                    UUID owner = network.getOwner();
                    return owner != null && !owner.equals(player.getUniqueId());
                })
                .map(network -> ItemsUtil.create(
                        Material.PURPLE_BANNER,
                        1,
                        network.getNetworkId().toString()
                ))
                .toList();

        for (int i = 0; i < items.size(); i++) {
            inv.setItem(i, items.get(i));
        }
        return inv;
    }
}
