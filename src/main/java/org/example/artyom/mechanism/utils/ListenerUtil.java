package org.example.artyom.mechanism.utils;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.example.artyom.mechanism.Mechanism;
import org.example.artyom.mechanism.database.MechanismRepository;
import org.example.artyom.mechanism.database.NetworkRepository;
import org.example.artyom.mechanism.database.TransactionManager;
import org.example.artyom.mechanism.inventories.MechanismHolder;
import org.example.artyom.mechanism.items.BaseItem;
import org.example.artyom.mechanism.mechanism.MechanismManager;
import org.example.artyom.mechanism.mechanism.MechanismType;
import org.example.artyom.mechanism.mechanism.barrier.Barrier;
import org.example.artyom.mechanism.mechanism.base.IConsumer;
import org.example.artyom.mechanism.mechanism.base.IProducer;
import org.example.artyom.mechanism.mechanism.base.Mech;
import org.example.artyom.mechanism.mechanism.encoder.Encoder;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;
import org.example.artyom.mechanism.mechanism.network.NetworkManager;
import org.example.artyom.mechanism.mechanism.network.NetworkSystems;
import org.example.artyom.mechanism.records.BreakContext;
import org.example.artyom.mechanism.records.NetworkComponentData;
import org.example.artyom.mechanism.records.NetworkSplitResult;
import org.example.artyom.mechanism.records.PlaceContext;

import java.sql.SQLException;
import java.util.*;

import static org.example.artyom.mechanism.Mechanism.getNetworkSystems;

public class ListenerUtil {
    /**
     * Проверяет, каким механизмом является блок и возвращает его тип
     */

    public static MechanismType getMechanismType(Mechanism plugin, ItemStack item){
        MechanismType mechanismType = null;
        for(MechanismType type : MechanismType.values()){
            if(BaseItem.isMechanismItem(plugin, item, type)){
                mechanismType = type;
            }
        }
        return mechanismType;
    }

    public static MechanismType getMechanismType(Block block){
        MechanismType mechanismType = null;
        for(MechanismType type : MechanismType.values()){
            MechanismManager manager = type.getMechanismManager();
            if(manager.isMechanism(block)){
                mechanismType = type;
            }
        }
        return mechanismType;
    }


    /**
     * Печать информации по графу
     */
    public static void showNetworkInfo(Player player, INetworkElement netElem) {
        Location loc = netElem.getLocation();
        UUID networkId = netElem.getNetworkId();

        NetworkManager netManager = getNetworkSystems().getNetworkManager(networkId);

        UUID ownerId = netManager.getOwner();
        int password = netManager.getPassword();

        player.sendMessage("§6=== Информация о сети ===");
        player.sendMessage("§7ID сети: §f" + networkId);
        player.sendMessage("§7Локация элемента: §f" + loc);
        player.sendMessage("§7Компонентов: §f" + netManager.getElements().size());
        player.sendMessage("§7Владелец: " + (ownerId == null ? "Нет владельца" : ownerId.toString()));
        player.sendMessage("§7Пароль: " + (password == -1 ? "Нет пароля" : password));
        // Дополнительная информация (если есть доступ к конкретным множествам)
        if (netElem instanceof IProducer) {
            player.sendMessage("Это генератор!");
        }
        else if (netElem instanceof IConsumer consumer){
            if(consumer instanceof Barrier) {
                player.sendMessage("Это барьер!");
            }
            else if (consumer instanceof Encoder) {
                player.sendMessage("Это шифратор!");
            }
        } else {
            player.sendMessage("Это кабель!");
        }

        int generatorCount = 0;
        int cableCount = 0;
        int barrierCount = 0;
        int enecoderCount = 0;
        for (INetworkElement elem : netManager.getElements()) {
            if(elem instanceof IProducer) {
                generatorCount++;
            }
            else if (elem instanceof IConsumer consumer) {
                if(consumer instanceof Barrier) {
                    barrierCount++;
                }
                else if (consumer instanceof Encoder) {
                    enecoderCount++;
                }
            }
            else {
                cableCount++;
            }

        }
        player.sendMessage("§7Всего: " + generatorCount + " Генераторов" );
        player.sendMessage("§7Всего: " + cableCount + " Кабелей");
        player.sendMessage("§7Всего: " + barrierCount + " Барьеров");
        player.sendMessage("§7Всего: " + enecoderCount + " Шифраторов");
    }

    /**
     * Эффект спавна механизма
     */
    public static void spawnPlaceEffect(Block block) {
        block.getWorld().playSound(block.getLocation(),
                org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 1.5f);
        block.getWorld().spawnParticle(org.bukkit.Particle.PORTAL,
                block.getLocation().add(0.5, 1, 0.5), 20, 0.3, 0.3, 0.3, 0.1);
    }
    /**
     * Проверяет, можно ли ставить здесь механизм
     */
    public static boolean canPlaceMechanism(Block block, Player player) {
        // Проверка на пустой блок
        return block.getType() == Material.AIR || !BlockUtil.isReplaceableBlock(block);

        // Проверка на наличие другого генератора
//!mechanismManager.isMechanism(block);

        // Проверка прав
//player.hasPermission("generator.place");
    }

    /**
     * Пишет энергию механизма и статус работы
     */
    public static void writeMechanismInfoToPlayer(Player player, Mech mechanism) {
        int maxEnergyStorage = mechanism.getMaxEnergyStorage();
        int currentEnergy = mechanism.getCurrentEnergy();

        player.sendMessage(ChatColor.YELLOW + "  " + mechanism.getMechanismType().getDisplayName());
        player.sendMessage(ChatColor.GRAY + "  Энергия: " + formatEnergy(currentEnergy, maxEnergyStorage));
        player.sendMessage(ChatColor.GRAY + "  Статус: " + (mechanism.isWorking() ? "§aАктивен" : "§cНеактивен"));
    }

    /**
     * Форматирует энергию для красивого отображения
     */
    public static String formatEnergy(int current, int max) {
        double percent = (double) current / max * 100;
        String color;

        if (percent >= 75) color = "§a";
        else if (percent >= 50) color = "§e";
        else if (percent >= 25) color = "§6";
        else color = "§c";

        return color + current + "§7/§f" + max + " §7(" + String.format("%.1f", percent) + "%)";
    }

    /**
     * Проверяет, используется ли шифратор
     */
    public static boolean isEncryptorInUse(
            Location location,
            Map<Block, Player> encryptorOwners,
            Map<Player, MechanismHolder> openedInventories
    ) {
        // Быстрая проверка через дополнительный мап
        if (encryptorOwners.containsKey(location)) {
            Player owner = encryptorOwners.get(location);
            // Проверяем, что владелец все еще онлайн и держит инвентарь
            if (owner != null && owner.isOnline() && openedInventories.containsKey(owner)) {
                return true;
            } else {
                // Если владелец оффлайн или закрыл инвентарь - чистим
                encryptorOwners.remove(location);
                return false;
            }
        }
        return false;
    }

    public static PlaceContext validateAndPrepare(
            BlockPlaceEvent event,
            Mechanism plugin,
            NetworkSystems networkSystems
    ) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();

        if (!item.hasItemMeta()) return null;

        MechanismType type = getMechanismType(plugin, item);
        if (type == null) return null;

        if (!canPlaceMechanism(block, player)) {
            event.setCancelled(true);
            player.sendMessage("§cНельзя установить " + type.getDisplayName() + " здесь!");
            return null;
        }

        INetworkElement mechanism = type.create(block.getLocation());
        if (mechanism == null) {
            event.setCancelled(true);
            player.sendMessage("§cОшибка при создании " + type.name());
            return null;
        }

        Set<NetworkManager> networks = NetworkUtil.getNetworkManagersByLoc(networkSystems, block.getLocation());
        Map<UUID, List<INetworkElement>> mechMap = type.getMechsByNetwork();

        return new PlaceContext(
                event, block, block.getLocation(), player, item, type,
                type.getMechanismManager(), mechanism, networks, mechMap
        );
    }

    public static boolean createMechanism(PlaceContext ctx) {
        // Механизм уже создан в validateAndPrepare, здесь только дополнительная валидация
        if (ctx.mechanism() == null) {
            ctx.event().setCancelled(true);
            ctx.player().sendMessage("§cОшибка: механизм не создан");
            return false;
        }

        // Дополнительная проверка: можно ли разместить этот тип механизма
        if (!canCreateMechanism(ctx)) {
            ctx.event().setCancelled(true);
            ctx.player().sendMessage("§cНельзя создать " + ctx.mechanismType().getDisplayName() + " здесь!");
            return false;
        }

        return true;
    }

    private static boolean canCreateMechanism(PlaceContext ctx) {
        // Проверки специфичные для создания механизма
        Block block = ctx.block();
        Player player = ctx.player();
        // Нельзя ставить в воздух (если блок уже не существует)
        if (block.getType() == Material.AIR) {
            return false;
        }
        return canPlaceMechanism(block, player);
    }

    public static boolean handleNetworkLogic(
            PlaceContext ctx,
            NetworkSystems networkSystems,
            TransactionManager transactionManager,
            NetworkRepository networkRepository,
            MechanismRepository mechanismRepository
    ) throws SQLException {
        if (ctx.connectedNetworks().isEmpty()) {
            handleNewNetwork(ctx, networkSystems, transactionManager, networkRepository, mechanismRepository);
        } else if (ctx.connectedNetworks().size() == 1) {
            handleSingleNeighbor(ctx, transactionManager, mechanismRepository);
        } else {
            return handleMultipleNeighbors(ctx, networkSystems, transactionManager, networkRepository, mechanismRepository);
        }
        return true;
    }

    private static void handleSingleNeighbor(
            PlaceContext ctx,
            TransactionManager transactionManager,
            MechanismRepository mechanismRepository
            ) throws SQLException {
        NetworkManager networkManager = ctx.connectedNetworks().iterator().next();
        UUID networkId = networkManager.getNetworkId();
        ctx.mechanism().setNetworkId(networkId);
        transactionManager.execute(connection -> {
            mechanismRepository.addMechanism(connection, ctx.mechanism());
            return true;
        });

        addMechanismToNetwork(networkManager, ctx.manager(), ctx.mechanismMap(), ctx.mechanism());
        ctx.player().sendMessage("Один сосед, перенимаю сеть!");
    }

    private static void handleNewNetwork(
            PlaceContext ctx,
            NetworkSystems networkSystems,
            TransactionManager transactionManager,
            NetworkRepository networkRepository,
            MechanismRepository mechanismRepository
            ) throws SQLException {
        NetworkManager networkManager = networkSystems.createDetachedNetwork(ctx.loc());
        UUID networkId = networkManager.getNetworkId();
        ctx.mechanism().setNetworkId(networkId);

        transactionManager.execute(connection -> {
            networkRepository.createNetwork(connection, networkManager);
            mechanismRepository.addMechanism(connection, ctx.mechanism());
            return true;
        });

        addMechanismToNetwork(networkManager, ctx.manager(), ctx.mechanismMap(), ctx.mechanism());
        networkSystems.addNetworkManager(networkManager);
        ctx.player().sendMessage("Создаю новую сеть!");

    }
    
    private static boolean handleMultipleNeighbors(
            PlaceContext ctx,
            NetworkSystems networkSystems,
            TransactionManager transactionManager,
            NetworkRepository networkRepository,
            MechanismRepository mechanismRepository
    ) throws SQLException {
        HashMap<UUID, NetworkManager> playerNetworkMap = getPlayerNetworkMap(ctx.connectedNetworks());

        if (playerNetworkMap.size() > 1) {
            if (ctx.mechanismType() == MechanismType.ENCODER) {
                handleEncoderMultipleOwners(
                        ctx,
                        playerNetworkMap,
                        transactionManager,
                        mechanismRepository,
                        networkRepository,
                        networkSystems
                );
            } else {
                rejectConflictingNetworks(ctx);
                return false;
            }
        } else if (playerNetworkMap.size() == 1) {
            mergeNetworksSingleOwner(
                    ctx,
                    playerNetworkMap,
                    transactionManager,
                    mechanismRepository,
                    networkRepository,
                    networkSystems);
        } else {
            mergeNetworksNoOwners(ctx, transactionManager, mechanismRepository, networkRepository, networkSystems);
        }
        return true;
    }

    /**
     *
     */
    private static void mergeNetworksNoOwners(
            PlaceContext ctx,
            TransactionManager transactionManager,
            MechanismRepository mechanismRepository,
            NetworkRepository networkRepository,
            NetworkSystems networkSystems
    ) throws SQLException {
        NetworkManager primaryNetwork = ctx.connectedNetworks().stream().max(Comparator.comparingInt(n -> n.getElements().size()))
                .orElseThrow();

        mergeByPrimaryNetwork(
                primaryNetwork,
                ctx.connectedNetworks(),
                ctx.mechanism(),
                ctx.loc(),
                ctx.mechanismMap(),
                ctx.manager(),
                ctx.player(),
                transactionManager,
                mechanismRepository,
                networkRepository,
                networkSystems
        );
    }

    /**
     * Просто склеиваем сети как раньше для одного владельца
     */
    private static void mergeNetworksSingleOwner(
            PlaceContext ctx,
            HashMap<UUID, NetworkManager> playerNetworkMap,
            TransactionManager transactionManager,
            MechanismRepository mechanismRepository,
            NetworkRepository networkRepository,
            NetworkSystems networkSystems
            ) throws SQLException {
        NetworkManager primaryNetwork = playerNetworkMap.values().iterator().next();
        mergeByPrimaryNetwork(
                primaryNetwork,
                ctx.connectedNetworks(),
                ctx.mechanism(),
                ctx.loc(),
                ctx.mechanismMap(),
                ctx.manager(),
                ctx.player(),
                transactionManager,
                mechanismRepository,
                networkRepository,
                networkSystems
        );
    }

    private static void rejectConflictingNetworks(PlaceContext ctx) {
        ctx.player().sendMessage("Конфликт! Необходим шифратор для разрешения");
        ctx.event().setCancelled(true);
    }

    /**
     * Если решаем конфликт шифратором
     */
    private static void handleEncoderMultipleOwners(
            PlaceContext ctx,
            HashMap<UUID, NetworkManager> playerNetworkMap,
            TransactionManager transactionManager,
            MechanismRepository mechanismRepository,
            NetworkRepository networkRepository,
            NetworkSystems networkSystems
    ) throws SQLException {
        //Если игрок ставит шифратор возле своей сети, берем ее
        // Собираем ВСЕ сети игрока
        UUID playerId = ctx.player().getUniqueId();

        List<NetworkManager> playerNetworks = ctx.connectedNetworks().stream()
                .filter(nm -> playerId.equals(nm.getOwner()))
                .toList();

        if (playerNetworks.isEmpty()) {
            ctx.player().sendMessage("Вы не являетесь владельцем ни одной из сетей! Досвидос");
            ctx.event().setCancelled(true);
            return;
        }

        // Объединяем все сети игрока в одну
        NetworkManager primaryPlayerNetwork = playerNetworks.get(0);
        if (playerNetworks.size() > 1) {
            for (int i = 1; i < playerNetworks.size(); i++) {
                // Исправлено: передаем Set, а не List
                Set<NetworkManager> toMerge = new HashSet<>();
                toMerge.add(playerNetworks.get(i));
                mergeByPrimaryNetwork(
                        primaryPlayerNetwork,
                        toMerge,
                        ctx.mechanism(),
                        ctx.loc(),
                        ctx.mechanismMap(),
                        ctx.manager(),
                        ctx.player(),
                        transactionManager,
                        mechanismRepository,
                        networkRepository,
                        networkSystems
                );
            }
        }

        // Только свои сети — просто добавляем механизм
        ctx.mechanism().setNetworkId(primaryPlayerNetwork.getNetworkId());
        addMechanismToNetwork(primaryPlayerNetwork, ctx.manager(), ctx.mechanismMap(), ctx.mechanism());
        transactionManager.execute(connection -> {
            mechanismRepository.addMechanism(connection, ctx.mechanism());
            return true;
        });
    }

    /**
     * Функция получения уникальных владельцев соседних сетей
     */
    private static HashMap<UUID, NetworkManager> getPlayerNetworkMap(Set<NetworkManager> connectedNetworks){
        HashMap<UUID, NetworkManager> playerNetworkMap = new HashMap<>();
        for(NetworkManager networkManager : connectedNetworks) {
            UUID ownerId = networkManager.getOwner();
            if (ownerId != null) {
                playerNetworkMap.put(ownerId, networkManager);
            }
        }
        return playerNetworkMap;
    }
    /**
     * Вспомогательная функция привязки механизма к сети и переменным хранения механизма
     */
    private static void addMechanismToNetwork(NetworkManager networkManager,
                                              MechanismManager manager,
                                              Map<UUID, List<INetworkElement>> mechanismMap,
                                              INetworkElement mechanism
    ){
        UUID networkId = networkManager.getNetworkId();
        networkManager.addElement(mechanism);
        manager.registerMechanism(mechanism, mechanism.getLocation());
        mechanismMap.computeIfAbsent(networkId, id -> new ArrayList<>())
                .add(mechanism);

    }

    /**
     * Объединяет несколько сетей вокруг главной сети
     */
    private static void mergeByPrimaryNetwork(
            NetworkManager primaryNetwork,
            Set<NetworkManager> connectedNetworks,
            INetworkElement mechanism,
            Location loc,
            Map<UUID, List<INetworkElement>> mechanismMap,
            MechanismManager manager,
            Player player,
            TransactionManager transactionManager,
            MechanismRepository mechanismRepository,
            NetworkRepository networkRepository,
            NetworkSystems networkSystems
    ) throws SQLException {
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

    /**
     * Эффекты установки механизма
     */
    public static void finalizePlacement(PlaceContext ctx) {
        // ШАГ 5: Сообщение игроку
        ctx.player().sendMessage("§a✓ " + ctx.mechanismType().getDisplayName() + " успешно установлен!");

        // ШАГ 6: Визуальный эффект
        spawnPlaceEffect(ctx.block());
    }

    public static void handlePlacementError(PlaceContext ctx, SQLException e) {

        ctx.event().setCancelled(true);
        ctx.player().sendMessage("§cОшибка при сохранении механизма");
        e.printStackTrace();
    }

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

    public static BreakContext createBreakContext(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        MechanismType mechanismType = getMechanismType(block);
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
                .map(comp -> ListenerUtil.analyzeComponent(comp, networkSystems))
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
    public static void persistSplitBreakToDatabase(
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

    public static void updateMemoryBreak(
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

    public static void handleDropAndEffectsBreak(
            BlockBreakEvent event,
            BreakContext context,
            Mechanism plugin
    ) {
        ListenerUtil.spawnPlaceEffect(context.block());
        event.setDropItems(false);

        if (context.block().getState() instanceof Container cont) {
            cont.getInventory().clear();
            cont.update(true);
        }
        context.block().setType(Material.AIR);

        if (context.player().getGameMode() != GameMode.CREATIVE) {
            ItemStack item = context.mechanismType().create(plugin).createItem(1);
            context.block().getWorld().dropItemNaturally(
                    context.block().getLocation(), item
            );
        }
    }

    public static void rollbackBreak(
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
