package org.example.artyom.mechanism.mechanism;

import org.bukkit.Location;
import org.bukkit.Material;
import org.example.artyom.mechanism.mechanism.cable.Cable;
import org.example.artyom.mechanism.mechanism.generator.Generator;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;

import java.util.HashMap;
import java.util.Map;

public enum MechanismType  {
    GENERATOR(
              Material.DROPPER,
             "Генератор",
             "Супер мега генератор")
    ,
    CABLE(
            Material.PURPLE_STAINED_GLASS_PANE,
            "Кабель",
            "Супер мега кабель")
    ,
    //BARRIER(Barrier.class, Material.BARREL, "Барьер", "Супер мега барьер")
    ;
    private static final Map<MechanismType, IMechanismConstructor> registry = new HashMap<>();

    static {
        registry.put(GENERATOR, Generator::new);
        registry.put(CABLE, Cable::new);
//        registry.put(IRON, loc -> new IronMechanism(loc));
//        registry.put(GOLD, loc -> new GoldMechanism(loc));
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

    /**
     * Создает объект нужного класса
     */
    public INetworkElement create(Location loc) {
        return registry.get(this).create(loc);
    }
}
