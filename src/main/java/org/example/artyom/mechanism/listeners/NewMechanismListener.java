package org.example.artyom.mechanism.listeners;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.example.artyom.mechanism.Mechanism;
import org.example.artyom.mechanism.database.MechanismRepository;
import org.example.artyom.mechanism.database.NetworkRepository;
import org.example.artyom.mechanism.database.TransactionManager;
import org.example.artyom.mechanism.inventories.GeneratorHolder;
import org.example.artyom.mechanism.inventories.MechanismHolder;
import org.example.artyom.mechanism.items.BaseItem;
import org.example.artyom.mechanism.mechanism.MechanismManager;
import org.example.artyom.mechanism.mechanism.MechanismType;
import org.example.artyom.mechanism.mechanism.base.IConsumer;
import org.example.artyom.mechanism.mechanism.base.IProducer;
import org.example.artyom.mechanism.mechanism.base.Mech;
import org.example.artyom.mechanism.mechanism.generator.Generator;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;
import org.example.artyom.mechanism.mechanism.network.NetworkManager;
import org.example.artyom.mechanism.mechanism.network.NetworkSystems;
import org.example.artyom.mechanism.records.NetworkComponentData;
import org.example.artyom.mechanism.records.PlaceContext;
import org.example.artyom.mechanism.utils.*;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class NewMechanismListener implements Listener {
    private final Mechanism plugin;
    private final NetworkSystems networkSystems;
    private final TransactionManager transactionManager;
    private final NetworkRepository networkRepository;
    private final MechanismRepository mechanismRepository;
    private final NamespacedKey key;
    private final Map<Player, MechanismHolder> openedInventories;
    private final Map<Location, Player> encryptorOwners = new HashMap<>();

    public NewMechanismListener(Mechanism plugin,
                                NetworkSystems networkSystems,
                                TransactionManager transactionManager,
                                NetworkRepository networkRepository,
                                MechanismRepository mechanismRepository,
                                Map<Player, MechanismHolder> openedInventories) {
        this.plugin = plugin;
        this.networkSystems = networkSystems;
        this.transactionManager = transactionManager;
        this.networkRepository = networkRepository;
        this.mechanismRepository = mechanismRepository;
        key = new NamespacedKey(plugin, "mechanism_items");
        this.openedInventories = openedInventories;
    }

    /**
     * Ставим механизм
     */
    @EventHandler
    public void onMechanismPlace(BlockPlaceEvent event) {
        // Шаг 1: Валидация и подготовка
        PlaceContext context = ListenerUtil.validateAndPrepare(event, plugin, networkSystems);
        if (context == null) return;

        try {
            // Шаг 2: Создание механизма
            if (!ListenerUtil.createMechanism(context)) return;

            // Шаг 3: Обработка сетей
            ListenerUtil.handleNetworkLogic(context, networkSystems, transactionManager, networkRepository, mechanismRepository);

            // Шаг 4: Сохранение и финализация
            ListenerUtil.finalizePlacement(context);
        } catch (SQLException e) {
            ListenerUtil.handlePlacementError(context, e);
        }
    }

    /**
     * Ломаем механизм
     */
    @EventHandler
    public void onMechanismBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        Location loc = block.getLocation();

        MechanismType mechanismType = ListenerUtil.getMechanismType(block);
        if (mechanismType == null) return;
        MechanismManager manager = mechanismType.getMechanismManager();

        INetworkElement mechanism = manager.getMechanism(loc);
        if (mechanism == null) return;

        // Проверка инструмента
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!ToolUtil.canBreakWithTool(player, tool)) {
            event.setCancelled(true);
            player.sendMessage("§c " + mechanismType.getDisplayName() + " можно сломать только киркой!");
            return;
        }

        // 1. Сохраняем данные для восстановления
        Set<INetworkElement> neighbors = new HashSet<>(mechanism.getConnections());
        UUID oldNetworkId = mechanism.getNetworkId();
        NetworkManager oldNetwork = networkSystems.getNetworkManager(oldNetworkId);
        UUID oldOwnerId = oldNetwork != null ? oldNetwork.getOwner() : null;

        player.sendMessage("[Удаляю] Соседей: " + neighbors.size());

        // 2. Удаляем связи
        for (INetworkElement neighbor : neighbors) {
            neighbor.removeConnection(mechanism);
        }

        // 3. Находим компоненты связности
        Set<INetworkElement> unvisited = new HashSet<>(neighbors);
        List<Set<INetworkElement>> components = new ArrayList<>();

        while (!unvisited.isEmpty()) {
            INetworkElement start = unvisited.iterator().next();
            Set<INetworkElement> component = networkSystems.collectComponent(start);
            unvisited.removeAll(component);
            component.remove(mechanism);
            components.add(component);
        }

        // 4. Обрабатываем каждую компоненту с учётом барьеров
        List<NetworkComponentData> componentDataList = new ArrayList<>();

        for (Set<INetworkElement> component : components) {
            NetworkComponentData data = ListenerUtil.analyzeComponent(component, networkSystems);
            componentDataList.add(data);
        }

        // 5. Создаём NetworkManager для каждой компоненты
        List<NetworkManager> plannedManagers = new ArrayList<>();
        for (NetworkComponentData data : componentDataList) {
            Location compLoc = data.component().stream().iterator().next().getLocation();
            NetworkManager netManager = networkSystems.createDetachedNetwork(compLoc);
            netManager.setOwner(data.newOwnerId()); // Устанавливаем владельца
            networkSystems.addNetworkManager(netManager);
            plannedManagers.add(netManager);
        }

        try {
            // 6. Транзакция БД
            transactionManager.execute(connection -> {
                mechanismRepository.deleteMechanism(connection, mechanism.getLocation());

                for (int i = 0; i < components.size(); i++) {
                    NetworkManager newManager = plannedManagers.get(i);
                    Set<INetworkElement> component = components.get(i);
                    NetworkComponentData data = componentDataList.get(i);
                    newManager.setOwner(data.newOwnerId());
                    newManager.setPassword(data.password());
                    networkRepository.createNetwork(connection, newManager);
                    mechanismRepository.batchUpdateMechanismLocNetworks(connection, component, newManager.getNetworkId());
                }

                networkRepository.deleteNetwork(connection, oldNetworkId.toString());
                return true;
            });

            // 7. Удаляем механизм из памяти
            manager.deleteMechanism(loc);
            Map<UUID, List<INetworkElement>> mechanismMap = mechanism.getMechanismType().getMechsByNetwork();

            // 8. Привязываем элементы к новым сетям
            for (int i = 0; i < components.size(); i++) {
                NetworkManager newManager = plannedManagers.get(i);
                Set<INetworkElement> component = components.get(i);


                for (INetworkElement element : component) {
                    element.setNetworkId(newManager.getNetworkId());

                    newManager.addElement(element);
                    MechanismType type = element.getMechanismType();
                    Map<UUID, List<INetworkElement>> targetMap = type.getMechsByNetwork();
                    targetMap.computeIfAbsent(newManager.getNetworkId(), id -> new ArrayList<>())
                            .add(element);
                }
            }

            // 9. Удаляем старую сеть
            networkSystems.removeNetworkManager(oldNetworkId);
            for (MechanismType type : MechanismType.values()) {
                type.getMechsByNetwork().remove(oldNetworkId);
            }

            // 10. Визуальные эффекты и дроп
            ListenerUtil.spawnPlaceEffect(block);
            event.setDropItems(false);

            if (block.getState() instanceof Container cont) {
                cont.getInventory().clear();
                cont.update(true);
            }
            block.setType(Material.AIR);

            if (player.getGameMode() != GameMode.CREATIVE) {
                ItemStack mechanismItem = mechanismType.create(plugin).createItem(1);
                block.getWorld().dropItemNaturally(block.getLocation(), mechanismItem);
            }

        } catch (SQLException e) {
            event.setCancelled(true);

            // Восстанавливаем связи
            for (INetworkElement neighbor : neighbors) {
                neighbor.addConnection(mechanism);
            }

            // Удаляем созданные сети
            for (NetworkManager netManager : plannedManagers) {
                networkSystems.removeNetworkManager(netManager);
            }

            player.sendMessage("§cОшибка при сохранении механизма");
            e.printStackTrace();
        }
    }
    /**
     * Проверка блока при касании палочкой
     */
    @EventHandler
    public void onInteractByStick(PlayerInteractEvent event) {
        // Проверяем, что это ПКМ по блоку палкой
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || tool.getType() != Material.STICK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        MechanismType mechanismType = ListenerUtil.getMechanismType(block);
        if (mechanismType == null) return;
        MechanismManager manager = mechanismType.getMechanismManager();

        INetworkElement mechanism = manager.getMechanism(block.getLocation());
        if(mechanism == null) return;

        // Отменяем событие, чтобы не открывался ванильный интерфейс
        event.setCancelled(true);
        //Информация о сети
        ListenerUtil.showNetworkInfo(player, mechanism);
    }


    @EventHandler
    public void onInteractInfo(PlayerInteractEvent event) {
        // Проверяем, что это ПКМ по блоку
        Player player = event.getPlayer();
        if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK && player.isSneaking())) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        MechanismType mechanismType = ListenerUtil.getMechanismType(block);
        if (mechanismType == null) return;
        MechanismManager manager = mechanismType.getMechanismManager();

        INetworkElement element = manager.getMechanism(block);
        if (element == null) return;

        if(element instanceof Mech mechanism) {
            event.setCancelled(true);

            ListenerUtil.writeMechanismInfoToPlayer(player, mechanism);
        }
    }

    /**
     * Открытие инвентаря (дополнительного)
     */
    @EventHandler
    public void onInteractInventory(PlayerInteractEvent e) {
        // Проверяем, что это ПКМ по блоку
        Player player = e.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK ||
                player.isSneaking() ||
                item.getType() == Material.STICK) return;

        Block block = e.getClickedBlock();
        if (block == null) return;

        MechanismType mechanismType = ListenerUtil.getMechanismType(block);
        if (mechanismType == null) return;

        MechanismManager manager = mechanismType.getMechanismManager();

        INetworkElement element = manager.getMechanism(block);
        if (element == null) return;
        if (element instanceof Mech mechanism) {
            e.setCancelled(true);
            MechanismHolder holder = mechanismType.getMechanismHolder(mechanism, player);

            openedInventories.put(player, holder);

            Inventory gui = holder.getInventory();

            // Получаем TileState блока (для Dropper, Furnace и т.д.)
//            if (!(block.getState() instanceof TileState tileState)) {
//                player.sendMessage(ChatColor.RED + "Ошибка: блок не является TileState!");
//                return;
//            }

            //Если хранилище - восстанавливаем предметы из PDC
//            if (block.getType() == Material.DROPPER || block.getType() == Material.HOPPER) {
//                MechanismStorageUtil.loadItems(tileState, gui, key);
//            }

            player.openInventory(gui);
        }

    }

    /**
     * При вызове инвентаря
     */
    @EventHandler
    public void onOpen(InventoryOpenEvent e) {
        if (!(e.getInventory().getHolder() instanceof MechanismHolder holder)) return;
        //guiManager.addViewer(h.getLocation(), e.getPlayer().getUniqueId());
        Player player = (Player) e.getPlayer();

        if (holder.getMechanismType() == MechanismType.ENCODER) {
            Location loc = holder.getLocation();
            // Проверяем через дополнительный мап
            if (encryptorOwners.containsKey(loc)) {
                Player owner = encryptorOwners.get(loc);
                if (owner != null && owner.isOnline() && !owner.equals(player)) {
                    player.sendMessage("§cШифратор уже использует §e" + owner.getName());
                    e.setCancelled(true);
                    return;
                }
            }
            // Занимаем шифратор
            encryptorOwners.put(loc, player);
        }

        holder.updateEnergyBar();
        openedInventories.put(player, holder);
    }

    /**
     * Закрытие инвентаря с сохранением в PDC
     */
    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getView().getTopInventory().getHolder() instanceof MechanismHolder holder)) return;
        Player player = (Player) e.getPlayer();
//        BlockState blockState = holder.getLocation().getBlock().getState();
//        if (blockState instanceof TileState tile) {
//            MechanismStorageUtil.saveItems(tile, e.getView().getTopInventory(), key);
//        }

        if (holder.getMechanismType() == MechanismType.ENCODER) {
            Location loc = holder.getLocation();
            if(encryptorOwners.get(loc) == player) {
                encryptorOwners.remove(loc);
            }
        }

        openedInventories.remove(player);
    }

    @EventHandler
    public void onClickInventory(InventoryClickEvent e){
        Inventory top = e.getView().getTopInventory();
        if (!(top.getHolder() instanceof MechanismHolder holder)) return;

        int topSize = top.getSize();

        // Только shift-клик из НИЖНЕГО инвентаря (инвентарь игрока) -> вверх
        if (e.getRawSlot() >= topSize) return;

        int slot = e.getSlot();

        if(holder.isBlocked(slot)){
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShiftToGenerator(InventoryClickEvent e) {
        Inventory top = e.getView().getTopInventory();
        if (!(top.getHolder() instanceof MechanismHolder holder)) return;
        // Только shift-перенос
        if (e.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY) return;
        Location loc = holder.getLocation();
        Block block = loc.getBlock();

        MechanismType mechanismType = ListenerUtil.getMechanismType(block);
        if (mechanismType == null) return;
        MechanismManager manager = mechanismType.getMechanismManager();


        INetworkElement mechanism = manager.getMechanism(block);
        if (mechanism == null) return;
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
}
