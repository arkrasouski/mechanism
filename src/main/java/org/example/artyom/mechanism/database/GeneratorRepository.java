package org.example.artyom.mechanism.database;

import com.google.common.graph.ImmutableNetwork;
import org.example.artyom.mechanism.mechanism.generator.Generator;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
}
