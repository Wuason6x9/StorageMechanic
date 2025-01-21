package dev.wuason.storagemechanic.storages.types.block.config;

import dev.wuason.libs.adapter.Adapter;
import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.storages.types.block.mechanics.BlockMechanic;
import dev.wuason.storagemechanic.storages.types.block.mechanics.BlockMechanicManager;
import dev.wuason.storagemechanic.storages.types.block.mechanics.integrated.hopper.HopperBlockMechanic;
import dev.wuason.storagemechanic.storages.types.furnitures.config.FurnitureStorageConfig;
import dev.wuason.storagemechanic.utils.StorageUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class BlockStorageConfigManager {

    private StorageMechanic core;
    private HashMap<String,BlockStorageConfig> blockStorageConfigs = new HashMap<>();
    private HashMap<String, Map.Entry<String, BlockStorageConfig>> blockStorageConfigsAdapter = new HashMap<>();

    public BlockStorageConfigManager(StorageMechanic core) {
        this.core = core;
    }

    public void loadBlockStorageConfigs(){

        blockStorageConfigs = new HashMap<>();
        blockStorageConfigsAdapter = new HashMap<>();

        File base = new File(core.getDataFolder() + "/types/");
        base.mkdirs();

        File[] files = Arrays.stream(base.listFiles()).filter(f -> {

            if(f.getName().contains(".yml")) return true;

            return false;

        }).toArray(File[]::new);

        for(File file : files){


            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

            ConfigurationSection sectionBlockStorages = config.getConfigurationSection("storage.block");

            if(sectionBlockStorages != null){
                for(Object key : sectionBlockStorages.getKeys(false).toArray()){

                    if(!StorageUtils.isValidConfigId((String)key)){
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Block Storage Config! Blockstorage_id: " + key + " in file: " + file.getName());
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error: The Blockstorage id cannot contain \"_\" in the id!");
                        continue;
                    }

                    ConfigurationSection sectionBlockStorage = sectionBlockStorages.getConfigurationSection((String)key);

                    BlockStorageType blockStorageType;

                    try {
                        blockStorageType = BlockStorageType.valueOf(sectionBlockStorage.getString("type"));
                    }catch (IllegalArgumentException a){
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Block storage Config! blockstorage_id: " + key + " in file: " + file.getName());
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error: Type is null or invalid");
                        continue;
                    }
                    boolean breakable = sectionBlockStorage.getBoolean("properties.isBreakable",true);
                    boolean storageable = sectionBlockStorage.getBoolean("properties.isStorageable",false);
                    BlockStorageProperties blockStorageProperties = new BlockStorageProperties(breakable,storageable);
                    BlockStorageClickType blockStorageClickType;
                    try {
                        blockStorageClickType = BlockStorageClickType.valueOf(sectionBlockStorage.getString("open"));
                    }catch (IllegalArgumentException a){
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Block storage Config! blockstorage_id: " + key + " in file: " + file.getName());
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error: Open is null or invalid");
                        continue;
                    }

                    String block = sectionBlockStorage.getString("block",".");

                    if(block.equals(".") || !Adapter.isValid(block)){
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Block storage Config! blockstorage_id: " + key + " in file: " + file.getName());
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error: Block is null or invalid");
                        continue;
                    }

                    String storage = sectionBlockStorage.getString("storage_id",".");

                    if(storage.equals(".") || !core.getManagers().getStorageConfigManager().existsStorageConfig(storage)){
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Block storage Config! blockstorage_id: " + key + " in file: " + file.getName());
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error: Storage_ID is null or invalid");
                        continue;
                    }

                    HashMap<String,BlockStorageMechanicConfig> mechanicConfigHashMap = new HashMap<>();
                    BlockMechanicManager blockMechanicManager = core.getManagers().getBlockMechanicManager();
                    ConfigurationSection configurationMechanicsSection = sectionBlockStorage.getConfigurationSection("mechanics");
                    if(configurationMechanicsSection != null){
                        for(Object mechanicKey : configurationMechanicsSection.getKeys(false)){

                            if(!blockMechanicManager.mechanicExists(((String)mechanicKey).toUpperCase(Locale.ENGLISH))) {
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Block storage mechanic Config! blockstorage_id: " + key + "BlockMechanic_id: " + mechanicKey + " in file: " + file.getName());
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error: mechanic is invalid");
                                continue;
                            };
                            BlockMechanic blockMechanic = blockMechanicManager.getMechanic(((String)mechanicKey).toUpperCase(Locale.ENGLISH));
                            boolean enabled = sectionBlockStorage.getBoolean("mechanics." + mechanicKey + ".enabled", false);
                            if(!blockStorageType.equals(BlockStorageType.CHEST) && !blockStorageType.equals(BlockStorageType.SHULKER)) enabled = false;
                            BlockMechanicProperties blockMechanicProperties = null;

                            switch (blockMechanic.getId()){
                                case HopperBlockMechanic.HOPPER_MECHANIC_KEY -> {

                                    long tick = sectionBlockStorage.getLong("mechanics." + mechanicKey + ".properties.transfer_tick", 8);
                                    int transferAmount = sectionBlockStorage.getInt("mechanics." + mechanicKey + ".properties.transfer_amount", 1);

                                    blockMechanicProperties = new BlockHopperMechanicProperties(tick,transferAmount);

                                }

                            }
                            BlockStorageMechanicConfig blockStorageMechanicConfig = new BlockStorageMechanicConfig(enabled,blockMechanicProperties,blockMechanic);
                            mechanicConfigHashMap.put(blockMechanic.getId(),blockStorageMechanicConfig);
                        }
                    }

                    BlockStorageConfig blockStorageConfig = new BlockStorageConfig(blockStorageType,(String)key,blockStorageProperties,blockStorageClickType,block,storage,mechanicConfigHashMap);

                    blockStorageConfigs.put(blockStorageConfig.getId(),blockStorageConfig);

                }
            }
        }

        for(Map.Entry<String, BlockStorageConfig> configEntry : blockStorageConfigs.entrySet()){
            blockStorageConfigsAdapter.put(configEntry.getValue().getBlock(), configEntry);
        }

        AdventureUtils.sendMessagePluginConsole(core, "<aqua> BlockStorages loaded: <yellow>" + blockStorageConfigs.size());

    }


    public Collection<BlockStorageConfig> getBlockStorageConfigs() {
        return this.blockStorageConfigs.values();
    }

    // Método para agregar una nueva configuración de almacenamiento de bloques.
    public void addBlockStorageConfig(BlockStorageConfig config) {
        this.blockStorageConfigs.put(config.getId(),config);
    }

    // Método para eliminar una configuración de almacenamiento de bloques.
    public void removeBlockStorageConfig(BlockStorageConfig config) {
        this.blockStorageConfigs.remove(config.getId());
    }

    // Método para buscar una configuración de almacenamiento de bloques por su ID.
    public Optional<BlockStorageConfig> findBlockStorageConfigById(String id) {
        return Optional.of(blockStorageConfigs.get(id));
    }
    public BlockStorageConfig getBlockStorageConfig(String id) {
        return blockStorageConfigs.get(id);
    }



    public BlockStorageConfig findBlockStorageConfigByItemID(String id) {
        Map.Entry<String,BlockStorageConfig> entry = blockStorageConfigsAdapter.getOrDefault(id,null);
        return entry != null ? entry.getValue() : null;
    }

    // Método para comprobar si existe una configuración de almacenamiento de bloques por su ID.
    public boolean blockStorageConfigExists(String id) {
        return blockStorageConfigs.containsKey(id);
    }
    public HashMap<String,BlockStorageConfig> getBlockStorageConfigHashMap(){
        return this.blockStorageConfigs;
    }
    public HashMap<String, Map.Entry<String, BlockStorageConfig>> getBlockStorageConfigsAdapter() {
        return blockStorageConfigsAdapter;
    }
}
