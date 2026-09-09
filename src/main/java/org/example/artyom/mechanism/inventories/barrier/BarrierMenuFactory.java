package org.example.artyom.mechanism.inventories.barrier;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.example.artyom.mechanism.Mechanism;
import org.example.artyom.mechanism.database.PlayerRepository;
import org.example.artyom.mechanism.database.TransactionManager;
import org.example.artyom.mechanism.inventories.MechanismHolder;
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
        return fillPageContent(inv, barrierHolder.getScreen(), barrierHolder.getPage());
    }

    private static void fillBase(Inventory inv, MechanismHolder holder) {
        // фон, инфо, декоративные элементы
    }

    public static Inventory fillPageContent(Inventory inv, BarrierActionInventory screen, int page) {
        switch (screen) {
            case SET_PASSWORD:
                return fillSetPassword(inv);
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

    private static void fillNavigation(Inventory inv, int page, int playersSize) {
        if(page > 1) {
            inv.setItem(18, ItemsUtil.create(Material.YELLOW_WOOL, 1, String.format("Страница %d", page-1), List.of("Нажмите, чтобы", "Перелистнуть назад")));
        }
        inv.setItem(22, ItemsUtil.create(Material.PAPER, 1, "Страница № " + page));

        if(playersSize == 14) {
            inv.setItem(26, ItemsUtil.create(Material.ORANGE_WOOL, 1, String.format("Страница %d", page+1), List.of("Нажмите, чтобы", "Перелистнуть вперед")));
        }
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
            int playersSize = playerList.size();
            int ROWS = 2;
            int COLUMNS = 7;
            for (int i = 0; i < ROWS; i++) {
//                if (playersSize < COLUMNS * i) break;
                for (int j = 0; j < COLUMNS; j++) {

                        int playerIndex = j + COLUMNS * i;
                        if (playerIndex >= playersSize) {
                            // Если игроков больше нет, можно выйти из обоих циклов
                            // или просто прервать внутренний цикл
                            break;
                        }
                        LogUtil.warn(playerIndex + " " + i + " " + j + "/" + playersSize);
                        // Теперь расчёт слота: колонка j на строке i
                        int slot = j + (9 * i);

                        PlayerData playerData = playerList.get(playerIndex);
                        LogUtil.warn(playerData.name());
                        ItemStack item = ItemsUtil.create(Material.LIGHT_BLUE_WOOL,
                                1,
                                playerData.name(),
                                List.of("Добавить"));
                        inv.setItem(slot, item);
                    }

            }
            fillNavigation(inv, page, playersSize);
            return inv;
        }
        catch (SQLException e) {
            LogUtil.error("Ошибка получения списка игроков из бд", e);
            e.printStackTrace();
        }
        return null;
    }

    private static Inventory fillSetPassword(Inventory inv){
        ItemStack password_item = ItemsUtil.create(Material.ORANGE_WOOL,
                1,
                "1",
                List.of("Нажмите, чтобы", "изменить цифру"));
        for(int i = 11; i <= 15; i++){
            inv.setItem(i, password_item);
        }

        ItemStack accept_item = ItemsUtil.create(Material.LIME_STAINED_GLASS_PANE,
                1,
                "Сохранить пароль");
        inv.setItem(22, accept_item);

        return inv;
    }
}
