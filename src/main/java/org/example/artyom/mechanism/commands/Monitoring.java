package org.example.artyom.mechanism.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.example.artyom.mechanism.mechanism.MechanismManager;
import org.example.artyom.mechanism.mechanism.network.NetworkManager;
import org.example.artyom.mechanism.mechanism.network.NetworkSystems;
import org.jetbrains.annotations.NotNull;

public class Monitoring implements CommandExecutor {

    private final NetworkSystems networkSystems;
    private final MechanismManager generatorManager;
    private final MechanismManager cableManager;

    public Monitoring(NetworkSystems networkSystems, MechanismManager generatorManager, MechanismManager cableManager) {
        this.networkSystems = networkSystems;
        this.generatorManager = generatorManager;
        this.cableManager = cableManager;
    }


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if (command.getName().equalsIgnoreCase("monitor")) {
                player.sendMessage("Network list:");
                for(NetworkManager network : networkSystems.getNetworks()) {
                    player.sendMessage(network.getNetworkId().toString());
                }

            }
        }
        return false;
    }
}
