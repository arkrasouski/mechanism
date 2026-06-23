package org.example.artyom.mechanism.database;

import org.bukkit.Location;
import org.example.artyom.mechanism.Mechanism;
import org.example.artyom.mechanism.mechanism.cable.Cable;
import org.example.artyom.mechanism.mechanism.generator.Generator;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;
import org.example.artyom.mechanism.mechanism.network.NetworkManager;
import org.example.artyom.mechanism.utils.LogUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.bukkit.Bukkit.getServer;

public class CableRepository {
    private static DatabaseManager db = null;
    
    public CableRepository(DatabaseManager db){
        CableRepository.db = db;
    }

    // Добавить Кабель
    public static boolean addCable(INetworkElement element) {
        Cable cable = (Cable) element;

        String sql = """
        INSERT INTO cables
        (network_id, world_name, x, y, z) 
        VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, cable.getNetworkId().toString());
            stmt.setString(2, "Earth");
            stmt.setInt(3, (int) cable.getLocation().getX());
            stmt.setInt(4, (int) cable.getLocation().getY());
            stmt.setInt(5, (int) cable.getLocation().getZ());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Получить все кабели в сети
    public static List<INetworkElement> getCablesByNetwork(String networkId) {
        List<INetworkElement> cables = new ArrayList<>();
        String sql = "SELECT * FROM cables WHERE network_id = ?";

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, networkId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Cable cable = new Cable(new Location(
                        getServer().getWorld("world"),
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z")
                )

                );
                cable.setNetworkId(UUID.fromString(rs.getString("network_id")));
                cables.add(cable);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return cables;
    }

    // удалить кабели из сети
    public static boolean removeCablesByNetwork(String networkId) {
        List<Cable> cables = new ArrayList<>();
        String sql = "DELETE FROM cables WHERE network_id = ?";

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, networkId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Восстановить все генераторы из бд
     */
//    public static void restoreAllCables() {
//        LogUtil.info("Starting to restore all cables from database...");
//
//        // Получить все сети
//        List<NetworkManager> allNetworks = NetworkRepository.getAllNetworks();
//
//        int restoredCount = 0;
//
//        for (NetworkManager network : allNetworks) {
//            UUID networkId = network.getNetworkId();
//
//            // Получить все генераторы в этой сети
//            List<Cable> cables = getCablesByNetwork(networkId.toString());
//
//            for (Cable cable : cables) {
//                restoredCount++;
//                network.addElement(cable);
//                Mechanism.getCableManager().registerMechanism(cable,cable.getLocation());
//            }
//        }
//
//        LogUtil.info("Restored " + restoredCount + " cables from database");
//    }
}
