package org.example.artyom.mechanism.utils.mechanism_listener;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.example.artyom.mechanism.Mechanism;
import org.example.artyom.mechanism.database.MechanismRepository;
import org.example.artyom.mechanism.database.NetworkRepository;
import org.example.artyom.mechanism.database.TransactionManager;
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
import org.example.artyom.mechanism.utils.BlockUtil;

import java.sql.SQLException;
import java.util.*;

import static org.example.artyom.mechanism.Mechanism.getNetworkSystems;

public class CommonUtil {
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
     * Объединяет несколько сетей вокруг главной сети
     */
    static void mergeByPrimaryNetwork(
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
}
