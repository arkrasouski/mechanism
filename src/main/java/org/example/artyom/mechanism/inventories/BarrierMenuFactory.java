package org.example.artyom.mechanism.inventories;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.example.artyom.mechanism.Mechanism;
import org.example.artyom.mechanism.database.PlayerRepository;
import org.example.artyom.mechanism.database.TransactionManager;
import org.example.artyom.mechanism.records.PlayerData;
import org.example.artyom.mechanism.utils.ItemsUtil;
import org.example.artyom.mechanism.utils.LogUtil;

import java.sql.SQLException;
import java.util.List;

public class BarrierMenuFactory {

    public static Inventory create(MechanismHolder holder, int size, String glif) {
        Inventory inv = Bukkit.createInventory(holder, size, glif);
        BarrierHolder barrierHolder = (BarrierHolder) holder;
        fillBase(inv, holder);
        //fillNavigation(inv, holder.getPage());

        return fillPageContent(inv, BarrierActionInventory.MAIN_MENU, barrierHolder.getPage());
    }

    private static void fillBase(Inventory inv, MechanismHolder holder) {
        // фон, инфо, декоративные элементы
    }

    public static Inventory fillPageContent(Inventory inv, BarrierActionInventory screen, int page) {
        switch (screen) {
            case MAIN_MENU:
                return fillMainMenu(inv);
            case PLAYER_LIST:
                return fillPlayerList(inv, page);
            case PLAYER_SETTINGS:

                return null;
            default:
                return fillMainMenu(inv);
        }
    }

    private static void fillNavigation(Inventory inv, int page) {
//        inv.setItem(27, previousButton(page));
//        inv.setItem(31, closeButton());
//        inv.setItem(35, nextButton(page));
    }

    private static Inventory fillMainMenu(Inventory inv) { //флаг чтобы отличать первый раз открываем или нет
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                if (j < 4) {
                    int num = (j + 1) + 4 * i;
                    ItemStack item = ItemsUtil.create(Material.LIGHT_BLUE_WOOL,
                            1,
                            "Игрок №" + num,
                            List.of("Редактировать"));
                    int slot_num = j + (9 * i);
                    inv.setItem(slot_num, item);
                }
            }
        }
        inv.setItem(14, ItemsUtil.create(Material.GREEN_WOOL, 1,
                "Добавить игрока",
                List.of("Нажмите, чтобы добавить игрока в приват")));
        return inv;
    }

    private static Inventory fillPlayerList(Inventory inv, int page) {

        TransactionManager transactionManager = Mechanism.getTransactionManager();
        try {
            List<PlayerData> playerList = transactionManager.execute((connection -> PlayerRepository.getPlayers(connection, page)));
            for (int i = 0; i < playerList.size(); i++) {
                PlayerData playerData = playerList.get(i);
                LogUtil.warn(playerData.name());
                ItemStack item = ItemsUtil.create(Material.LIGHT_BLUE_WOOL,
                        1,
                        playerData.name(),
                        List.of("Добавить"));
                inv.setItem(i, item);
            }
            return inv;
        }
        catch (SQLException e) {
            LogUtil.error("Ошибка получения списка игроков из бд", e);
            e.printStackTrace();
        }
        return null;
    }
}
