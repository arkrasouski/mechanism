package org.example.artyom.mechanism.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.example.artyom.mechanism.database.PlayerRepository;
import org.example.artyom.mechanism.database.TransactionManager;
import org.example.artyom.mechanism.utils.LogUtil;

import java.sql.SQLException;

public class PlayerListener implements Listener {

    private final TransactionManager transactionManager;

    public PlayerListener(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        try {
            transactionManager.execute((connection) -> {
                PlayerRepository.upsertPlayer(connection, player);
                return true;
            });
        } catch (SQLException e) {
            LogUtil.error("Ошибка при сохранении игрока", e);
            e.printStackTrace();
        }
    }
}
