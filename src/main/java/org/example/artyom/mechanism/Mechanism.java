package org.example.artyom.mechanism;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.example.artyom.mechanism.commands.MechanismCommands;
import org.example.artyom.mechanism.commands.Monitoring;
import org.example.artyom.mechanism.database.*;
import org.example.artyom.mechanism.listeners.ChunkListener;
import org.example.artyom.mechanism.listeners.MechanismListener;
import org.example.artyom.mechanism.mechanism.MechanismManager;
import org.example.artyom.mechanism.mechanism.MechanismType;

import org.example.artyom.mechanism.mechanism.network.INetworkElement;
import org.example.artyom.mechanism.mechanism.network.NetworkManager;
import org.example.artyom.mechanism.mechanism.network.NetworkSystems;
import org.example.artyom.mechanism.utils.ChunkUtil;
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

        getCommand("monitor").setExecutor(new Monitoring(networkSystems, generatorManager, cableManager));

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
                new ChunkListener(transactionManager, mechanismRepository, networkSystems),
                this
        );

        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                int chunkX = chunk.getX();
                int chunkZ = chunk.getZ();
                ChunkUtil.restoreMechanismsByChunk(transactionManager, mechanismRepository, networkSystems, world, chunkX, chunkZ);
            }
        }
    }

    @Override
    public void onDisable() {

        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                // Сохранить механизмы в чанке
                int chunkX = chunk.getX();
                int chunkZ = chunk.getZ();
                ChunkUtil.unloadMechanismsByChunk(transactionManager, mechanismRepository, networkSystems, world, chunkX, chunkZ);
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

}
