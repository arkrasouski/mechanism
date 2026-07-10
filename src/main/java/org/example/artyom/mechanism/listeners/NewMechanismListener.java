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
import org.example.artyom.mechanism.utils.*;

import java.sql.SQLException;
import java.util.*;

public class NewMechanismListener implements Listener {
    private final Mechanism plugin;
    private final NetworkSystems networkSystems;
    private final TransactionManager transactionManager;
    private final NetworkRepository networkRepository;
    private final MechanismRepository mechanismRepository;
    private final NamespacedKey key;
    private final Map<Player, MechanismHolder> openedInventories;

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
        Block block = event.getBlock();
        Location loc = block.getLocation();
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();

        if(!item.hasItemMeta()) return;
        //TODO::Добавить также отличие от других плагинов/предметов

        MechanismType mechanismType = ListenerUtil.getMechanismType(block);
        if (mechanismType == null) return;
        MechanismManager manager = mechanismType.getMechanismManager();

        if (!ListenerUtil.canPlaceMechanism(block, player)) {
            event.setCancelled(true);
            player.sendMessage("§cНельзя установить " + mechanismType.getDisplayName() + " здесь!");
            return;
        }

        INetworkElement mechanism = mechanismType.create(loc);
        if (mechanism == null) {
            event.setCancelled(true);
            player.sendMessage("§cОшибка при создании " + mechanismType.name());
            return;
        }

        Set<NetworkManager> connectedNetworks = new HashSet<>();
        // 6 сторон куба
        Location[] sides = BlockUtil.getSidesByLoc(loc);

        for(Location side : sides) {
            for (NetworkManager netManager : networkSystems.getNetworks()) {
                INetworkElement elem = netManager.getElement(side);
                if (elem != null) {
                    player.sendMessage("сеть" + netManager.getNetworkId());
                    connectedNetworks.add(netManager);
                }
            }
        }

        try {
            Map<UUID, List<INetworkElement>> mechanismMap = mechanism.getMechanismType().getMechsByNetwork();
            //Здесь создается новая сеть
            if(connectedNetworks.isEmpty()) {
                NetworkManager networkManager =  networkSystems.createDetachedNetwork(mechanism.getLocation());
                UUID networkId = networkManager.getNetworkId();
                mechanism.setNetworkId(networkId);
                transactionManager.execute(connection -> {
                    networkRepository.createNetwork(connection, networkManager);
                    mechanismRepository.addMechanism(connection, mechanism);
                    return true;
                });
                networkManager.addElement(mechanism);
                networkSystems.addNetworkManager(networkManager);
                manager.registerMechanism(mechanism, loc);
                List<INetworkElement> elements = new ArrayList<>();
                elements.add(mechanism);
                mechanismMap.put(networkId, elements);
                player.sendMessage("Создаю новую сеть!");
            }
            //Здесь склейка сетей
            else {
                //Может не вернуть ничего если пустые connectedNetworks
                NetworkManager primaryNetwork = connectedNetworks.stream()
                        .max(Comparator.comparingInt(n -> n.getElements().size()))
                        .orElseThrow();

                List<NetworkManager> secondaryNetworks = connectedNetworks.stream()
                        .filter(n -> n != primaryNetwork)
                        .toList();
                UUID primaryId = primaryNetwork.getNetworkId();
                List<UUID> secondaryIds = secondaryNetworks.stream().map(NetworkManager::getNetworkId).toList();
                mechanism.setNetworkId(primaryId);

                transactionManager.execute(connection -> {
                    mechanismRepository.addMechanism(connection, mechanism);
                    mechanismRepository.batchUpdateMechanismNetworks(connection, primaryId, secondaryIds);
                    networkRepository.deleteSecondaryNetworks(connection, secondaryIds);
                    return true;
                });
                primaryNetwork.addElement(mechanism);

                for (NetworkManager secondary : secondaryNetworks) {
                    for (INetworkElement element : secondary.getElements()) {
                        element.setNetworkId(primaryId);
                        primaryNetwork.addElement(element);

                        MechanismType type = element.getMechanismType();
                        Map<UUID, List<INetworkElement>> targetMap = type.getMechsByNetwork();
                        targetMap.computeIfAbsent(primaryId, id -> new ArrayList<>())
                                .add(element);


                    }
                    networkSystems.removeNetworkManager(secondary);
                    for(MechanismType type : MechanismType.values()){
                        type.getMechsByNetwork().remove(secondary.getNetworkId());
                    }
                }

                mechanismMap.computeIfAbsent(primaryId, id -> new ArrayList<>())
                        .add(mechanism);

                manager.registerMechanism(mechanism, loc);
                player.sendMessage("✓ Объединено " + (secondaryNetworks.size() + 1) + " сетей");
            }
            // ШАГ 5: Сообщение игроку
            player.sendMessage("§a✓ " + mechanismType.getDisplayName() + " успешно установлен!");

            // ШАГ 6: Визуальный эффект
            ListenerUtil.spawnPlaceEffect(block);
        } catch (SQLException e) {
            event.setCancelled(true);
            player.sendMessage("§cОшибка при сохранении механизма");
            e.printStackTrace();
        }
    }

    /**
     * Ломаем генератор
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
        if(mechanism == null) return;

        // Проверяем, является ли сломанный блок механизмом
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!ToolUtil.canBreakWithTool(player, tool)) {
            event.setCancelled(true);
            player.sendMessage("§c " + mechanismType.getDisplayName() + " можно сломать только киркой!");
            return;
        }
        // 1. Сохраняем данные для восстановления
        Set<INetworkElement> neighbors = new HashSet<>(mechanism.getConnections());
        UUID oldNetworkId = mechanism.getNetworkId();

        player.sendMessage("[Удаляю] Соседей: " + neighbors.size());
        Set<INetworkElement> unvisited = new HashSet<>(neighbors);

        // 2. Удаляем связи (Внимание! если их тут не удалить, компоненты будут хранить связь с механизмом)
        for (INetworkElement neighbor : neighbors) {
            neighbor.removeConnection(mechanism);
        }

        //Сохраняем компоненты которые станут сетями
        List<Set<INetworkElement>> components = new ArrayList<>();
        while (!unvisited.isEmpty()) {
            INetworkElement start = unvisited.iterator().next();
            Set<INetworkElement> component = networkSystems.collectComponent(start);
            unvisited.removeAll(component);
            component.remove(mechanism);
            components.add(component);
        }

        //Сохраняем сети которые создаем для обновления
        List<NetworkManager> plannedManagers = new ArrayList<>();
        for (Set<INetworkElement> component : components) {
            Location compLoc = component.stream().iterator().next().getLocation();
            NetworkManager netManager = networkSystems.createDetachedNetwork(compLoc);
            networkSystems.addNetworkManager(netManager);
            plannedManagers.add(netManager);
        }
        try {
            transactionManager.execute(connection -> {
                mechanismRepository.deleteMechanism(connection, mechanism.getLocation());
                for (int i = 0; i < components.size(); i++) {
                    NetworkManager newManager = plannedManagers.get(i);
                    Set<INetworkElement> component = components.get(i);
                    //пишем сеть в бд
                    networkRepository.createNetwork(connection, newManager);
                    //обновляем механизмы в бд
                    mechanismRepository.batchUpdateMechanismLocNetworks(connection, component, newManager.getNetworkId());
                }

                networkRepository.deleteNetwork(connection, oldNetworkId.toString());
                return true;
            });
            // 3. Удаляем механизм
            manager.deleteMechanism(loc);
            Map<UUID, List<INetworkElement>> mechanismMap = mechanism.getMechanismType().getMechsByNetwork();
            //Устанавливаем элементы к определенной сети
            for (int i = 0; i < components.size(); i++) {
                NetworkManager newManager = plannedManagers.get(i);
                UUID newNetworkIid = newManager.getNetworkId();

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

            networkSystems.removeNetworkManager(oldNetworkId);
            for(MechanismType type : MechanismType.values()) {
                type.getMechsByNetwork().remove(oldNetworkId);
            }

            // 6. Обновляем блок
            ListenerUtil.spawnPlaceEffect(block);
            event.setDropItems(false);
            //Очищаем инвентарь
            if (block.getState() instanceof Container cont) {
                cont.getInventory().clear();
                cont.update(true);
            }
            block.setType(Material.AIR);

            // 7. Дропаем предмет
            if (player.getGameMode() != GameMode.CREATIVE) {
                ItemStack mechanismItem = mechanismType.create(plugin).createItem(1);
                block.getWorld().dropItemNaturally(block.getLocation(), mechanismItem);
            }
        } catch (SQLException e) {
            event.setCancelled(true);

            //Восстановить связи между соседями и механизмом в случае неудачи
            for (INetworkElement neighbor : neighbors) {
                neighbor.addConnection(mechanism);
            }

            //Удалить созданные сети в случае неудачи
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
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = e.getClickedBlock();
        if (block == null) return;

        MechanismType mechanismType = ListenerUtil.getMechanismType(block);
        if (mechanismType == null) return;

        MechanismManager manager = mechanismType.getMechanismManager();

        INetworkElement element = manager.getMechanism(block);
        if (element == null) return;
        if (element instanceof Mech mechanism) {
            e.setCancelled(true);

            Player player = e.getPlayer();

            MechanismHolder holder = mechanismType.getMechanismHolder(mechanism);
            holder.updateEnergyBar();

            openedInventories.put(player, holder);

            Inventory gui = holder.getInventory();

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
        openedInventories.remove(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShiftToGenerator(InventoryClickEvent e) {
        Inventory top = e.getView().getTopInventory();
        if (!(top.getHolder() instanceof GeneratorHolder holder)) return;

        // Только shift-перенос
        if (e.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY) return;
        Location loc = holder.getLocation();
        Block block = loc.getBlock();

        MechanismType mechanismType = ListenerUtil.getMechanismType(block);
        if (mechanismType == null) return;
        MechanismManager manager = mechanismType.getMechanismManager();

        Generator generator = (Generator) manager.getMechanism(block);
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
}
