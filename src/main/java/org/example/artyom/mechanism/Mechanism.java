package org.example.artyom.mechanism;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.example.artyom.mechanism.commands.MechanismCommands;
import org.example.artyom.mechanism.database.DatabaseManager;
import org.example.artyom.mechanism.database.GeneratorRepository;
import org.example.artyom.mechanism.database.NetworkRepository;
import org.example.artyom.mechanism.listeners.MechanismListener;
import org.example.artyom.mechanism.mechanism.MechanismManager;
import org.example.artyom.mechanism.mechanism.MechanismType;
import org.example.artyom.mechanism.mechanism.network.NetworkSystems;
import org.example.artyom.mechanism.utils.LogUtil;

public final class Mechanism extends JavaPlugin {

    private DatabaseManager databaseManager;
    private NetworkRepository networkRepository;
    private GeneratorRepository generatorRepository;

    private static NetworkSystems networkSystems;
    private static MechanismManager generatorManager;
    private static MechanismManager cableManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        // Сохраняем конфиг по умолчанию из resources
        saveDefaultConfig();
        // Перезагружаем конфиг (на всякий случай)
        reloadConfig();
        LogUtil.init(this);

        // Создание пути к базе данных
        String dbPath = getDataFolder().getPath() + "/energy_networks.db";
        // Инициализация DatabaseManager
        databaseManager = new DatabaseManager(this, dbPath);
        networkRepository = new NetworkRepository(databaseManager);
        generatorRepository = new GeneratorRepository(databaseManager);
        //managers
        generatorManager = new MechanismManager(this);
        cableManager = new MechanismManager(this);
        //network
        networkSystems = new NetworkSystems();

        //commands
        getCommand("getgen").setExecutor(new MechanismCommands(this));
        getCommand("givecell").setExecutor(new MechanismCommands(this));
        getCommand("getbarrier").setExecutor(new MechanismCommands(this));
        getCommand("getcable").setExecutor(new MechanismCommands(this));

        //listeners
        Bukkit.getPluginManager().registerEvents(
                new MechanismListener(this,
                                        generatorManager,
                                        networkSystems,
                                        MechanismType.GENERATOR
                                        ),this);
        Bukkit.getPluginManager().registerEvents(
               new MechanismListener(this,
                                        cableManager,
                                        networkSystems,
                                        MechanismType.CABLE
                                        ), this);
        GeneratorRepository.restoreAllGenerators();
    }

    @Override
    public void onDisable() {
        // Закрытие соединения с базой
        if (databaseManager != null) {
            databaseManager.close();
        }
        // Plugin shutdown logic
        getLogger().info("NetworkSystems disabled!");
    }

    // Getters для Repository'ев
    public NetworkRepository getNetworkRepository() { return networkRepository; }
    public GeneratorRepository getGeneratorRepository() { return generatorRepository; }

    //Getters для Менеджеров
    public static MechanismManager getGeneratorManager() { return generatorManager; }
    public static NetworkSystems getNetworkSystems() { return networkSystems; }
    public static MechanismManager getCableManager() { return cableManager; }
}
