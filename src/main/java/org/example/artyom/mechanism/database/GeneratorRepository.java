package org.example.artyom.mechanism.database;

import com.google.common.graph.ImmutableNetwork;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.example.artyom.mechanism.Mechanism;
import org.example.artyom.mechanism.mechanism.MechanismManager;
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

public class GeneratorRepository {
    private static DatabaseManager db = null;

    public GeneratorRepository(DatabaseManager db) {
        GeneratorRepository.db = db;
    }

    // Добавить генератор
    public static boolean addGenerator(INetworkElement mechanism) {
        Generator generator = (Generator) mechanism;

        String sql = """
        INSERT INTO generators 
        (network_id, world_name, x, y, z, is_working, current_energy) 
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, generator.getNetworkId().toString());
            stmt.setString(2, "Earth");
            stmt.setInt(3, (int) generator.getLocation().getX());
            stmt.setInt(4, (int) generator.getLocation().getY());
            stmt.setInt(5, (int) generator.getLocation().getZ());
            stmt.setBoolean(6, generator.isWorking());
            //stmt.setString(6, generator.getGeneratorType());
            stmt.setDouble(7, generator.getCurrentEnergy());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Получить все генераторы в сети
    public static List<Generator> getGeneratorsByNetwork(String networkId) {
        List<Generator> generators = new ArrayList<>();
        String sql = "SELECT * FROM generators WHERE network_id = ?";

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, networkId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Generator generator = new Generator(new Location(
                        getServer().getWorld("world"),
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z")
                )

                );
                generator.setCurrentEnergy(rs.getInt("current_energy"));
                generator.setNetworkId(UUID.fromString(rs.getString("network_id")));
                generators.add(generator);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return generators;
    }

    // удалить генераторы из сети
    public static boolean removeGeneratorsByNetwork(String networkId) {
        List<Generator> generators = new ArrayList<>();
        String sql = "DELETE FROM generators WHERE network_id = ?";

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
    public static void restoreAllGenerators() {
        LogUtil.info("Starting to restore all generators from database...");

        // Получить все сети
        List<NetworkManager> allNetworks = NetworkRepository.getAllNetworks();

        int restoredCount = 0;

        for (NetworkManager network : allNetworks) {
            UUID networkId = network.getNetworkId();

            // Получить все генераторы в этой сети
            List<Generator> generators = getGeneratorsByNetwork(networkId.toString());

            for (Generator generator : generators) {
                restoredCount++;
                network.addElement(generator);
                Mechanism.getGeneratorManager().registerMechanism(generator, generator.getLocation());
            }
        }

        LogUtil.info("Restored " + restoredCount + " generators from database");
    }
}
