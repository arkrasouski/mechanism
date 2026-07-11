package org.example.artyom.mechanism.database;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;
import org.example.artyom.mechanism.records.PlayerData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerRepository {
    public static void upsertPlayer(Connection connection, Player player) throws SQLException {
        String sql = """
        INSERT INTO players (uuid, name, last_join)
        VALUES (?, ?, ?)
        ON CONFLICT(uuid) DO UPDATE SET
            name = excluded.name,
            last_join = CURRENT_TIMESTAMP
    """;

        long now = System.currentTimeMillis();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            UUID uuid = player.getUniqueId();
            String name = player.getName();
            ps.setObject(1, uuid);
            ps.setString(2, name);
            ps.setLong(3, now);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static List<PlayerData> getPlayers(Connection connection, int page) throws SQLException {
        final int LIMIT = 18;
        int offset = (page - 1) * LIMIT;

        String sql = String.format("""
                SELECT 
                    uuid,
                    name
                FROM players
                ORDER BY name
                LIMIT %d
                OFFSET %d
        """, LIMIT, offset);

        List<PlayerData> result = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    String name = rs.getString("name");
                    result.add(new PlayerData(uuid, name));
                }
            }
        }

        return result;
    }
}
