package org.example.artyom.mechanism.utils;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.example.artyom.mechanism.mechanism.MechanismManager;
import org.example.artyom.mechanism.mechanism.MechanismType;
import org.example.artyom.mechanism.mechanism.base.IConsumer;
import org.example.artyom.mechanism.mechanism.base.IProducer;
import org.example.artyom.mechanism.mechanism.base.Mech;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;
import org.example.artyom.mechanism.mechanism.network.NetworkManager;

import java.util.UUID;

import static org.example.artyom.mechanism.Mechanism.getNetworkSystems;

public class ListenerUtil {
    /**
     * Проверяет, каким механизмом является блок и возвращает его тип
     */

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

        player.sendMessage(ChatColor.YELLOW + "⚡ Генератор ⚡");
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
}
