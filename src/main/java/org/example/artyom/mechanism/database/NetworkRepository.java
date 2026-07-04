package org.example.artyom.mechanism.database;



import com.google.common.graph.Network;
import org.apache.commons.logging.Log;
import org.example.artyom.mechanism.Mechanism;
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
    public boolean createNetwork(NetworkManager network) {
        String sql = "INSERT INTO networks (network_id, world_name) VALUES (?, ?)";

        LogUtil.warn(sql);
        pool.printPoolStats();
        try (Connection conn = pool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
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

        try (PreparedStatement stmt = pool.getConnection().prepareStatement(sql)) {
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

        try (PreparedStatement stmt = pool.getConnection().prepareStatement(sql)) {
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
                        UUID.fromString(rs.getString("network_id"))
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
    private void deleteSecondaryNetworks(
            Connection connection,
            List<UUID> secondaryNetworkIds
    ) throws SQLException {

                LogUtil.warn("Перед плейсхолдером");
                LogUtil.warn("" + secondaryNetworkIds.size());
                String placeholders = secondaryNetworkIds.stream()
                        .map(id -> "?")
                        .collect(java.util.stream.Collectors.joining(", ", "(", ")"));

                String sql = "DELETE FROM networks WHERE network_id IN %s".formatted(placeholders);
                LogUtil.warn(sql);
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    int index = 1;
                    for (UUID id : secondaryNetworkIds) {
                        ps.setString(index++, id.toString());
                    }
                    int updated = ps.executeUpdate();
                    connection.commit();
                    LogUtil.warn("Deleted rows: " + updated);
                }

            catch (SQLException e){
                connection.rollback();
                throw e;
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

                LogUtil.warn("Здесь был");
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
