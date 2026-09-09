package org.example.artyom.mechanism.utils.mechanism_listener;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
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
import org.example.artyom.mechanism.records.PlaceContext;
import org.example.artyom.mechanism.utils.NetworkUtil;

import java.sql.SQLException;
import java.util.*;

public class PlaceUtil {
    public static PlaceContext validateAndPrepare(
            BlockPlaceEvent event,
            Mechanism plugin,
            NetworkSystems networkSystems
    ) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();

        if (!item.hasItemMeta()) return null;

        MechanismType type = CommonUtil.getMechanismType(plugin, item);
        if (type == null) return null;

        if (!CommonUtil.canPlaceMechanism(block, player)) {
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
        return CommonUtil.canPlaceMechanism(block, player);
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

        CommonUtil.mergeByPrimaryNetwork(
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
        CommonUtil.mergeByPrimaryNetwork(
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
                CommonUtil.mergeByPrimaryNetwork(
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
     * Эффекты установки механизма
     */
    public static void finalizePlacement(PlaceContext ctx) {
        // ШАГ 5: Сообщение игроку
        ctx.player().sendMessage("§a✓ " + ctx.mechanismType().getDisplayName() + " успешно установлен!");

        // ШАГ 6: Визуальный эффект
        CommonUtil.spawnPlaceEffect(ctx.block());
    }

    public static void handlePlacementError(PlaceContext ctx, SQLException e) {

        ctx.event().setCancelled(true);
        ctx.player().sendMessage("§cОшибка при сохранении механизма");
        e.printStackTrace();
    }
}
