package dev.wuason.storagemechanic.storages.types.block.config;

import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.utils.Adapter;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.customblocks.CustomBlock;
import dev.wuason.storagemechanic.storages.types.block.mechanics.BlockMechanic;
import dev.wuason.storagemechanic.utils.StorageUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlockStorageConfigManager {

    private StorageMechanic core;
    private ArrayList<BlockStorageConfig> blockStorageConfigs;

    public BlockStorageConfigManager(StorageMechanic core) {
        this.core = core;
    }

    public void loadBlockStorageConfigs(){

        blockStorageConfigs = new ArrayList<>();

        File base = new File(Mechanics.getInstance().getMechanicsManager().getMechanic(core).getDirConfig().getPath() + "/StoragesBlocks/");
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
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Storage Config! storage_id: " + key + " in file: " + file.getName());
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error: The storage id cannot contain \"_\" in the id!");
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
                    BlockStorageProperties blockStorageProperties = new BlockStorageProperties(sectionBlockStorage.getBoolean("properties.isBreakable",true));
                    BlockStorageClickType blockStorageClickType;
                    try {
                        blockStorageClickType = BlockStorageClickType.valueOf(sectionBlockStorage.getString("open"));
                    }catch (IllegalArgumentException a){
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Block storage Config! blockstorage_id: " + key + " in file: " + file.getName());
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error: Open is null or invalid");
                        continue;
                    }

                    String block = sectionBlockStorage.getString("block",".");

                    if(block.equals(".") || !Adapter.isItemValid(block)){
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Block storage Config! blockstorage_id: " + key + " in file: " + file.getName());
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error: Block is null or invalid");
                        continue;
                    }

                    String storage = sectionBlockStorage.getString("storage_id",".");

                    if(block.equals(".") || !core.getManagers().getStorageConfigManager().existsStorageConfig(storage)){
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Block storage Config! blockstorage_id: " + key + " in file: " + file.getName());
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error: Storage_ID is null or invalid");
                        continue;
                    }

                    ArrayList<BlockMechanic> blockMechanics = new ArrayList<>();

                    for(Object mechanicKey : sectionBlockStorage.getConfigurationSection("mechanics").getKeys(false)){

                        boolean enabled = sectionBlockStorage.getBoolean("mechanics." + mechanicKey,false);

                        if(enabled){

                            if(!core.getManagers().getBlockStorageManager().getBlockMechanicManager().mechanicExists((String)mechanicKey)){

                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Block storage mechanic Config! blockstorage_id: " + key + "BlockMechanic_id: " + mechanicKey + " in file: " + file.getName());
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error: mechanic is invalid");
                                continue;

                            }

                            blockMechanics.add(core.getManagers().getBlockStorageManager().getBlockMechanicManager().getMechanic((String)mechanicKey));

                        }

                    }

                    BlockStorageConfig blockStorageConfig = new BlockStorageConfig(blockStorageType,(String)key,blockStorageProperties,blockStorageClickType,block,blockMechanics.toArray(BlockMechanic[]::new),storage);

                    blockStorageConfigs.add(blockStorageConfig);

                }
            }
        }

        AdventureUtils.sendMessagePluginConsole(core, "<aqua> BlockStorages loaded: <yellow>" + blockStorageConfigs.size());

    }


    public ArrayList<BlockStorageConfig> getBlockStorageConfigs() {
        return this.blockStorageConfigs;
    }

    // Método para agregar una nueva configuración de almacenamiento de bloques.
    public void addBlockStorageConfig(BlockStorageConfig config) {
        this.blockStorageConfigs.add(config);
    }

    // Método para eliminar una configuración de almacenamiento de bloques.
    public void removeBlockStorageConfig(BlockStorageConfig config) {
        this.blockStorageConfigs.remove(config);
    }

    // Método para buscar una configuración de almacenamiento de bloques por su ID.
    public BlockStorageConfig findBlockStorageConfigById(String id) {
        for (BlockStorageConfig config : blockStorageConfigs) {
            if (config.getId().equals(id)) {
                return config;
            }
        }
        return null;
    }

    public BlockStorageConfig findBlockStorageConfigByItemID(String id) {
        for (BlockStorageConfig config : blockStorageConfigs) {
            if (config.getBlock().equals(id)) {
                return config;
            }
        }
        return null;
    }

    // Método para comprobar si existe una configuración de almacenamiento de bloques por su ID.
    public boolean blockStorageConfigExists(String id) {
        for (BlockStorageConfig config : blockStorageConfigs) {
            if (config.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

}
