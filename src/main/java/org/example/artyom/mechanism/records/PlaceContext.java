package org.example.artyom.mechanism.records;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.example.artyom.mechanism.mechanism.MechanismManager;
import org.example.artyom.mechanism.mechanism.MechanismType;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;
import org.example.artyom.mechanism.mechanism.network.NetworkManager;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record PlaceContext(
        BlockPlaceEvent event,
        Block block,
        Location loc,
        Player player,
        ItemStack item,
        MechanismType mechanismType,
        MechanismManager manager,
        INetworkElement mechanism,
        Set<NetworkManager> connectedNetworks,
        Map<UUID, List<INetworkElement>> mechanismMap
) {}
