package org.example.artyom.mechanism.inventories;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.example.artyom.mechanism.utils.ItemsUtil;

import java.util.List;

public class BarrierMenuFactory {

    public static Inventory create(MechanismHolder holder, int size, String glif) {
        Inventory inv = Bukkit.createInventory(holder, size, glif);

        fillBase(inv, holder);
        fillPageContent(inv, BarrierActionInventory.MAIN_MENU);
        //fillNavigation(inv, holder.getPage());

        return inv;
    }

    private static void fillBase(Inventory inv, MechanismHolder holder) {
        // фон, инфо, декоративные элементы
    }

    private static void fillPageContent(Inventory inv, BarrierActionInventory screen) {
        switch (screen) {
            case MAIN_MENU:
                fillMainMenu(inv);
                break;
            case PLAYER_LIST:

                break;
            case PLAYER_SETTINGS:

                break;
            default:
                fillMainMenu(inv);
        }
    }

    private static void fillNavigation(Inventory inv, int page) {
//        inv.setItem(27, previousButton(page));
//        inv.setItem(31, closeButton());
//        inv.setItem(35, nextButton(page));
    }

    private static void fillMainMenu(Inventory inv) { //флаг чтобы отличать первый раз открываем или нет
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
    }
}
