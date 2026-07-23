package org.example.artyom.mechanism.mechanism;

import org.bukkit.Location;
import org.bukkit.Material;
import org.example.artyom.mechanism.IMechanismManager;
import org.example.artyom.mechanism.Mechanism;
import org.example.artyom.mechanism.inventories.BarrierHolder;
import org.example.artyom.mechanism.inventories.GeneratorHolder;
import org.example.artyom.mechanism.inventories.EncoderHolder;
import org.example.artyom.mechanism.inventories.MechanismHolder;
import org.example.artyom.mechanism.items.BaseItem;
import org.example.artyom.mechanism.items.CableItem;
import org.example.artyom.mechanism.items.GeneratorItem;
import org.example.artyom.mechanism.items.BarrierItem;
import org.example.artyom.mechanism.items.EncoderItem;
import org.example.artyom.mechanism.mechanism.barrier.Barrier;
import org.example.artyom.mechanism.mechanism.base.Mech;
import org.example.artyom.mechanism.mechanism.cable.Cable;
import org.example.artyom.mechanism.mechanism.encoder.Encoder;
import org.example.artyom.mechanism.mechanism.functional_interfaces.*;
import org.example.artyom.mechanism.mechanism.generator.Generator;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.example.artyom.mechanism.utils.BlockUtil.extractLocation;

/**
 * Класс типа механизма
 * Использует функциональные интерфейсы для создания общих методов работы с механизмами, начиная с элементов сети
 */
public enum MechanismType  {
    CABLE(
            Material.PURPLE_STAINED_GLASS_PANE,
            "Кабель",
            "Супер мега кабель") {
        @Override
        public INetworkElement createFromResultSet(ResultSet rs) throws SQLException {
            Cable cable = new Cable(extractLocation(rs));
            cable.setNetworkId(UUID.fromString(rs.getString("network_id")));
            return cable;
        }
    },
    GENERATOR(
              Material.DROPPER,
             "Генератор",
             "Супер мега генератор") {
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
    },

    BARRIER(Material.BARREL,  "Барьер", "Супер мега барьер") {
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
    },
    ENCODER(Material.NOTE_BLOCK, "Шифратор", "Супер мега шифратор"){
        @Override
        public INetworkElement createFromResultSet(ResultSet rs) throws SQLException {
            Encoder encoder = new Encoder(
                    extractLocation(rs),
                    rs.getInt("current_energy")
            );
            encoder.setNetworkId(UUID.fromString(rs.getString("network_id")));
            return encoder;
        }
    }
    ;
    private static final Map<MechanismType, IMechanismConstructor> registry = new HashMap<>();
    private static final Map<MechanismType, IMechanismItemConstructor> registryItem = new HashMap<>();
    private static final Map<MechanismType, IMechanismMechsByNetwork> registryMechsByNetwork = new HashMap<>();
    private static final Map<MechanismType, IMechanismRepositoryRemover> registryRepositoryRemover = new HashMap<>();
    private static final Map<MechanismType, IMechanismRepositoryMerge> registryRepositoryMerge = new HashMap<>();
    private static final Map<MechanismType, IMechanismManager> registryMechanismManager = new HashMap<>();
    private static final Map<MechanismType, IMechanismHolderCreate> registryMechanismHolder = new HashMap<>();

    static {
        registry.put(GENERATOR, Generator::new);
        registry.put(CABLE, Cable::new);
        registry.put(BARRIER, Barrier::new);
        registry.put(ENCODER, Encoder::new);

        registryItem.put(GENERATOR, GeneratorItem::new);
        registryItem.put(CABLE, CableItem::new);
        registryItem.put(BARRIER, BarrierItem::new);
        registryItem.put(ENCODER, EncoderItem::new);

        registryMechsByNetwork.put(GENERATOR, Mechanism::getGeneratorsByNetwork);
        registryMechsByNetwork.put(CABLE, Mechanism::getCablesByNetwork);
        registryMechsByNetwork.put(BARRIER, Mechanism::getBarriersByNetwork);
        registryMechsByNetwork.put(ENCODER, Mechanism::getEncodersByNetwork);

        registryMechanismManager.put(GENERATOR, Mechanism::getGeneratorManager);
        registryMechanismManager.put(CABLE, Mechanism::getCableManager);
        registryMechanismManager.put(BARRIER, Mechanism::getBarrierManager);
        registryMechanismManager.put(ENCODER, Mechanism::getEncoderManager);

        registryMechanismHolder.put(GENERATOR, GeneratorHolder::new);
        registryMechanismHolder.put(BARRIER, BarrierHolder::new);
        registryMechanismHolder.put(ENCODER, EncoderHolder::new);
    }

    private final Material material;
    private final String displayName;
    private final String guiLore;



    MechanismType(
            Material material,
            String displayName,
            String guiLore
    ) {
        this.material = material;
        this.displayName = displayName;
        this.guiLore = guiLore;
    }

    public Material getMaterial() { return material; }
    public String getDisplayName() { return "§6⚡" + displayName + "⚡"; }
    public String getGuiLore() {return "§7" + guiLore + "!"; }
    public abstract INetworkElement createFromResultSet(ResultSet rs) throws SQLException;
    /**
     * Создает объект нужного класса
     */
    public INetworkElement create(Location loc) {
        return registry.get(this).create(loc);
    }

    /**
     * Создает предмет нужного класса
     */
    public BaseItem create(Mechanism plugin) {return registryItem.get(this).create(plugin); }


    /**
     * Получить менджер соответствующего механизма
     */
    public MechanismManager getMechanismManager(){
        return registryMechanismManager.get(this).getMechanism();
    }

    /**
     * Получить менеджер сеть->механизмы для типа механизма
     */
    public Map<UUID, List<INetworkElement>> getMechsByNetwork() {return registryMechsByNetwork.get(this).getMechanismByNetwork();}
    /**
     * Получить холдер инвентаря для типа механизма
     */
    public MechanismHolder getMechanismHolder(Mech mechanism) {
        return registryMechanismHolder.get(this).getHolder(mechanism);
    }
}
