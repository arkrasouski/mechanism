package org.example.artyom.mechanism.listeners;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.example.artyom.mechanism.Mechanism;
import org.example.artyom.mechanism.database.MechanismRepository;
import org.example.artyom.mechanism.database.NetworkRepository;
import org.example.artyom.mechanism.database.TransactionManager;
import org.example.artyom.mechanism.items.BaseItem;
import org.example.artyom.mechanism.mechanism.MechanismManager;
import org.example.artyom.mechanism.mechanism.MechanismType;
import org.example.artyom.mechanism.mechanism.base.IConsumer;
import org.example.artyom.mechanism.mechanism.base.IProducer;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;
import org.example.artyom.mechanism.mechanism.network.NetworkManager;
import org.example.artyom.mechanism.mechanism.network.NetworkSystems;
import org.example.artyom.mechanism.utils.BlockUtil;
import org.example.artyom.mechanism.utils.ToolUtil;

import java.sql.SQLException;
import java.util.*;

public class NewMechanismListener implements Listener {
    private final Mechanism plugin;
    private final NetworkSystems networkSystems;
    private final TransactionManager transactionManager;
    private final NetworkRepository networkRepository;
    private final MechanismRepository mechanismRepository;

    public NewMechanismListener(Mechanism plugin,
                                NetworkSystems networkSystems,
                                TransactionManager transactionManager,
                                NetworkRepository networkRepository,
                                MechanismRepository mechanismRepository) {
        this.plugin = plugin;
        this.networkSystems = networkSystems;
        this.transactionManager = transactionManager;
        this.networkRepository = networkRepository;
        this.mechanismRepository = mechanismRepository;
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

        MechanismManager manager = null;
        MechanismType mechanismType = null;

        for(MechanismType type : MechanismType.values()){
            if(BaseItem.isMechanismItem(plugin, item, type)){
                manager = type.getMechanismManager();
                mechanismType = type;
            }
        }
        if(manager == null) return;

        if (!canPlaceMechanism(block, player)) {
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
            spawnPlaceEffect(block);
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

        MechanismManager manager = null;
        MechanismType mechanismType = null;

        for(MechanismType type : MechanismType.values()){
            manager = type.getMechanismManager();
            if(manager.isMechanism(block)){
                mechanismType = type;
            }
        }
        if(mechanismType == null) return;

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
            spawnPlaceEffect(block);
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

        MechanismManager manager = null;
        MechanismType mechanismType = null;

        for(MechanismType type : MechanismType.values()){
            manager = type.getMechanismManager();
            if(manager.isMechanism(block)){
                mechanismType = type;
            }
        }
        if(mechanismType == null) return;

        INetworkElement mechanism = manager.getMechanism(block.getLocation());
        if(mechanism == null) return;

        // Отменяем событие, чтобы не открывался ванильный интерфейс
        event.setCancelled(true);
        //Информация о сети
        showNetworkInfo(player, mechanism);
    }

    /**
     * Печать информации по графу
     */
    private void showNetworkInfo(Player player, INetworkElement netElem) {
        Location loc = netElem.getLocation();
        UUID networkId = netElem.getNetworkId();

        NetworkManager netManager = networkSystems.getNetworkManager(networkId);
        player.sendMessage("§6=== Информация о сети ===");
        player.sendMessage("§7ID сети: §f" + networkId);
        player.sendMessage("§7Локация элемента: §f" + loc);
        player.sendMessage("§7Компонентов: §f" + netManager.getElements().size());

        // Дополнительная информация (если есть доступ к конкретным множествам)
        if (netElem instanceof IProducer) {
            player.sendMessage("Это генератор!");
        }
        else if (netElem instanceof IConsumer){
            player.sendMessage("Это барьер!");
        } else {
            player.sendMessage("Это кабель!");
        }

        int generatorCount = 0;
        int cableCount = 0;
        int barrierCount = 0;
        for (INetworkElement elem : netManager.getElements()) {
            if(elem instanceof IProducer) {
                generatorCount++;
            }
            else if (elem instanceof IConsumer) {
                barrierCount++;
            }
            else {
                cableCount++;
            }

        }
        player.sendMessage("§7Всего: " + generatorCount + " Генераторов" );
        player.sendMessage("§7Всего: " + cableCount + " Кабелей");
        player.sendMessage("§7Всего: " + barrierCount + " Барьеров");
    }


    /**
     * Эффект спавна механизма
     */
    private void spawnPlaceEffect(Block block) {
        block.getWorld().playSound(block.getLocation(),
                org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 1.5f);
        block.getWorld().spawnParticle(org.bukkit.Particle.PORTAL,
                block.getLocation().add(0.5, 1, 0.5), 20, 0.3, 0.3, 0.3, 0.1);
    }
    /**
     * Проверяет, можно ли ставить здесь механизм
     */
    private boolean canPlaceMechanism(Block block, Player player) {
        // Проверка на пустой блок
        return block.getType() == Material.AIR || !BlockUtil.isReplaceableBlock(block);

        // Проверка на наличие другого генератора
//!mechanismManager.isMechanism(block);

        // Проверка прав
//player.hasPermission("generator.place");
    }
}
