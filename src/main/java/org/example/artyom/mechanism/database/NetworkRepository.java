package org.example.artyom.mechanism.database;



import org.example.artyom.mechanism.mechanism.network.NetworkManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NetworkRepository {
    private final DatabaseManager db;

    public NetworkRepository(DatabaseManager db) {
        this.db = db;
    }

    // Создать новую сеть
    public boolean createNetwork(NetworkManager network) {
        String sql = "INSERT INTO networks (network_id, world_name) VALUES (?, ?)";

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, network.getNetworkId().toString());
            stmt.setString(2, "Earth");//network.getWorldName());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Получить сеть по ID
    public NetworkManager getNetworkById(String networkId) {
        String sql = "SELECT * FROM networks WHERE network_id = ?";

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, networkId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new NetworkManager(UUID.fromString(rs.getString("network_id")));
                // Нужно добавить конструктор с всеми полями
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Получить все сети в мире
    public List<NetworkManager> getNetworkByWorld(String worldName) {
        List<NetworkManager> networks = new ArrayList<>();
        String sql = "SELECT * FROM networks WHERE world_name = ?";

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, worldName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                NetworkManager network = new NetworkManager(UUID.fromString(rs.getString("world_name")));
                networks.add(network);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return networks;
    }

    // Удалить сеть
    public boolean deleteNetwork(String networkId) {
        String sql = "DELETE FROM networks WHERE network_id = ?";

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, networkId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
