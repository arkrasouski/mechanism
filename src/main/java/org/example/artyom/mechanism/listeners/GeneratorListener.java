package org.example.artyom.mechanism.listeners;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.example.artyom.mechanism.mechanism.MechanismManager;
import org.example.artyom.mechanism.mechanism.generator.Generator;
import org.example.artyom.mechanism.utils.LogUtil;

public class GeneratorListener implements Listener {

    private final MechanismManager generatorManager;

    public GeneratorListener(MechanismManager generatorManager) {
        this.generatorManager = generatorManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        // Проверяем, что это ПКМ по блоку
        Player player = event.getPlayer();
        if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK && player.isSneaking())) return;
        LogUtil.warn("CLicked!");
        Block block = event.getClickedBlock();
        if (block == null) return;

        // Проверяем, является ли блок генератором через generatorManager
        if(!generatorManager.isMechanism(block)) return;

        Generator generator = (Generator) generatorManager.getMechanism(block);
        if (generator == null) return;

        event.setCancelled(true);

        writeGeneratorInfoToPlayer(player, generator);
    }

    private void writeGeneratorInfoToPlayer(Player player, Generator generator) {
        int maxEnergyStorage = generator.getMaxEnergyStorage();
        int currentEnergy = generator.getCurrentEnergy();

        player.sendMessage(ChatColor.YELLOW + "⚡ Генератор ⚡");
        player.sendMessage(ChatColor.GRAY + "  Энергия: " + formatEnergy(currentEnergy, maxEnergyStorage));
        player.sendMessage(ChatColor.GRAY + "  Статус: " + (generator.isWorking() ? "§aАктивен" : "§cНеактивен"));
    }

    /**
     * Форматирует энергию для красивого отображения
     */
    private String formatEnergy(int current, int max) {
        double percent = (double) current / max * 100;
        String color;

        if (percent >= 75) color = "§a";
        else if (percent >= 50) color = "§e";
        else if (percent >= 25) color = "§6";
        else color = "§c";

        return color + current + "§7/§f" + max + " §7(" + String.format("%.1f", percent) + "%)";
    }

}
