package org.example.artyom.mechanism.utils.mechanism_listener;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.example.artyom.mechanism.Mechanism;
import org.example.artyom.mechanism.database.MechanismRepository;
import org.example.artyom.mechanism.database.NetworkRepository;
import org.example.artyom.mechanism.database.TransactionManager;
import org.example.artyom.mechanism.mechanism.MechanismManager;
import org.example.artyom.mechanism.mechanism.MechanismType;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;
import org.example.artyom.mechanism.mechanism.network.NetworkManager;
import org.example.artyom.mechanism.mechanism.network.NetworkSystems;
import org.example.artyom.mechanism.records.BreakContext;
import org.example.artyom.mechanism.records.NetworkComponentData;
import org.example.artyom.mechanism.records.NetworkSplitResult;
import org.example.artyom.mechanism.utils.ToolUtil;

import java.sql.SQLException;
import java.util.*;

import static org.example.artyom.mechanism.Mechanism.getNetworkSystems;

public class BreakUtil {
    /**
     * Анализирует компоненту сети и определяет нового владельца
     * @param component Компонента механизмов
     * @param networkSystems Все сети
     * @return Данные о компоненте (новый владелец)
     */
    public static NetworkComponentData analyzeComponent(Set<INetworkElement> component, NetworkSystems networkSystems) {
        // Ищем все барьеры в компоненте
        List<INetworkElement> barriers = component.stream()
                .filter(e -> e.getMechanismType() == MechanismType.BARRIER)
                .toList();

        UUID newOwnerId;
        boolean hasOwner;
        int password;

        if (barriers.isEmpty()) {
            // Нет барьеров — сеть без владельца
            newOwnerId = null;
            hasOwner = false;
            password = -1;
        } else {
            // Несколько барьеров — берём владельца первого найденного
            // (можно добавить логику приоритета: например, ближайший к игроку)
            INetworkElement primaryBarrier = barriers.getFirst();
            NetworkManager networkManager = getNetworkSystems().getNetworkManager(primaryBarrier.getNetworkId());
            newOwnerId = networkManager.getOwner();
            hasOwner = newOwnerId != null;
            password = networkManager.getPassword();
        }

        return new NetworkComponentData(component, newOwnerId, hasOwner, password);
    }

    public static BreakContext createContext(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        MechanismType mechanismType = CommonUtil.getMechanismType(block);
        if (mechanismType == null) return null;

        MechanismManager manager = mechanismType.getMechanismManager();
        INetworkElement mechanism = manager.getMechanism(block.getLocation());
        if (mechanism == null) return null;

        return new BreakContext(block, player, mechanismType, manager, mechanism);
    }

    public static boolean validateTool(Player player, MechanismType mechanismType) {
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!ToolUtil.canBreakWithTool(player, tool)) {
            player.sendMessage("§c " + mechanismType.getDisplayName() + " можно сломать только киркой!");
            return false;
        }
        return true;
    }

    public static NetworkSplitResult splitNetwork(INetworkElement mechanism, NetworkSystems networkSystems) {
        Set<INetworkElement> neighbors = new HashSet<>(mechanism.getConnections());
        UUID oldNetworkId = mechanism.getNetworkId();
        NetworkManager oldNetwork = networkSystems.getNetworkManager(oldNetworkId);

        // Удаляем связи
        for (INetworkElement neighbor : neighbors) {
            neighbor.removeConnection(mechanism);
        }

        // Находим компоненты связности
        Set<INetworkElement> unvisited = new HashSet<>(neighbors);
        List<Set<INetworkElement>> components = new ArrayList<>();

        while (!unvisited.isEmpty()) {
            INetworkElement start = unvisited.iterator().next();
            Set<INetworkElement> component = networkSystems.collectComponent(start);
            unvisited.removeAll(component);
            component.remove(mechanism);
            components.add(component);
        }

        // Анализируем компоненты
        List<NetworkComponentData> componentData = components.stream()
                .map(comp -> analyzeComponent(comp, networkSystems))
                .toList();

        // Создаём новые менеджеры
        List<NetworkManager> newManagers = componentData.stream()
                .map(data -> {
                    Location loc = data.component().stream().iterator().next().getLocation();
                    NetworkManager manager = networkSystems.createDetachedNetwork(loc);
                    manager.setOwner(data.newOwnerId());
                    manager.setPassword(data.password());
                    networkSystems.addNetworkManager(manager);
                    return manager;
                })
                .toList();

        return new NetworkSplitResult(
                oldNetworkId, oldNetwork, neighbors,
                newManagers, components, componentData
        );
    }
    public static void persistSplitToDatabase(
            NetworkSplitResult result,
            INetworkElement mechanism,
            TransactionManager transactionManager,
            MechanismRepository mechanismRepository,
            NetworkRepository networkRepository
    ) throws SQLException {
        transactionManager.execute(connection -> {
            mechanismRepository.deleteMechanism(connection, mechanism.getLocation());

            for (int i = 0; i < result.components().size(); i++) {
                NetworkManager manager = result.newManagers().get(i);
                Set<INetworkElement> component = result.components().get(i);

                networkRepository.createNetwork(connection, manager);
                mechanismRepository.batchUpdateMechanismLocNetworks(
                        connection, component, manager.getNetworkId()
                );
            }

            networkRepository.deleteNetwork(connection, result.oldNetworkId().toString());
            return true;
        });
    }

    public static void updateMemory(
            NetworkSplitResult result,
            BreakContext context,
            NetworkSystems networkSystems
    ) {
        // Удаляем старый механизм
        context.manager().deleteMechanism(context.mechanism().getLocation());

        // Привязываем элементы к новым сетям
        for (int i = 0; i < result.components().size(); i++) {
            NetworkManager newManager = result.newManagers().get(i);
            Set<INetworkElement> component = result.components().get(i);

            for (INetworkElement element : component) {
                element.setNetworkId(newManager.getNetworkId());
                newManager.addElement(element);

                Map<UUID, List<INetworkElement>> targetMap =
                        element.getMechanismType().getMechsByNetwork();
                targetMap.computeIfAbsent(newManager.getNetworkId(), id -> new ArrayList<>())
                        .add(element);
            }
        }

        // Удаляем старую сеть
        networkSystems.removeNetworkManager(result.oldNetworkId());
        for (MechanismType type : MechanismType.values()) {
            type.getMechsByNetwork().remove(result.oldNetworkId());
        }
    }

    public static void handleDropAndEffects(
            BlockBreakEvent event,
            BreakContext context,
            Mechanism plugin
    ) {
        CommonUtil.spawnPlaceEffect(context.block());
        event.setDropItems(false);

        if (context.block().getState() instanceof Container cont) {
            cont.getInventory().clear();
            cont.update(true);
        }
        context.block().setType(Material.AIR);

        if (context.player().getGameMode() != GameMode.CREATIVE) {
            ItemStack item = context.mechanismType().createItem(plugin).createItem(1);
            context.block().getWorld().dropItemNaturally(
                    context.block().getLocation(), item
            );
        }
    }

    public static void rollback(
            NetworkSplitResult result,
            INetworkElement mechanism,
            NetworkSystems networkSystems
    ) {
        // Восстанавливаем связи
        for (INetworkElement neighbor : result.neighbors()) {
            neighbor.addConnection(mechanism);
        }

        // Удаляем созданные сети
        for (NetworkManager manager : result.newManagers()) {
            networkSystems.removeNetworkManager(manager);
        }
    }
}
