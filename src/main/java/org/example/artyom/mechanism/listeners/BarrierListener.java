package org.example.artyom.mechanism.listeners;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.example.artyom.mechanism.mechanism.MechanismManager;
import org.example.artyom.mechanism.mechanism.barrier.Barrier;


public class BarrierListener implements Listener {

    private final MechanismManager barrierManager;

    public BarrierListener(MechanismManager barrierManager){
        this.barrierManager = barrierManager;
    }

    @EventHandler
    public void onInteractInfo(PlayerInteractEvent event) {
        // Проверяем, что это ПКМ по блоку
        Player player = event.getPlayer();
        if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK && player.isSneaking())) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        // Проверяем, является ли блок генератором через generatorManager
        if(!barrierManager.isMechanism(block)) return;

        Barrier barrier = (Barrier) barrierManager.getMechanism(block);
        if (barrier == null) return;

        event.setCancelled(true);

        writeBarrierInfoToPlayer(player, barrier);
    }

    private void writeBarrierInfoToPlayer(Player player, Barrier barrier) {
        int maxEnergyStorage = barrier.getMaxEnergyStorage();
        int currentEnergy = barrier.getCurrentEnergy();

        player.sendMessage(ChatColor.YELLOW + "⚡ Барьер ⚡");
        player.sendMessage(ChatColor.GRAY + "  Энергия: " + formatEnergy(currentEnergy, maxEnergyStorage));
        player.sendMessage(ChatColor.GRAY + "  Статус: " + (barrier.isWorking() ? "§aАктивен" : "§cНеактивен"));
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
