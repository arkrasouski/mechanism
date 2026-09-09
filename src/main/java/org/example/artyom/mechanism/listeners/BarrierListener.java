package org.example.artyom.mechanism.listeners;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.bukkit.event.inventory.InventoryClickEvent;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.example.artyom.mechanism.Mechanism;
import org.example.artyom.mechanism.database.NetworkRepository;
import org.example.artyom.mechanism.database.TransactionManager;
import org.example.artyom.mechanism.inventories.barrier.BarrierActionInventory;
import org.example.artyom.mechanism.inventories.barrier.BarrierHolder;
import org.example.artyom.mechanism.inventories.barrier.BarrierMenuFactory;
import org.example.artyom.mechanism.mechanism.network.NetworkManager;
import org.example.artyom.mechanism.utils.LogUtil;

import java.sql.SQLException;
import java.util.UUID;


public class BarrierListener implements Listener {

    private final TransactionManager transactionManager;
    private final NetworkRepository networkRepository;

    public BarrierListener(TransactionManager transactionManager, NetworkRepository networkRepository) {
        this.transactionManager = transactionManager;
        this.networkRepository = networkRepository;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof BarrierHolder holder)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();

        int slot = event.getRawSlot();

        //Нажимаю на изменение цифры пароля
        if (holder.getScreen() == BarrierActionInventory.SET_PASSWORD && slot >= 11 && slot <= 15) {
            ItemStack item = event.getCurrentItem();
            ItemMeta meta = item.getItemMeta();
            int num = Integer.parseInt(meta.getDisplayName());
            int newNum = ++num > 9 ? 0 : num;
            meta.setDisplayName(String.valueOf(newNum));
            item.setItemMeta(meta);
        }

        //Нажимаю сохранить пароль
        if (holder.getScreen() == BarrierActionInventory.SET_PASSWORD && slot == 22) {
            StringBuilder password = new StringBuilder();
            for(int i = 11; i <= 15; i++){
                ItemStack item = event.getClickedInventory().getItem(i);
                ItemMeta meta = item.getItemMeta();
                password.append(meta.getDisplayName());
            }


            UUID networkId = holder.getMechanismNetworkId();
            NetworkManager network = Mechanism.getNetworkSystems().getNetworkManager(networkId);

            int pass = Integer.parseInt(password.toString());
            UUID playerId = player.getUniqueId();

            try {
                transactionManager.execute((connection -> networkRepository.updateNetwork(connection, networkId, playerId, pass)));
            } catch (SQLException e) {
                LogUtil.error("Не удалось сохранить пароль и владельца сети", e);
                throw new RuntimeException(e);
            }

            network.setPassword(pass);
            network.setOwner(playerId);
            player.sendMessage("Пароль: " + password);
            player.closeInventory();
        }

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
