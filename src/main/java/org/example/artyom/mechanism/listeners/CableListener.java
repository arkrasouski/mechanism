package org.example.artyom.mechanism.listeners;

import org.bukkit.event.Listener;
import org.example.artyom.mechanism.Mechanism;

public class CableListener implements Listener {
    private final Mechanism plugin;

    public CableListener(Mechanism plugin) {
        this.plugin = plugin;
    }
}
