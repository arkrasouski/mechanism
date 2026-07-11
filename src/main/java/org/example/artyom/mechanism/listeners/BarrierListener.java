package org.example.artyom.mechanism.listeners;
import org.apache.commons.logging.Log;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.bukkit.event.inventory.InventoryClickEvent;

import org.bukkit.inventory.Inventory;
import org.example.artyom.mechanism.inventories.BarrierActionInventory;
import org.example.artyom.mechanism.inventories.BarrierHolder;
import org.example.artyom.mechanism.inventories.BarrierMenuFactory;
import org.example.artyom.mechanism.utils.LogUtil;


public class BarrierListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof BarrierHolder holder)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();

        int slot = event.getRawSlot();
        LogUtil.warn(slot + "слот");
        if (holder.getScreen() == BarrierActionInventory.MAIN_MENU && slot == 14) {
           Inventory inv = BarrierMenuFactory.fillPageContent(Bukkit.createInventory(holder, holder.getSize(), holder.getGlif()), BarrierActionInventory.PLAYER_LIST, 1);
           holder.setInventory(inv);

           player.openInventory(inv);
        }

    }
}
