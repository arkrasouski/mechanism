package org.example.artyom.mechanism.mechanism;

import org.bukkit.Location;
import org.bukkit.Material;
import org.example.artyom.mechanism.IMechanismManager;
import org.example.artyom.mechanism.Mechanism;
import org.example.artyom.mechanism.items.BaseItem;
import org.example.artyom.mechanism.items.CableItem;
import org.example.artyom.mechanism.items.GeneratorItem;
import org.example.artyom.mechanism.mechanism.cable.Cable;
import org.example.artyom.mechanism.mechanism.functional_interfaces.*;
import org.example.artyom.mechanism.mechanism.generator.Generator;
import org.example.artyom.mechanism.mechanism.network.INetworkElement;

import java.util.*;

/**
 * Класс типа механизма
 * Использует функциональные интерфейсы для создания общих методов работы с механизмами, начиная с элементов сети
 */
public enum MechanismType  {
    CABLE(
            Material.PURPLE_STAINED_GLASS_PANE,
            "Кабель",
            "Супер мега кабель")
    ,
    GENERATOR(
              Material.DROPPER,
             "Генератор",
             "Супер мега генератор")
    ,

    //BARRIER(Barrier.class, Material.BARREL, "Барьер", "Супер мега барьер")
    ;
    private static final Map<MechanismType, IMechanismConstructor> registry = new HashMap<>();
    private static final Map<MechanismType, IMechanismItemConstructor> registryItem = new HashMap<>();
    private static final Map<MechanismType, IMechanismRepositoryConstructor> registryRepository = new HashMap<>();
    private static final Map<MechanismType, IMechanismRepositoryRemover> registryRepositoryRemover = new HashMap<>();
    private static final Map<MechanismType, IMechanismRepositoryMerge> registryRepositoryMerge = new HashMap<>();
    private static final Map<MechanismType, IMechanismManager> registryMechanismManager = new HashMap<>();

    static {
        registry.put(GENERATOR, Generator::new);
        registry.put(CABLE, Cable::new);

        registryItem.put(GENERATOR, GeneratorItem::new);
        registryItem.put(CABLE, CableItem::new);

//        registryRepository.put(GENERATOR, GeneratorRepository::addGenerator);
//        registryRepository.put(CABLE, CableRepository::addCable);
//
//        registryRepositoryRemover.put(GENERATOR, GeneratorRepository::removeGeneratorsByNetwork);
//        registryRepositoryRemover.put(CABLE, CableRepository::removeCablesByNetwork);
//
//        registryRepositoryMerge.put(GENERATOR, GeneratorRepository::getGeneratorsByNetwork);
//        registryRepositoryMerge.put(CABLE, CableRepository::getCablesByNetwork);
//
        registryMechanismManager.put(GENERATOR, Mechanism::getGeneratorManager);
        registryMechanismManager.put(CABLE, Mechanism::getCableManager);

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

    /**
     * Создает предмет нужного класса
     */
    public BaseItem create(Mechanism plugin) {return registryItem.get(this).create(plugin); }

    /**
     * Добавляет в бд нужный механизм
     */
//    public boolean addNetworkToDB(INetworkElement mechanism) {return registryRepository.get(this).add(mechanism);}
//
//    /**
//     * Удаляет все механизмы из сети, которая передается, в бд
//     */
//    public boolean removeFromPreviousNetwork(UUID networkId) {
//        return registryRepositoryRemover.get(this).remove(networkId.toString());
//    }
//
//    /**
//     * Получить все элементы типа механизма по id сети
//     */
//    public List<INetworkElement> getByNetwork(UUID networkId) {
//        return  registryRepositoryMerge.get(this).get(networkId.toString());
//    }

    /**
     * Получить менджер соответствующего механизма
     */
    public MechanismManager getMechanismManager(){
        return registryMechanismManager.get(this).getMechanism();
    }
}
