package dev.wuason.storagemechanic.storages.types.item.config;

import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.compatibilities.adapter.Adapter;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.storages.types.block.config.BlockStorageClickType;
import dev.wuason.storagemechanic.storages.types.block.config.BlockStorageConfig;
import dev.wuason.storagemechanic.storages.types.block.config.BlockStorageProperties;
import dev.wuason.storagemechanic.storages.types.block.config.BlockStorageType;
import dev.wuason.storagemechanic.storages.types.block.mechanics.BlockMechanic;
import dev.wuason.storagemechanic.utils.StorageUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ItemStorageConfigManager {
    private HashMap<String,ItemStorageConfig> itemStorageConfigs = new HashMap<>();
    private StorageMechanic core;

    public ItemStorageConfigManager(StorageMechanic core) {
        this.core = core;
    }

    public void loadItemStorageConfigs(){

        itemStorageConfigs = new HashMap<>();

        File base = new File(core.getDataFolder() + "/types/");
        base.mkdirs();

        File[] files = Arrays.stream(base.listFiles()).filter(f -> {

            if(f.getName().contains(".yml")) return true;

            return false;

        }).toArray(File[]::new);

        for(File file : files){

            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

            ConfigurationSection sectionItemStorages = config.getConfigurationSection("storage.item");

            if(sectionItemStorages != null){
                for(Object key : sectionItemStorages.getKeys(false).toArray()){

                    if(!StorageUtils.isValidConfigId((String)key)){
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Item Storage Config! itemstorage_id: " + key + " in file: " + file.getName());
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error: The Itemstorage id cannot contain \"_\" in the id!");
                        continue;
                    }

                    ConfigurationSection sectionItemStorage = sectionItemStorages.getConfigurationSection((String)key);


                    ItemStorageClickType itemStorageClickType;
                    try {
                        itemStorageClickType = ItemStorageClickType.valueOf(sectionItemStorage.getString("open"));
                    }catch (IllegalArgumentException a){
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Item storage Config! itemstorage_id: " + key + " in file: " + file.getName());
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error: Open is null or invalid");
                        continue;
                    }

                    //ITEM ID
                    String itemId = sectionItemStorage.getString("item",".");
                    if(itemId.equals(".") || !Adapter.getInstance().existAdapterID(itemId)){
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Item storage Config! itemstorage_id: " + key + " in file: " + file.getName());
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error: Item is null or invalid");
                        continue;
                    }

                    //STORAGE ID
                    String storage = sectionItemStorage.getString("storage_id",".");
                    if(storage.equals(".") || !core.getManagers().getStorageConfigManager().existsStorageConfig(storage)){
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Item storage Config! itemstorage_id: " + key + " in file: " + file.getName());
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error: Storage_ID is null or invalid");
                        continue;
                    }

                    //PROPERTIES
                    boolean storageable = sectionItemStorage.getBoolean("properties.isStorageable",false);
                    boolean damageable = sectionItemStorage.getBoolean("properties.isDamageable",false);
                    boolean dropAllItemsOnDeath = sectionItemStorage.getBoolean("properties.dropAllItemsOnDeath",true);
                    ItemStoragePropertiesConfig itemStoragePropertiesConfig = new ItemStoragePropertiesConfig(storageable,damageable,dropAllItemsOnDeath);

                    ItemStorageConfig itemStorageConfig = new ItemStorageConfig((String)key,itemId,itemStorageClickType,storage,itemStoragePropertiesConfig);
                    itemStorageConfigs.put((String)key,itemStorageConfig);

                }
            }
        }

        AdventureUtils.sendMessagePluginConsole(core, "<aqua> ItemStorages loaded: <yellow>" + itemStorageConfigs.size());

    }

    public ItemStorageConfig findItemStorageConfigByItemID(String id) {
        for (ItemStorageConfig config : itemStorageConfigs.values()) {
            if (config.getItemAdapter().equals(id)) {
                return config;
            }
        }
        return null;
    }
    public ItemStorageConfig getItemStorageConfig(String id){
        return itemStorageConfigs.getOrDefault(id,null);
    }
    public boolean existItemStorageConfig(String id){
        return itemStorageConfigs.containsKey(id);
    }

    public HashMap<String, ItemStorageConfig> getItemStorageConfigs() {
        return itemStorageConfigs;
    }
}
