package org.example.artyom.mechanism.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.example.artyom.mechanism.mechanism.MechanismManager;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;
import org.example.artyom.mechanism.mechanism.network.NetworkManager;
import org.example.artyom.mechanism.mechanism.network.NetworkSystems;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Monitoring implements CommandExecutor {

    private final NetworkSystems networkSystems;
    private final MechanismManager generatorManager;
    private final MechanismManager cableManager;
    private final MechanismManager barrierManager;
    private final Map<UUID, List<INetworkElement>> generatorsByNetwork;
    private final Map<UUID, List<INetworkElement>> cablesByNetwork;
    private final Map<UUID, List<INetworkElement>> barriersByNetwork;


    public Monitoring(NetworkSystems networkSystems,
                      MechanismManager generatorManager,
                      MechanismManager cableManager,
                      MechanismManager barrierManager,
                      Map<UUID, List<INetworkElement>> generatorsByNetwork,
                      Map<UUID, List<INetworkElement>> cablesByNetwork,
                      Map<UUID, List<INetworkElement>> barriersByNetwork
                      ) {
        this.networkSystems = networkSystems;
        this.generatorManager = generatorManager;
        this.cableManager = cableManager;
        this.barrierManager = barrierManager;
        this.generatorsByNetwork = generatorsByNetwork;
        this.cablesByNetwork = cablesByNetwork;
        this.barriersByNetwork = barriersByNetwork;
    }


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player player) {
            if (command.getName().equalsIgnoreCase("monitor")) {
                player.sendMessage("Network list:");
                for(NetworkManager network : networkSystems.getNetworks()) {
                    player.sendMessage(network.getNetworkId().toString());
                }
                player.sendMessage("Barrier man list");
                for(INetworkElement barrier : barrierManager.getActiveMechanisms()){
                    player.sendMessage(barrier.getLocation().toString());
                }
                player.sendMessage("Cable man list:");
                for(INetworkElement cable : cableManager.getActiveMechanisms()){
                    player.sendMessage(cable.getLocation().toString());
                }
                player.sendMessage("Generator man list:");
                for(INetworkElement generator : generatorManager.getActiveMechanisms()){
                    player.sendMessage(generator.getLocation().toString());
                }

                printNetworkWithMechanism(player, generatorsByNetwork, ChatColor.GRAY, "Генераторы");

                printNetworkWithMechanism(player, barriersByNetwork, ChatColor.BLUE, "Барьеры");

                printNetworkWithMechanism(player, cablesByNetwork, ChatColor.DARK_PURPLE, "Кабели");
            }
        }
        return false;
    }

    private void printNetworkWithMechanism(
            Player player,
            Map<UUID, List<INetworkElement>> mechanismByNetwork,
            ChatColor color,
            String text
    ) {
        for(Map.Entry<UUID, List<INetworkElement>> netWithMechanism : mechanismByNetwork.entrySet()) {
            player.sendMessage(ChatColor.BLACK + text);
            UUID networkId = netWithMechanism.getKey();
            player.sendMessage(ChatColor.GOLD + networkId.toString());
            List<INetworkElement> generators = netWithMechanism.getValue();
            for(INetworkElement generator : generators){
                player.sendMessage(color + generator.getLocation().toString());
            }
        }
    }
}
