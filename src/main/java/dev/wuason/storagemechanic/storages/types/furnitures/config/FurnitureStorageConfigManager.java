package dev.wuason.storagemechanic.storages.types.furnitures.config;

import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.compatibilities.adapter.Adapter;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.utils.StorageUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class FurnitureStorageConfigManager {

    private StorageMechanic core;
    private HashMap<String, FurnitureStorageConfig> furnitureStorageConfigs = new HashMap<>();
    private HashMap<String, Map.Entry<String, FurnitureStorageConfig>> furnitureStorageConfigsAdapter = new HashMap<>();

    public FurnitureStorageConfigManager(StorageMechanic core) {
        this.core = core;
    }


    public void loadFurnitureStorageConfigs(){

        furnitureStorageConfigs = new HashMap<>();
        furnitureStorageConfigsAdapter = new HashMap<>();

        File base = new File(core.getDataFolder() + "/types/");
        base.mkdirs();

        File[] files = Arrays.stream(base.listFiles()).filter(f -> {

            if(f.getName().contains(".yml")) return true;

            return false;

        }).toArray(File[]::new);

        for(File file : files){


            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

            ConfigurationSection sectionFurnitureStorages = config.getConfigurationSection("storage.furniture");

            if(sectionFurnitureStorages != null){
                for(Object key : sectionFurnitureStorages.getKeys(false).toArray()){

                    if(!StorageUtils.isValidConfigId((String)key)){
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Furniture Storage Config! FurnitureStorage_id: " + key + " in file: " + file.getName());
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error: The FurnitureStorage id cannot contain \"_\" in the id!");
                        continue;
                    }

                    ConfigurationSection sectionFurnitureStorage = sectionFurnitureStorages.getConfigurationSection((String)key);

                    FurnitureStorageType furnitureStorageType;

                    try {
                        furnitureStorageType = FurnitureStorageType.valueOf(sectionFurnitureStorage.getString("type"));
                    }catch (IllegalArgumentException a){
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Furniture storage Config! FurnitureStorage_id: " + key + " in file: " + file.getName());
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error: Type is null or invalid");
                        continue;
                    }

                    //a
                    boolean breakable = sectionFurnitureStorage.getBoolean("properties.isBreakable",true);
                    boolean storageable = sectionFurnitureStorage.getBoolean("properties.isStorageable",false);
                    FurnitureStorageProperties furnitureStorageProperties = new FurnitureStorageProperties(breakable,storageable);

                    String furniture = sectionFurnitureStorage.getString("furniture",".");

                    if(furniture.equals(".") || !Adapter.getInstance().existAdapterID(furniture)){
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Furniture storage Config! FurnitureStorage_id: " + key + " in file: " + file.getName());
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error: Furniture is null or invalid");
                        continue;
                    }

                    String storage = sectionFurnitureStorage.getString("storage_id",".");

                    if(storage.equals(".") || !core.getManagers().getStorageConfigManager().existsStorageConfig(storage)){
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Furniture storage Config! FurnitureStorage_id: " + key + " in file: " + file.getName());
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error: Storage_ID is null or invalid");
                        continue;
                    }
                    /* MECHANICS OF FURNITURE
                    ArrayList<BlockMechanic> FurnitureMechanics = new ArrayList<>();

                    for(Object mechanicKey : sectionFurnitureStorage.getConfigurationSection("mechanics").getKeys(false)){

                        boolean enabled = sectionFurnitureStorage.getBoolean("mechanics." + mechanicKey,false);

                        if(enabled){

                            if(!core.getManagers().getBlockStorageManager().getBlockMechanicManager().mechanicExists((String)mechanicKey)){

                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Furniture storage mechanic Config! FurnitureStorage_id: " + key + "FurnitureMechanic_id: " + mechanicKey + " in file: " + file.getName());
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error: mechanic is invalid");
                                continue;

                            }

                            blockMechanics.add(core.getManagers().getBlockStorageManager().getBlockMechanicManager().getMechanic((String)mechanicKey));

                        }

                    } */
                    //blockMechanics.toArray(BlockMechanic[]::new)

                    FurnitureStorageConfig furnitureStorageConfig = new FurnitureStorageConfig(furnitureStorageType,(String)key,furnitureStorageProperties,furniture,storage);

                    furnitureStorageConfigs.put(furnitureStorageConfig.getId(),furnitureStorageConfig);

                }
            }
        }

        for(Map.Entry<String,FurnitureStorageConfig> configEntry : furnitureStorageConfigs.entrySet()){

            furnitureStorageConfigsAdapter.put(configEntry.getValue().getFurniture().toLowerCase(Locale.ENGLISH),configEntry);

        }

        AdventureUtils.sendMessagePluginConsole(core, "<aqua> FurnitureStorages loaded: <yellow>" + furnitureStorageConfigs.size());

    }


    public Collection<FurnitureStorageConfig> getFurnitureStorageConfigs() {
        return this.furnitureStorageConfigs.values();
    }

    // Método para agregar una nueva configuración de almacenamiento de bloques.
    public void addFurnitureStorageConfig(FurnitureStorageConfig config) {
        this.furnitureStorageConfigs.put(config.getId(),config);
    }

    // Método para eliminar una configuración de almacenamiento de bloques.
    public void removeFurnitureStorageConfig(FurnitureStorageConfig config) {
        this.furnitureStorageConfigs.remove(config.getId());
    }

    // Método para buscar una configuración de almacenamiento de bloques por su ID.
    public Optional<FurnitureStorageConfig> findFurnitureStorageConfigById(String id) {
        return Optional.of(furnitureStorageConfigs.getOrDefault(id,null));
    }

    public FurnitureStorageConfig findFurnitureStorageConfigByItemID(String id) {
        Map.Entry<String,FurnitureStorageConfig> entry = furnitureStorageConfigsAdapter.getOrDefault(id,null);
        return entry != null ? entry.getValue() : null;
    }

    // Método para comprobar si existe una configuración de almacenamiento de bloques por su ID.
    public boolean furnitureStorageConfigExists(String id) {
        return furnitureStorageConfigs.containsKey(id);
    }

}
