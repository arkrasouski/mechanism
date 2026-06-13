package org.example.artyom.mechanism.utils;



import org.example.artyom.mechanism.Mechanism;

import java.sql.SQLException;

public class LogUtil {
    private static Mechanism plugin;

    public static void init(Mechanism pluginInstance) {
        plugin = pluginInstance;
    }

    public static void info(String msg) {
        plugin.getLogger().info(msg);
    }

    public static void warn(String msg) {
        plugin.getLogger().warning(msg);
    }

    public static void error(String msg, SQLException e) {
        plugin.getLogger().severe(msg);
        e.printStackTrace();
    }
}
