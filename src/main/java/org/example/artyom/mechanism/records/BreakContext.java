package org.example.artyom.mechanism.records;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.example.artyom.mechanism.mechanism.MechanismManager;
import org.example.artyom.mechanism.mechanism.MechanismType;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;
/**
 * Контекст для операции удаления механизма
 */
public record BreakContext(
        Block block,
        Player player,
        MechanismType mechanismType,
        MechanismManager manager,
        INetworkElement mechanism
) {}
