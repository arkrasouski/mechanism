package org.example.artyom.mechanism.database;

import com.google.common.graph.Network;
import org.apache.commons.logging.Log;
import org.bukkit.Location;
import org.example.artyom.mechanism.Mechanism;
import org.example.artyom.mechanism.mechanism.MechanismManager;
import org.example.artyom.mechanism.mechanism.base.Mech;
import org.example.artyom.mechanism.mechanism.cable.Cable;
import org.example.artyom.mechanism.mechanism.generator.Generator;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;
import org.example.artyom.mechanism.mechanism.network.NetworkManager;
import org.example.artyom.mechanism.utils.LogUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.bukkit.Bukkit.getServer;

public class MechanismRepository {
    private static DatabaseManager db = null;

    public MechanismRepository(DatabaseManager db) {
        MechanismRepository.db = db;
    }

    /**
     * Добавить механизм в бд
     */
    public static boolean addMechanism(Connection connection, INetworkElement networkElement) {
        int type = networkElement.getMechanismType().ordinal();
        String network_id = networkElement.getNetworkId().toString();
        String world_name = networkElement.getLocation().getWorld().getName();
        int x = networkElement.getLocation().getBlockX();
        int y = networkElement.getLocation().getBlockY();
        int z = networkElement.getLocation().getBlockZ();
        boolean is_working;
        int current_energy;

        if (networkElement instanceof Mech) {
            Mech mechanism = (Mech) networkElement;
            is_working = mechanism.isWorking();
            current_energy = mechanism.getCurrentEnergy();
        } else {
            Cable mechanism = (Cable) networkElement;
            is_working = false;
            current_energy = 0;
        }


        String sql = """
        INSERT INTO mechanism
        (network_id, world_name, x, y, z, type, is_working, current_energy) 
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        if (connection == null) {
            try {

                connection = db.getConnection();
                LogUtil.warn(connection.isClosed() + "=closed");
                connection.setAutoCommit(true);

            }
            catch (SQLException e) {
                LogUtil.error("Ошибка добавления механизма в бд!", e);
            }
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, network_id);
            stmt.setString(2, world_name);
            stmt.setInt(3, x);
            stmt.setInt(4, y);
            stmt.setInt(5, z);
            stmt.setInt(6, type);
            stmt.setBoolean(7, is_working);
            stmt.setDouble(8, current_energy);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Обновить сеть всех механизмов из старых сетей
     */
    public static void batchUpdateMechanismNetworks(
            Connection connection,
            UUID primaryNetworkId,
            List<UUID> secondaryNetworkIds
    ) throws SQLException {


                LogUtil.warn("И здесь был!");

                String placeholders = secondaryNetworkIds.stream()
                        .map(id -> "?")
                        .collect(java.util.stream.Collectors.joining(", ", "(", ")"));

                String sql = "UPDATE mechanism SET network_id = ? WHERE network_id IN %s".formatted(placeholders);
                LogUtil.warn(sql);
                LogUtil.warn("connection = " + connection);
                LogUtil.warn("closed = " + connection.isClosed());
                LogUtil.warn(secondaryNetworkIds.size() + "");
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, primaryNetworkId.toString());
                    int index = 2;
                    for (UUID id : secondaryNetworkIds) {
                        ps.setString(index++, id.toString());
                    }
                    LogUtil.warn("in update");
                    int updated = ps.executeUpdate();
                    LogUtil.warn("Updated rows: " + updated);

                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                }

        }

//    // Получить все генераторы в сети
//    public static List<INetworkElement> getGeneratorsByNetwork(String networkId) {
//        List<INetworkElement> generators = new ArrayList<>();
//        String sql = "SELECT * FROM generators WHERE network_id = ?";
//
//        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
//            stmt.setString(1, networkId);
//            ResultSet rs = stmt.executeQuery();
//
//            while (rs.next()) {
//                Generator generator = new Generator(new Location(
//                        getServer().getWorld("world"),
//                        rs.getInt("x"),
//                        rs.getInt("y"),
//                        rs.getInt("z")
//                )
//
//                );
//                generator.setCurrentEnergy(rs.getInt("current_energy"));
//                generator.setNetworkId(UUID.fromString(rs.getString("network_id")));
//                generators.add(generator);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        return generators;
//    }
//
//    // удалить генераторы из сети
//    public static boolean removeGeneratorsByNetwork(String networkId) {
//        List<Generator> generators = new ArrayList<>();
//        String sql = "DELETE FROM generators WHERE network_id = ?";
//
//        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
//            stmt.setString(1, networkId);
//            return stmt.executeUpdate() > 0;
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
}
