package org.example.artyom.mechanism.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.SQLException;
import java.sql.Connection;

public class DatabaseConnectionPool {

    private static HikariDataSource dataSource;
    private static DatabaseConnectionPool instance;

    // Конфигурация подключения
    private static String JDBC_URL;


    // Приватный конструктор (Singleton)
    private DatabaseConnectionPool(String JDBC_URL) {
        initializeDataSource(JDBC_URL);
    }

    // Инициализация пула соединений
    private void initializeDataSource(String JDBC_URL) {
        HikariConfig config = new HikariConfig();

        // Основные настройки
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + JDBC_URL);

        // Настройки пула
        config.setMaximumPoolSize(1);           // Максимальное количество соединений
        config.setMinimumIdle(1);                // Минимальное количество idle-соединений
        config.setConnectionTimeout(30000);      // Таймаут ожидания соединения (мс)
        config.setIdleTimeout(600000);           // Таймаут простоя соединения (мс)
        config.setMaxLifetime(1800000);          // Максимальное время жизни соединения (мс)

        // Дополнительные настройки
        config.setPoolName("MyHikariPool");
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(5000);
        config.setLeakDetectionThreshold(60000); // Обнаружение утечек соединений

        dataSource = new HikariDataSource(config);
    }

    // Получение экземпляра (Singleton)
    public static synchronized DatabaseConnectionPool getInstance(String JDBC_URL) {
        if (instance == null) {
            instance = new DatabaseConnectionPool(JDBC_URL);
        }
        return instance;
    }

    // Получение соединения из пула
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource is not initialized");
        }
        return dataSource.getConnection();
    }

    // Закрытие пула соединений
    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    // Получение статистики пула
    public void printPoolStats() {
        if (dataSource != null) {
            System.out.println("Active connections: " + dataSource.getHikariPoolMXBean().getActiveConnections());
            System.out.println("Idle connections: " + dataSource.getHikariPoolMXBean().getIdleConnections());
            System.out.println("Total connections: " + dataSource.getHikariPoolMXBean().getTotalConnections());
            System.out.println("Threads awaiting connection: " + dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
        }
    }
}
