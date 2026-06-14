package org.example.artyom.mechanism.listeners.generator;

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
import org.example.artyom.mechanism.items.GeneratorItem;
import org.example.artyom.mechanism.mechanism.MechanismType;
import org.example.artyom.mechanism.mechanism.generator.Generator;
import org.example.artyom.mechanism.mechanism.generator.GeneratorManager;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;
import org.example.artyom.mechanism.mechanism.network.INetworkProducer;
import org.example.artyom.mechanism.mechanism.network.NetworkManager;
import org.example.artyom.mechanism.mechanism.network.NetworkSystems;
import org.example.artyom.mechanism.utils.BlockUtil;
import org.example.artyom.mechanism.utils.ToolUtil;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class GeneratorListener implements Listener {
    private final Mechanism plugin;
    private final GeneratorManager manager;
    private final NetworkSystems networkSystems;

    public GeneratorListener(Mechanism plugin, GeneratorManager manager, NetworkSystems networkSystems) {
        this.plugin = plugin;
        this.manager = manager;
        this.networkSystems = networkSystems;
    }

    /**
     * Ставим генератор
     */
    @EventHandler
    public void onMechanismPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();

        if (!GeneratorItem.isGeneratorItem(plugin, item, MechanismType.GENERATOR)) {return;}

        if (!canPlaceMechanism(block, player)) {
            event.setCancelled(true);
            player.sendMessage("§cНельзя установить генератор здесь!");
            return;
        }
        Generator generator = manager.createGenerator(loc, player);
        if (generator == null) {
            event.setCancelled(true);
            player.sendMessage("§cОшибка при создании Генератора");
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
            networkManager.addElement(generator);
            player.sendMessage("Создаю новую сеть!");
        }
        else {
            networkSystems.mergeNetworksAndAddElement(
                    generator,
                    connectedNetworks,
                    player
            );
        }
        // ШАГ 5: Сообщение игроку
        player.sendMessage("§a✓ Генератор успешно установлен!");

        // ШАГ 6: Визуальный эффект
        spawnPlaceEffect(block);
    }

    /**
     * Ломаем генератор
     */
    @EventHandler
    public void onMechanismBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        Location loc = block.getLocation();

        Generator generator = manager.getGenerator(loc);
        // Проверяем, является ли сломанный блок генератором
        if(generator == null) return;

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!ToolUtil.canBreakWithTool(player, tool)) {
            event.setCancelled(true);
            player.sendMessage("§c Генератор можно сломать только киркой!");
            return;
        }

        Set<INetworkElement> neighbors = generator.getConnections();

        for(INetworkElement neighbor : neighbors) {
            neighbor.removeConnection(generator);
        }
        manager.deleteGenerator(loc);

        Set<INetworkElement> unvisited  = new HashSet<>(neighbors);

        //Пока ещё остались узлы, которые мы не обработали, продолжаем искать следующую компоненту.
        while(!unvisited.isEmpty()){
            //Берём любой один узел из множества unvisited как стартовую точку обхода.
            INetworkElement start = unvisited.iterator().next();
            //Запускаем BFS/DFS от этого узла и собираем все узлы, которые с ним связаны.
            //В результате получаем одну группу — одну подсеть.
            Set<INetworkElement> component = networkSystems.collectComponent(start);
            //Удаляем из unvisited все узлы, которые уже вошли в найденную компоненту.
            //То есть помечаем их как обработанные.
            unvisited.removeAll(component);
            //Создаём новый менеджер сети для этой найденной компоненты.
            NetworkManager newNetworkManager = networkSystems.addNetworkManager();
            for(INetworkElement element : component) {
                newNetworkManager.addElement(element);
            }
        }

        //Удаляем старую сеть
        NetworkManager netManager = networkSystems.getNetworkManager(loc);
        networkSystems.removeNetworkManager(netManager);

//        NetworkManager netManager = networkSystems.getNetworkManager(loc);
//
//        for (INetworkElement neighbor : neighbors) {
//            neighbor.removeConnection(generator);
//            NetworkManager newNetManager = networkSystems.addNetworkManager();
//            newNetManager.addElement(neighbor);
//            if(!neighbor.getConnections().isEmpty()){
//                Set<INetworkElement> newNeighbors = neighbor.getConnections();
//                for(INetworkElement newNeighbor : newNeighbors){
//                    newNetManager.addElement(newNeighbor);
//                }
//            }
//        }
//        // Удаляем генератор
//        manager.deleteGenerator(loc);
//
//        //Удаляем старую сеть
//        networkSystems.removeNetworkManager(netManager);
        spawnPlaceEffect(block);
        event.getPlayer().sendMessage("§c Генератор разрушен!");

        // Отменяем обычный дроп
        event.setDropItems(false);
        if (block.getState() instanceof Container cont) {
            cont.getInventory().clear();
            cont.update(true);
        }
        // Удаляем блок
        block.setType(Material.AIR);

        // Дропаем предмет генератора
        ItemStack mechanismItem = new GeneratorItem(plugin).createItem(1);
        block.getWorld().dropItemNaturally(block.getLocation(), mechanismItem);
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

        Generator generator = manager.getGenerator(block.getLocation());
        if(generator == null) return;

        // Отменяем событие, чтобы не открывался ванильный интерфейс
        event.setCancelled(true);

        //Информация о сети
        showNetworkInfo(player, generator);
    }

    /**
     * Печать информации по графу
     */
    private void showNetworkInfo(Player player, INetworkElement netElem) {
        Location loc = netElem.getLocation();
        NetworkManager netManager = networkSystems.getNetworkManager(loc);

        player.sendMessage("§6=== Информация о сети ===");
        player.sendMessage("§7ID сети: §f" + netManager.getNetworkId());
        player.sendMessage("§7Локация элемента: §f" + loc);
        player.sendMessage("§7Компонентов: §f" + netManager.getElements().size());

        // Дополнительная информация (если есть доступ к конкретным множествам)
        if (netElem instanceof INetworkProducer) {
            player.sendMessage("Это генератор!");

            //player.sendMessage("§7  Валидна: " + (enet.isValid() ? "§a✓" : "§c✗"));
        }
        int generatorCount = 0;
        for (INetworkElement elem : netManager.getElements()) {
            if(elem instanceof INetworkProducer) {
                generatorCount++;
            }

        }
        player.sendMessage("§7Всего: " + generatorCount + " Генераторов" );
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
