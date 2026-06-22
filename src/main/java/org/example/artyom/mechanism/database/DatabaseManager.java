package org.example.artyom.mechanism.database;

import org.bukkit.plugin.java.JavaPlugin;
import org.example.artyom.mechanism.utils.LogUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private final JavaPlugin plugin;
    private final String dbPath;
    private Connection connection;

    public DatabaseManager(JavaPlugin plugin, String dbPath) {
        this.plugin = plugin;
        this.dbPath = dbPath;
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            // Подключение к SQLite
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            createTables();
            plugin.getLogger().info("SQLite database connected successfully");
        } catch (SQLException e) {
            LogUtil.error("Failed to connect to database: ", e);
        }
    }

    private void createTables() {
        String createNetworksTable = """
            CREATE TABLE IF NOT EXISTS networks (
                network_id TEXT PRIMARY KEY,
                world_name TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """;

        String createGeneratorsTable = """
            CREATE TABLE IF NOT EXISTS generators (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                network_id TEXT NOT NULL,
                world_name TEXT NOT NULL,
                x INTEGER NOT NULL,
                y INTEGER NOT NULL,
                z INTEGER NOT NULL,
                is_working BOOLEAN DEFAULT TRUE,
                current_energy INT DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (network_id) REFERENCES networks(network_id)
            );
        """;

//        String createConsumersTable = """
//            CREATE TABLE IF NOT EXISTS consumers (
//                id INTEGER PRIMARY KEY AUTOINCREMENT,
//                network_id TEXT NOT NULL,
//                world_name TEXT NOT NULL,
//                x INTEGER NOT NULL,
//                y INTEGER NOT NULL,
//                z INTEGER NOT NULL,
//                consumer_type TEXT NOT NULL,
//                energy_consumption REAL DEFAULT 0,
//                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
//                FOREIGN KEY (network_id) REFERENCES networks(network_id)
//            );
//        """;

        String createCablesTable = """
            CREATE TABLE IF NOT EXISTS cables (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                network_id TEXT NOT NULL,
                world_name TEXT NOT NULL,
                x INTEGER NOT NULL,
                y INTEGER NOT NULL,
                z INTEGER NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (network_id) REFERENCES networks(network_id)
            );
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createNetworksTable);
            stmt.execute(createGeneratorsTable);
            //stmt.execute(createConsumersTable);
            stmt.execute(createCablesTable);

            // Создание индексов
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_generators_network ON generators(network_id)");
            //stmt.execute("CREATE INDEX IF NOT EXISTS idx_consumers_network ON consumers(network_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_cables_network ON cables(network_id)");
        } catch (SQLException e) {
            LogUtil.error("Failed to create tables: ", e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            LogUtil.error("Failed to close database: ", e);
        }
    }
}