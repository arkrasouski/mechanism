package org.example.artyom.mechanism.database;

import org.bukkit.Location;
import org.example.artyom.mechanism.mechanism.base.Mech;
import org.example.artyom.mechanism.mechanism.cable.Cable;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;
import org.example.artyom.mechanism.mechanism.network.NetworkManager;
import org.example.artyom.mechanism.utils.LogUtil;

import java.sql.*;
import java.util.*;

import static org.bukkit.Bukkit.getServer;

public class MechanismRepository {

    /**
     * Добавить механизм в бд
     */
    public boolean addMechanism(Connection connection, INetworkElement networkElement) throws SQLException {
        int type = networkElement.getMechanismType().ordinal();
        String network_id = networkElement.getNetworkId().toString();
        String world_name = networkElement.getLocation().getWorld().getName();
        int x = networkElement.getLocation().getBlockX();
        int y = networkElement.getLocation().getBlockY();
        int z = networkElement.getLocation().getBlockZ();
        boolean is_working;
        int current_energy;

        if (networkElement instanceof Mech mechanism) {
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
        }
    }

    /**
     * Обновить сеть всех механизмов из старых сетей
     */
    public boolean batchUpdateMechanismNetworks(
            Connection connection,
            UUID primaryNetworkId,
            List<UUID> secondaryNetworkIds
    ) throws SQLException {

            String placeholders = secondaryNetworkIds.stream()
                    .map(id -> "?")
                    .collect(java.util.stream.Collectors.joining(", ", "(", ")"));

            String sql = "UPDATE mechanism SET network_id = ? WHERE network_id IN %s".formatted(placeholders);

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, primaryNetworkId.toString());
                int index = 2;
                for (UUID id : secondaryNetworkIds) {
                    ps.setString(index++, id.toString());
                }
                int updated = ps.executeUpdate();
                LogUtil.warn("Updated rows: " + updated);
                return updated > 0;
            }

    }

    /**
     * Обновить сеть для конкретных механизмов
     */
    public boolean batchUpdateMechanismLocNetworks(
            Connection connection,
            Set<INetworkElement> elements,
            UUID newNetworkId
    ) throws SQLException {
        if (elements.isEmpty()) return true;

        String sql = "UPDATE mechanism SET network_id = ? " +
                "WHERE world_name = ? AND x = ? AND y = ? AND z = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (INetworkElement element : elements) {
                Location loc = element.getLocation();
                ps.setString(1, newNetworkId.toString());
                ps.setString(2, loc.getWorld().getName());
                ps.setInt(3, loc.getBlockX());
                ps.setInt(4, loc.getBlockY());
                ps.setInt(5, loc.getBlockZ());
                ps.addBatch();
            }

            int[] results = ps.executeBatch();
            return results.length > 0;
        }
    }
    /**
     * Удалить механизм из бд
     */

    public  boolean deleteMechanism(Connection connection, Location loc) throws SQLException {
        String sql = """
        
                DELETE FROM mechanism
        WHERE world_name = ? AND x = ? AND y = ? AND z = ?
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, loc.getWorld().getName());
            ps.setInt(2, loc.getBlockX());
            ps.setInt(3, loc.getBlockY());
            ps.setInt(4, loc.getBlockZ());
            return ps.executeUpdate() > 0;
        }
    }

//    // Получить все генераторы в сети
//    public static List<INetworkElement> getGeneratorsByNetwork(String networkId) {
//        List<INetworkElement> generators = new ArrayList<>();
//        String sql = "SELECT * FROM generators WHERE network_id = ?";
//
//        try (PreparedStatement stmt = pool.getConnection().prepareStatement(sql)) {
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
//        try (PreparedStatement stmt = pool.getConnection().prepareStatement(sql)) {
//            stmt.setString(1, networkId);
//            return stmt.executeUpdate() > 0;
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
}
