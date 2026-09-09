package org.example.artyom.mechanism.mechanism;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.example.artyom.mechanism.IMechanismManager;
import org.example.artyom.mechanism.Mechanism;
import org.example.artyom.mechanism.inventories.barrier.BarrierHolder;
import org.example.artyom.mechanism.inventories.encoder.EncoderHolder;
import org.example.artyom.mechanism.inventories.generator.GeneratorHolder;
import org.example.artyom.mechanism.inventories.MechanismHolder;
import org.example.artyom.mechanism.items.*;
import org.example.artyom.mechanism.mechanism.barrier.Barrier;
import org.example.artyom.mechanism.mechanism.base.Mech;
import org.example.artyom.mechanism.mechanism.cable.Cable;
import org.example.artyom.mechanism.mechanism.encoder.Encoder;
import org.example.artyom.mechanism.mechanism.generator.Generator;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.example.artyom.mechanism.utils.BlockUtil.extractLocation;

/**
 * Класс типа механизма
 */
public enum MechanismType {
    CABLE(
            Material.PURPLE_STAINED_GLASS_PANE,
            "Кабель",
            "Супер мега кабель"
    ) {
        @Override
        public INetworkElement createFromResultSet(ResultSet rs) throws SQLException {
            Cable cable = new Cable(extractLocation(rs));
            cable.setNetworkId(UUID.fromString(rs.getString("network_id")));
            return cable;
        }

        @Override
        public INetworkElement create(Location loc) {
            return new Cable(loc);
        }

        @Override
        public BaseItem createItem(Mechanism plugin) {
            return new CableItem(plugin);
        }

        @Override
        public MechanismManager getMechanismManager() {
            return Mechanism.getCableManager();
        }

        @Override
        public Map<UUID, List<INetworkElement>> getMechsByNetwork() {
            return Mechanism.getCablesByNetwork();
        }

        @Override
        public MechanismHolder createHolder(Mech mechanism, Player player) {
            // Cable, возможно, не имеет GUI; можно бросить UnsupportedOperationException
            throw new UnsupportedOperationException("Cable has no GUI holder");
        }
    },

    GENERATOR(
            Material.DROPPER,
            "Генератор",
            "Супер мега генератор"
    ) {
        @Override
        public INetworkElement createFromResultSet(ResultSet rs) throws SQLException {
            Generator generator = new Generator(
                    extractLocation(rs),
                    rs.getInt("current_energy"),
                    rs.getBoolean("is_working")
            );
            generator.setNetworkId(UUID.fromString(rs.getString("network_id")));
            return generator;
        }

        @Override
        public INetworkElement create(Location loc) {
            return new Generator(loc, 0, true); // или конструктор без энергии
        }

        @Override
        public BaseItem createItem(Mechanism plugin) {
            return new GeneratorItem(plugin);
        }

        @Override
        public MechanismManager getMechanismManager() {
            return Mechanism.getGeneratorManager();
        }

        @Override
        public Map<UUID, List<INetworkElement>> getMechsByNetwork() {
            return Mechanism.getGeneratorsByNetwork();
        }

        @Override
        public MechanismHolder createHolder(Mech mechanism, Player player) {
            return new GeneratorHolder(mechanism, player);
        }
    },
    BARRIER(
            Material.BARREL,
            "Барьер",
            "Супер мега барьер"
    ) {
        @Override
        public INetworkElement createFromResultSet(ResultSet rs) throws SQLException {
            Barrier barrier = new Barrier(
                    extractLocation(rs),
                    rs.getInt("current_energy"),
                    rs.getBoolean("is_working")
            );

            barrier.setNetworkId(UUID.fromString(rs.getString("network_id")));
            return barrier;
        }

        @Override
        public INetworkElement create(Location loc) {
            // Подставь значения по умолчанию, соответствующие твоему конструктору.
            return new Barrier(loc, 0, false);
        }

        @Override
        public BaseItem createItem(Mechanism plugin) {
            return new BarrierItem(plugin);
        }

        @Override
        public MechanismManager getMechanismManager() {
            return Mechanism.getBarrierManager();
        }

        @Override
        public Map<UUID, List<INetworkElement>> getMechsByNetwork() {
            return Mechanism.getBarriersByNetwork();
        }

        @Override
        public MechanismHolder createHolder(Mech mechanism, Player player) {
            return new BarrierHolder(mechanism, player);
        }
    },

    ENCODER(
            Material.NOTE_BLOCK,
            "Шифратор",
            "Супер мега шифратор"
    ) {
        @Override
        public INetworkElement createFromResultSet(ResultSet rs) throws SQLException {
            Encoder encoder = new Encoder(
                    extractLocation(rs),
                    rs.getInt("current_energy")
            );

            encoder.setNetworkId(UUID.fromString(rs.getString("network_id")));
            return encoder;
        }

        @Override
        public INetworkElement create(Location loc) {
            // Подставь значения по умолчанию, соответствующие твоему конструктору.
            return new Encoder(loc, 0);
        }

        @Override
        public BaseItem createItem(Mechanism plugin) {
            return new EncoderItem(plugin);
        }

        @Override
        public MechanismManager getMechanismManager() {
            return Mechanism.getEncoderManager();
        }

        @Override
        public Map<UUID, List<INetworkElement>> getMechsByNetwork() {
            return Mechanism.getEncodersByNetwork();
        }

        @Override
        public MechanismHolder createHolder(Mech mechanism, Player player) {
            return new EncoderHolder(mechanism, player);
        }
    };

    private final Material material;
    private final String displayName;
    private final String guiLore;

    MechanismType(Material material, String displayName, String guiLore) {
        this.material = material;
        this.displayName = displayName;
        this.guiLore = guiLore;
    }

    public Material getMaterial() { return material; }
    public String getDisplayName() { return "§6⚡" + displayName + "⚡"; }
    public String getGuiLore() { return "§7" + guiLore + "!"; }

    public abstract INetworkElement createFromResultSet(ResultSet rs) throws SQLException;
    public abstract INetworkElement create(Location loc);
    public abstract BaseItem createItem(Mechanism plugin);
    public abstract MechanismManager getMechanismManager();
    public abstract Map<UUID, List<INetworkElement>> getMechsByNetwork();
    public abstract MechanismHolder createHolder(Mech mechanism, Player player);
}
