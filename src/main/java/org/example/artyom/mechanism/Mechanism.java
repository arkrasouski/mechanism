package org.example.artyom.mechanism;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.example.artyom.mechanism.commands.MechanismCommands;
import org.example.artyom.mechanism.listeners.generator.GeneratorListener;
import org.example.artyom.mechanism.mechanism.generator.GeneratorManager;
import org.example.artyom.mechanism.mechanism.network.NetworkSystems;
import org.example.artyom.mechanism.utils.LogUtil;

public final class Mechanism extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        // Сохраняем конфиг по умолчанию из resources
        saveDefaultConfig();
        // Перезагружаем конфиг (на всякий случай)
        reloadConfig();
        LogUtil.init(this);

        //managers
        GeneratorManager generatorManager = new GeneratorManager(this);

        //network
        NetworkSystems networkSystems = new NetworkSystems();

        //commands
        getCommand("getgen").setExecutor(new MechanismCommands(this));
        getCommand("givecell").setExecutor(new MechanismCommands(this));
        getCommand("getbarrier").setExecutor(new MechanismCommands(this));
        getCommand("getcable").setExecutor(new MechanismCommands(this));

        //listeners
        Bukkit.getPluginManager().registerEvents(new GeneratorListener(this, generatorManager, networkSystems),this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("NetworkSystems disabled!");
    }
}
