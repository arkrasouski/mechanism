package org.example.artyom.mechanism.listeners;

import org.apache.commons.logging.Log;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.example.artyom.mechanism.Mechanism;
import org.example.artyom.mechanism.inventories.GeneratorHolder;

import org.example.artyom.mechanism.inventories.MechanismHolder;
import org.example.artyom.mechanism.mechanism.MechanismManager;
import org.example.artyom.mechanism.mechanism.generator.Generator;
import org.example.artyom.mechanism.utils.EnergyUtil;
import org.example.artyom.mechanism.utils.LogUtil;
import org.example.artyom.mechanism.utils.MechanismStorageUtil;

import java.util.Map;

public class GeneratorListener implements Listener {

    private final MechanismManager generatorManager;
    private final NamespacedKey key;
    private final Map<Player, MechanismHolder> openedInventories;
    private final Mechanism plugin;

    public GeneratorListener(Mechanism plugin, MechanismManager generatorManager, Map<Player, MechanismHolder> openedInventories) {
        this.generatorManager = generatorManager;
        key = new NamespacedKey(plugin, "generator_items");
        this.openedInventories = openedInventories;
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteractInfo(PlayerInteractEvent event) {
        // Проверяем, что это ПКМ по блоку
        Player player = event.getPlayer();
        if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK && player.isSneaking())) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        // Проверяем, является ли блок генератором через generatorManager
        if(!generatorManager.isMechanism(block)) return;

        Generator generator = (Generator) generatorManager.getMechanism(block);
        if (generator == null) return;

        event.setCancelled(true);

        writeGeneratorInfoToPlayer(player, generator);
    }


    /**
     * При вызове инвентаря
     */
    @EventHandler
    public void onOpen(InventoryOpenEvent e) {
        if (!(e.getInventory().getHolder() instanceof MechanismHolder h)) return;
        //guiManager.addViewer(h.getLocation(), e.getPlayer().getUniqueId());
    }
    /**
     * Открытие инвентаря (дополнительного)
     */
    //TODO: Перенести в общий для механизмов листенер
    @EventHandler
    public void onInteractInventory(PlayerInteractEvent e) {
        // Проверяем, что это ПКМ по блоку
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = e.getClickedBlock();
        if (block == null) return;

        // Проверяем, не генератор ли
        if (!generatorManager.isMechanism(block)) return;

        Generator generator = (Generator) generatorManager.getMechanism(block);
        if (generator == null) return;

        e.setCancelled(true);

        Player player = e.getPlayer();

        int maxEnergy = generator.getMaxEnergyStorage();
        int currentEnergy = generator.getCurrentEnergy();

        GeneratorHolder generatorHolder = new GeneratorHolder(
                generator
        );
        
        openedInventories.put(player, generatorHolder);

        Inventory gui = generatorHolder.getInventory();

        LogUtil.warn("put to open inv");
        // Получаем TileState блока (для Dropper, Furnace и т.д.)
        if (!(block.getState() instanceof TileState tileState)) {
            player.sendMessage(ChatColor.RED + "Ошибка: блок не является TileState!");
            return;
        }

        //Если хранилище - восстанавливаем предметы из PDC
        if (block.getType() == Material.DROPPER || block.getType() == Material.HOPPER) {
            MechanismStorageUtil.loadItems(tileState, gui, key);
        }

        player.openInventory(gui);
    }

    /**
     * Закрытие инвентаря с сохранением в PDC
     */
    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getView().getTopInventory().getHolder() instanceof MechanismHolder holder)) return;

        BlockState blockState = holder.getLocation().getBlock().getState();
        if (blockState instanceof TileState tile) {
            MechanismStorageUtil.saveItems(tile, e.getView().getTopInventory(), key);
        }

        Player player = (Player) e.getPlayer();
        LogUtil.warn("removed from opened inv");
        openedInventories.remove(player);
    }

    @EventHandler
    public void onClickEnergySlot(InventoryClickEvent e) {
        Inventory top = e.getView().getTopInventory();
        if (!(top.getHolder() instanceof GeneratorHolder holder)) return;
        int slot = e.getSlot(); // индекс в верхнем инвентаре
        Location loc = holder.getLocation();
        Block block = loc.getBlock();
        BlockState state = block.getState();

            int topSize = top.getSize();
            if(e.getRawSlot() >= topSize) return;

            if (holder.isBlocked(slot)) {
                e.setCancelled(true);
            }
//            Bukkit.getScheduler().runTask(MagicMechanism.getInstance(), () -> {
//                ItemStack cell = top.getItem(9);
//
//                if (!(state instanceof TileState tile)) return;
//                Player p = (Player) e.getWhoClicked();
//
//                if (EnergyCell.isEnergyCell(cell)) {
//                    p.sendMessage("Аккумулятор вставлен в слот 1!");
//                    GeneratorCellService.onCellInserted(loc);
//                } else {
//                    GeneratorCellService.onCellRemoved(loc);
//                }
//                // КЛЮЧЕВОЕ: синхронизируем PDC сразу чтобы фоновые тики увидели аккумулятор
//                MechanismStorage.saveItems(tile, top, Keys.KEY_ITEMS);
//            });

    }
    //
//
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShiftToGenerator(InventoryClickEvent e) {
        Inventory top = e.getView().getTopInventory();
        if (!(top.getHolder() instanceof GeneratorHolder holder)) return;

        // Только shift-перенос
        if (e.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY) return;
        Location loc = holder.getLocation();
        Block block = loc.getBlock();

            Generator generator = (Generator) generatorManager.getMechanism(block);
            if (generator == null) return;
            int topSize = top.getSize();

            // Только shift-клик из НИЖНЕГО инвентаря (инвентарь игрока) -> вверх
            if (e.getRawSlot() < topSize) return;

            ItemStack moving = e.getCurrentItem();
            if (moving == null || moving.getType().isAir()) return;

            // Полностью отключаем ванильный перенос, дальше всё делаем вручную
            e.setCancelled(true);

            // Важно: работать с инвентарями лучше на следующем тике, чтобы ваниль/другие плагины не перетёрли изменения
            Bukkit.getScheduler().runTask(plugin, () -> {
                // Если игрок уже закрыл — выходим
                if (!(e.getWhoClicked() instanceof Player p)) return;
                if (p.getOpenInventory() == null) return;

                Inventory topNow = p.getOpenInventory().getTopInventory();
                if (topNow.getHolder() != holder) return; // игрок мог открыть другой GUI

                // Берём актуальный предмет из того же слота НИЖНЕГО инвентаря (куда кликнули)
                // rawSlot указывает на view; чтобы взять предмет "снизу", используем getClickedInventory в момент клика нельзя.
                // Поэтому проще: берём из bottom по e.getSlot() НЕЛЬЗЯ; используем вычисление через view.
                // Надёжный вариант: просто повторно читаем current item из bottom через rawSlot:
                ItemStack movingNow = p.getOpenInventory().getItem(e.getRawSlot());
                // В некоторых реализациях getItem(rawSlot) может вернуть null, тогда используем старое значение как fallback
                if (movingNow == null || movingNow.getType().isAir()) movingNow = moving.clone();

                int target = holder.findTargetSlot(topNow);
                if (target == -1) {
                    // нет разрешённых мест — предмет остаётся у игрока
                    return;
                }

                // Кладём 1:1 (как у тебя было). Если нужно стакание/частичный перенос — допишем отдельно.
                topNow.setItem(target, movingNow.clone());

                // Удаляем из инвентаря игрока то, что перенесли
                // Удаляем именно в rawSlot view
                p.getOpenInventory().setItem(e.getRawSlot(), null);

                // Дальше твоя доменная логика
                BlockState st = loc.getBlock().getState();
                if (st instanceof TileState tile) {
                    ItemStack cell = topNow.getItem(9);

//                    if (EnergyCell.isEnergyCell(cell)) {
//                        p.sendMessage("Аккумулятор вставлен в слот 1!");
//                        GeneratorCellService.onCellInserted(loc);
//                    } else {
//                        GeneratorCellService.onCellRemoved(loc);
//                    }

                    MechanismStorageUtil.saveItems(tile, topNow, key);
                }
            });

    }

    private void writeGeneratorInfoToPlayer(Player player, Generator generator) {
        int maxEnergyStorage = generator.getMaxEnergyStorage();
        int currentEnergy = generator.getCurrentEnergy();

        player.sendMessage(ChatColor.YELLOW + "⚡ Генератор ⚡");
        player.sendMessage(ChatColor.GRAY + "  Энергия: " + formatEnergy(currentEnergy, maxEnergyStorage));
        player.sendMessage(ChatColor.GRAY + "  Статус: " + (generator.isWorking() ? "§aАктивен" : "§cНеактивен"));
    }

    /**
     * Форматирует энергию для красивого отображения
     */
    private String formatEnergy(int current, int max) {
        double percent = (double) current / max * 100;
        String color;

        if (percent >= 75) color = "§a";
        else if (percent >= 50) color = "§e";
        else if (percent >= 25) color = "§6";
        else color = "§c";

        return color + current + "§7/§f" + max + " §7(" + String.format("%.1f", percent) + "%)";
    }

}
