package org.example.artyom.mechanism;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.example.artyom.mechanism.commands.MechanismCommands;
import org.example.artyom.mechanism.database.*;
import org.example.artyom.mechanism.listeners.ChunkListener;
import org.example.artyom.mechanism.listeners.MechanismListener;
import org.example.artyom.mechanism.mechanism.MechanismManager;
import org.example.artyom.mechanism.mechanism.MechanismType;

import org.example.artyom.mechanism.mechanism.network.INetworkElement;
import org.example.artyom.mechanism.mechanism.network.NetworkManager;
import org.example.artyom.mechanism.mechanism.network.NetworkSystems;
import org.example.artyom.mechanism.utils.LogUtil;

import java.sql.SQLException;
import java.util.List;


public final class Mechanism extends JavaPlugin {

    //private DatabaseManager databaseManager;
    private DatabaseConnectionPool pool;
    private TransactionManager transactionManager;
    private NetworkRepository networkRepository;
    private MechanismRepository mechanismRepository;

    private static NetworkSystems networkSystems;
    private static MechanismManager generatorManager;
    private static MechanismManager cableManager;
    private final String dbPath = getDataFolder().getPath() + "/energy_networks.db";
    @Override
    public void onEnable() {
        // Plugin startup logic
        // Сохраняем конфиг по умолчанию из resources
        saveDefaultConfig();
        // Перезагружаем конфиг (на всякий случай)
        reloadConfig();
        LogUtil.init(this);

        // Создание пути к базе данных

        // Инициализация DatabaseManager

        try {
            pool = DatabaseConnectionPool.getInstance(dbPath);
            LogUtil.info("Database connection pool created");
        } catch (Exception e) {
            System.err.println("Error initializing connection pool: " + e.getMessage());
        }

        transactionManager = new TransactionManager(pool);
        mechanismRepository = new MechanismRepository();
        networkRepository = new NetworkRepository();


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
                        MechanismType.CABLE,
                        transactionManager,
                        networkRepository,
                        mechanismRepository
                ), this);
        Bukkit.getPluginManager().registerEvents(
                new MechanismListener(this,
                        generatorManager,
                        networkSystems,
                        MechanismType.GENERATOR,
                        transactionManager,
                        networkRepository,
                        mechanismRepository
                                        ),this);

        Bukkit.getPluginManager().registerEvents(
                new ChunkListener(transactionManager, mechanismRepository),
                this
        );

        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                try {
                    List<INetworkElement> mechanisms = transactionManager.execute(connection -> mechanismRepository.findByChunk(
                            connection,
                            world,
                            chunk.getX(),
                            chunk.getZ()
                    ));

                    int restoredCount = 0;

                    for (INetworkElement mechanism : mechanisms) {
                        restoredCount++;
                        NetworkManager networkManager;
                        LogUtil.warn("lol" + mechanism.getNetworkId());
                        if(networkSystems.hasNetwork(mechanism.getNetworkId())) {
                            networkManager = networkSystems.getNetworkManager(mechanism.getNetworkId());
                        } else {
                            networkManager = new NetworkManager(mechanism.getNetworkId(), mechanism.getLocation().getWorld());
                            networkSystems.addNetworkManager(networkManager);
                        }
                        networkManager.addElement(mechanism);
                        mechanism.getMechanismType().getMechanismManager().registerMechanism(mechanism, mechanism.getLocation());
                    }
                    LogUtil.info("Restored " + restoredCount + " mechanism from database");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        //restoreAllMechanism();
    }

    @Override
    public void onDisable() {

        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                // Сохранить механизмы в чанке
                int chunkX = chunk.getX();
                int chunkZ = chunk.getZ();
                try {
                    List<INetworkElement> mechanisms = transactionManager.execute(connection -> {
                        List<INetworkElement> mechs = mechanismRepository.findByChunk(connection, world, chunkX, chunkZ);

                        for (INetworkElement mechanism : mechs) {
                            mechanismRepository.updateMechanismState(connection, mechanism);
                        }

                        return mechs;
                    });
                }
                catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        // Закрытие пула при завершении приложения
        DatabaseConnectionPool.getInstance(dbPath).closePool();
        // Plugin shutdown logic
        getLogger().info("NetworkSystems disabled!");
    }

    // Getters для Repository'ев
    public NetworkRepository getNetworkRepository() { return networkRepository; }

    public MechanismRepository getMechanismRepository() {
        return mechanismRepository;
    }

    //Getters для Менеджеров
    public static MechanismManager getGeneratorManager() { return generatorManager; }
    public static NetworkSystems getNetworkSystems() { return networkSystems; }
    public static MechanismManager getCableManager() { return cableManager; }

//    private void restoreAllMechanism(){
//        LogUtil.info("Starting to restore all mechanisms from database...");
//
//        // Получить все сети
//        List<NetworkManager> allNetworks = NetworkRepository.getAllNetworks();
//
//        int restoredCount = 0;
//
//        for (NetworkManager network : allNetworks) {
//            UUID networkId = network.getNetworkId();
//
//            // Получить все механизмы в этой сети
//            List<INetworkElement> mechanisms = new ArrayList<>();
//
////            for (MechanismType mechanismType : MechanismType.values()) {
////               mechanisms.addAll(mechanismType.getByNetwork(networkId));
////            }
//
//            for (INetworkElement mechanism : mechanisms) {
//                restoredCount++;
//                network.addElement(mechanism);
//                mechanism.getMechanismType().getMechanismManager().registerMechanism(mechanism, mechanism.getLocation());
//                //Mechanism.getCableManager().registerMechanism(mechanism, mechanism.getLocation()); //TODO: Сделать регистрацию в свой менджер
//            }
//        }
//
//        LogUtil.info("Restored " + restoredCount + " cables from database");
//    }

}
