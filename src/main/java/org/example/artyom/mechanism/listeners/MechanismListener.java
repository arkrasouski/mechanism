package org.example.artyom.mechanism.listeners;

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
import org.example.artyom.mechanism.items.GeneratorItem;
import org.example.artyom.mechanism.mechanism.MechanismManager;
import org.example.artyom.mechanism.mechanism.MechanismType;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;
import org.example.artyom.mechanism.mechanism.base.IProducer;
import org.example.artyom.mechanism.mechanism.network.NetworkManager;
import org.example.artyom.mechanism.mechanism.network.NetworkSystems;
import org.example.artyom.mechanism.utils.BlockUtil;
import org.example.artyom.mechanism.utils.ToolUtil;

import java.util.HashSet;
import java.util.Set;

public class MechanismListener implements Listener {
    private final Mechanism plugin;
    private final MechanismManager manager;
    private final NetworkSystems networkSystems;
    private final MechanismType mechanismType;

    public MechanismListener(Mechanism plugin, MechanismManager manager, NetworkSystems networkSystems, MechanismType mechanismType) {
        this.plugin = plugin;
        this.manager = manager;
        this.networkSystems = networkSystems;
        this.mechanismType = mechanismType;
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

        if (!GeneratorItem.isGeneratorItem(plugin, item, mechanismType)) {return;}

        if (!canPlaceMechanism(block, player)) {
            event.setCancelled(true);
            player.sendMessage("§cНельзя установить " + mechanismType.getDisplayName() + " здесь!");
            return;
        }

        INetworkElement mechanism = mechanismType.create(loc);
        manager.registerMechanism(mechanism, loc);

        if (mechanism == null) {
            event.setCancelled(true);
            player.sendMessage("§cОшибка при создании " + mechanismType.getDisplayName());
            return;
        }
        //NetworkElement networkGen = new NetworkElement(loc);
        Set<INetworkElement> neighbors = new HashSet<>();
        Set<NetworkManager> connectedNetworks = new HashSet<>();
        // 6 сторон куба
        Location[] sides = BlockUtil.getSidesByLoc(loc);

        for(Location side : sides) {
            for (NetworkManager netManager : networkSystems.getNetworks()) {
                INetworkElement elem = netManager.getElement(side);
                if (elem != null) {
                    neighbors.add(elem);
                    connectedNetworks.add(netManager);
                }
            }
        }
        if(neighbors.isEmpty()) {
            NetworkManager networkManager =  networkSystems.addNetworkManager();
            NetworkRepository.createNetwork(networkManager);
            //Если элемент один, создаем ему сеть и в бд его
            networkManager.addElement(mechanism);
            MechanismRepository.addMechanism(null, mechanism);
            player.sendMessage("Создаю новую сеть!");
        }
        else {
            networkSystems.mergeNetworksAndAddElement(
                    mechanism,
                    connectedNetworks,
                    player
            );
        }
        // ШАГ 5: Сообщение игроку
        player.sendMessage("§a✓ " + mechanismType.getDisplayName() + " успешно установлен!");

        // ШАГ 6: Визуальный эффект
        spawnPlaceEffect(block);
    }

    /**
     * Ломаем генератор
     */
//    @EventHandler
//    public void onMechanismBreak(BlockBreakEvent event) {
//        Block block = event.getBlock();
//        Player player = event.getPlayer();
//        Location loc = block.getLocation();
//
//        INetworkElement mechanism = manager.getMechanism(loc);
//        // Проверяем, является ли сломанный блок генератором
//        if(mechanism == null) return;
//
//        ItemStack tool = player.getInventory().getItemInMainHand();
//        if (!ToolUtil.canBreakWithTool(player, tool)) {
//            event.setCancelled(true);
//            player.sendMessage("§c " + mechanismType.getDisplayName() + " можно сломать только киркой!");
//            return;
//        }
//
//        Set<INetworkElement> neighbors = mechanism.getConnections();
//
//        for(INetworkElement neighbor : neighbors) {
//            neighbor.removeConnection(mechanism);
//        }
//        manager.deleteMechanism(loc);
//
//        Set<INetworkElement> unvisited  = new HashSet<>(neighbors);
//
//        //Пока ещё остались узлы, которые мы не обработали, продолжаем искать следующую компоненту.
//        while(!unvisited.isEmpty()){
//            //Берём любой один узел из множества unvisited как стартовую точку обхода.
//            INetworkElement start = unvisited.iterator().next();
//            //Запускаем BFS/DFS от этого узла и собираем все узлы, которые с ним связаны.
//            //В результате получаем одну группу — одну подсеть.
//            Set<INetworkElement> component = networkSystems.collectComponent(start);
//            //Удаляем из unvisited все узлы, которые уже вошли в найденную компоненту.
//            //То есть помечаем их как обработанные.
//            unvisited.removeAll(component);
//            //Создаём новый менеджер сети для этой найденной компоненты.
//            NetworkManager newNetworkManager = networkSystems.addNetworkManager();
//            NetworkRepository.createNetwork(newNetworkManager);
//            for(INetworkElement element : component) {
//                newNetworkManager.addElement(element.getMechanismType(), element);
//            }
//        }
//
//        //Удаляем старую сеть и все типы механизмов из нее
//        NetworkManager netManager = networkSystems.getNetworkManager(mechanism.getNetworkId());
//        for (MechanismType mechanismType : MechanismType.values()) {
//            mechanismType.removeFromPreviousNetwork(mechanism.getNetworkId());
//        }
//        networkSystems.removeNetworkManager(netManager);
//
//        spawnPlaceEffect(block);
//        player.sendMessage("§c " + mechanismType.getDisplayName() + " разрушен!");
//
//        // Отменяем обычный дроп
//        event.setDropItems(false);
//        if (block.getState() instanceof Container cont) {
//            cont.getInventory().clear();
//            cont.update(true);
//        }
//        // Удаляем блок
//        block.setType(Material.AIR);
//
//        // Дропаем предмет генератора
//        ItemStack mechanismItem = mechanismType.create(plugin).createItem(1);
//        block.getWorld().dropItemNaturally(block.getLocation(), mechanismItem);
//    }

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
        NetworkManager netManager = networkSystems.getNetworkManager(netElem.getNetworkId());

        player.sendMessage("§6=== Информация о сети ===");
        player.sendMessage("§7ID сети: §f" + netManager.getNetworkId());
        player.sendMessage("§7Локация элемента: §f" + loc);
        player.sendMessage("§7Компонентов: §f" + netManager.getElements().size());

        // Дополнительная информация (если есть доступ к конкретным множествам)
        if (netElem instanceof IProducer) {
            player.sendMessage("Это генератор!");
            //player.sendMessage("§7  Валидна: " + (enet.isValid() ? "§a✓" : "§c✗"));
        }
        else {
            player.sendMessage("Это кабель!");
        }

        int generatorCount = 0;
        int cableCount = 0;
        for (INetworkElement elem : netManager.getElements()) {
            if(elem instanceof IProducer) {
                generatorCount++;
            }
            else {
                cableCount++;
            }

        }
        player.sendMessage("§7Всего: " + generatorCount + " Генераторов" );
        player.sendMessage("§7Всего: " + cableCount + " Кабелей");
    }


    /**
     * Эффект спавна генератора
     */
    private void spawnPlaceEffect(Block block) {
        block.getWorld().playSound(block.getLocation(),
                org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 1.5f);
        block.getWorld().spawnParticle(org.bukkit.Particle.PORTAL,
                block.getLocation().add(0.5, 1, 0.5), 20, 0.3, 0.3, 0.3, 0.1);
    }

    /**
     * Проверяет, можно ли ставить здесь генератор
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
