package org.example.artyom.mechanism.listeners;
import org.apache.commons.logging.Log;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.bukkit.event.inventory.InventoryClickEvent;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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
        if (holder.getScreen() == BarrierActionInventory.MAIN_MENU && slot == 14) {;
           Inventory inv = BarrierMenuFactory.fillPageContent(Bukkit.createInventory(holder, holder.getSize(),
                                                                holder.getGlif()),
                                                                BarrierActionInventory.PLAYER_LIST,
                                                                1);
           holder.setInventory(inv);
           holder.updateEnergyBar();
           holder.setScreen(BarrierActionInventory.PLAYER_LIST);
           player.openInventory(inv);
        }

        if (holder.getScreen() == BarrierActionInventory.PLAYER_LIST && slot == 26) {
            ItemStack item = event.getCurrentItem();
            if (!item.hasItemMeta()) return;
            int page = holder.getPage() + 1;
            holder.setPage(page);
            Inventory inv = BarrierMenuFactory.fillPageContent(Bukkit.createInventory(holder, holder.getSize(),
                                                                holder.getGlif()),
                                                                BarrierActionInventory.PLAYER_LIST,
                                                                page);
            holder.setInventory(inv);
            holder.updateEnergyBar();
            player.openInventory(inv);
        }

        if (holder.getScreen() == BarrierActionInventory.PLAYER_LIST && slot == 18) {
            ItemStack item = event.getCurrentItem();
            if (!item.hasItemMeta()) return;
            int page = holder.getPage() - 1;
            holder.setPage(page);
            Inventory inv = BarrierMenuFactory.fillPageContent(Bukkit.createInventory(holder, holder.getSize(),
                            holder.getGlif()),
                    BarrierActionInventory.PLAYER_LIST,
                    page);
            holder.setInventory(inv);
            holder.updateEnergyBar();
            player.openInventory(inv);
        }

    }
}
