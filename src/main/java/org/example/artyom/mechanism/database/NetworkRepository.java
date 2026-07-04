package org.example.artyom.mechanism.database;



import com.google.common.graph.Network;
import org.apache.commons.logging.Log;
import org.bukkit.Bukkit;
import org.example.artyom.mechanism.Mechanism;
import org.example.artyom.mechanism.items.BaseItem;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;
import org.example.artyom.mechanism.mechanism.network.NetworkManager;
import org.example.artyom.mechanism.mechanism.network.NetworkSystems;
import org.example.artyom.mechanism.utils.LogUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NetworkRepository {
    private final DatabaseConnectionPool pool;
    private final MechanismRepository mechanismRepository;

    public NetworkRepository(DatabaseConnectionPool pool, MechanismRepository mechanismRepository) {
        this.pool = pool;
        this.mechanismRepository = mechanismRepository;
    }

    // Создать новую сеть
    public boolean createNetwork(Connection connection, NetworkManager network) throws SQLException{
        String sql = "INSERT INTO networks (network_id, world_name) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql))
        {
            LogUtil.warn("Пусто" + network.getNetworkId().toString());
            stmt.setString(1, network.getNetworkId().toString());
            stmt.setString(2, network.getWorld().getName());//network.getWorldName());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    // Получить сеть по ID
    public NetworkManager getNetworkById(String networkId) {
        String sql = "SELECT * FROM networks WHERE network_id = ?";

        try (PreparedStatement stmt = pool.getConnection().prepareStatement(sql)) {
            stmt.setString(1, networkId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new NetworkManager(UUID.fromString(rs.getString("network_id")), Bukkit.getServer().getWorld(rs.getString("world_name")));
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

        try (PreparedStatement stmt = pool.getConnection().prepareStatement(sql)) {
            stmt.setString(1, worldName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                NetworkManager network = new NetworkManager(UUID.fromString(rs.getString("network_id")), Bukkit.getServer().getWorld(rs.getString("world_name")));
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

        try (PreparedStatement stmt = pool.getConnection().prepareStatement(sql)) {
            stmt.setString(1, networkId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Получить все сети из бд
     */

    public List<NetworkManager> getAllNetworks() {
        List<NetworkManager> networks = new ArrayList<>();
        String sql = "SELECT * FROM networks";

        try (Statement stmt = pool.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                NetworkManager network = new NetworkManager(
                        UUID.fromString(rs.getString("network_id")),
                        Bukkit.getServer().getWorld(rs.getString("world_name"))
                );
                Mechanism.getNetworkSystems().addNetworkManager(network);
                networks.add(network);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return networks;
    }

    /**
     * Удаляет старые сети
     */
    public boolean deleteSecondaryNetworks(
            Connection connection,
            List<UUID> secondaryNetworkIds
    ) throws SQLException {

                String placeholders = secondaryNetworkIds.stream()
                        .map(id -> "?")
                        .collect(java.util.stream.Collectors.joining(", ", "(", ")"));

                String sql = "DELETE FROM networks WHERE network_id IN %s".formatted(placeholders);
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    int index = 1;
                    for (UUID id : secondaryNetworkIds) {
                        ps.setString(index++, id.toString());
                    }
                    int updated = ps.executeUpdate();
                    LogUtil.warn("Deleted rows: " + updated);
                    return updated > 0;
                }
    }

    /**
     * Функция обновления элементов сетей при склейке и удалении старых сетей
     */
    public void mergeNetworks(
            UUID primaryNetworkId,
            List<UUID> secondaryNetworkIds,
            INetworkElement newElement
    ) throws SQLException {
        try (Connection connection = pool.getConnection()) {
            connection.setAutoCommit(false);

            try {
                mechanismRepository.addMechanism(connection, newElement);


                mechanismRepository.batchUpdateMechanismNetworks(connection, primaryNetworkId, secondaryNetworkIds);
                LogUtil.warn("Перехожу к delete");

                deleteSecondaryNetworks(connection, secondaryNetworkIds);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }
}
