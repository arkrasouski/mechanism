package org.example.artyom.mechanism;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.example.artyom.mechanism.commands.MechanismCommands;
import org.example.artyom.mechanism.database.CableRepository;
import org.example.artyom.mechanism.database.DatabaseManager;
import org.example.artyom.mechanism.database.GeneratorRepository;
import org.example.artyom.mechanism.database.NetworkRepository;
import org.example.artyom.mechanism.listeners.MechanismListener;
import org.example.artyom.mechanism.mechanism.MechanismManager;
import org.example.artyom.mechanism.mechanism.MechanismType;
import org.example.artyom.mechanism.mechanism.cable.Cable;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;
import org.example.artyom.mechanism.mechanism.network.NetworkManager;
import org.example.artyom.mechanism.mechanism.network.NetworkSystems;
import org.example.artyom.mechanism.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Mechanism extends JavaPlugin {

    private DatabaseManager databaseManager;
    private NetworkRepository networkRepository;
    private GeneratorRepository generatorRepository;
    private CableRepository cableRepository;

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
        cableRepository = new CableRepository(databaseManager);

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
                        cableManager,
                        networkSystems,
                        MechanismType.CABLE
                ), this);
        Bukkit.getPluginManager().registerEvents(
                new MechanismListener(this,
                                        generatorManager,
                                        networkSystems,
                                        MechanismType.GENERATOR
                                        ),this);

        restoreAllMechanism();
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
    public CableRepository getCableRepository() { return cableRepository; }

    //Getters для Менеджеров
    public static MechanismManager getGeneratorManager() { return generatorManager; }
    public static NetworkSystems getNetworkSystems() { return networkSystems; }
    public static MechanismManager getCableManager() { return cableManager; }

    private void restoreAllMechanism(){
        LogUtil.info("Starting to restore all mechanisms from database...");

        // Получить все сети
        List<NetworkManager> allNetworks = NetworkRepository.getAllNetworks();

        int restoredCount = 0;

        for (NetworkManager network : allNetworks) {
            UUID networkId = network.getNetworkId();

            // Получить все механизмы в этой сети
            List<INetworkElement> mechanisms = new ArrayList<>();

            for (MechanismType mechanismType : MechanismType.values()) {
               mechanisms.addAll(mechanismType.getByNetwork(networkId));
            }

            for (INetworkElement mechanism : mechanisms) {
                restoredCount++;
                network.addElement(mechanism);
                mechanism.getMechanismType().getMechanismManager().registerMechanism(mechanism, mechanism.getLocation());
                //Mechanism.getCableManager().registerMechanism(mechanism, mechanism.getLocation()); //TODO: Сделать регистрацию в свой менджер
            }
        }

        LogUtil.info("Restored " + restoredCount + " cables from database");
    }

}
