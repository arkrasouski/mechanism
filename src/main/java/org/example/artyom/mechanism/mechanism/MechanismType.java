package org.example.artyom.mechanism.mechanism;

import org.bukkit.Material;

public enum MechanismType {
    GENERATOR(Material.DROPPER, "Генератор", "Супер мега генератор"),
    CABLE(Material.PURPLE_STAINED_GLASS_PANE, "Кабель", "Супер мега кабель"),
    BARRIER(Material.BARREL, "Барьер", "Супер мега барьер")
    ;

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
    public String getGuiLore() {return "§7" + guiLore + "!"; }
}
