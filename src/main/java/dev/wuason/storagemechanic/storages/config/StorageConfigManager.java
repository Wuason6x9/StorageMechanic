package dev.wuason.storagemechanic.storages.config;

import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.compatibilities.AdapterManager;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.mechanics.utils.Utils;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.utils.StorageUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class StorageConfigManager {

    private StorageMechanic core;

    private HashMap<String,StorageConfig> storagesConfig = new HashMap<>();

    public StorageConfigManager(StorageMechanic core) {
        this.core = core;
    }



    public void loadStoragesConfig(){

        storagesConfig = new HashMap<>();

        File base = new File(Mechanics.getInstance().getManager().getMechanicsManager().getMechanic(core).getDirConfig().getPath() + "/storages/");
        base.mkdirs();

        File[] files = Arrays.stream(base.listFiles()).filter(f -> {

            if(f.getName().contains(".yml")) return true;

            return false;

        }).toArray(File[]::new);

        for(File file : files){

            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

            ConfigurationSection sectionStorages = config.getConfigurationSection("storages");

            if(sectionStorages != null){
                for(Object key : sectionStorages.getKeys(false).toArray()){

                    if(!StorageUtils.isValidConfigId((String)key)){
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Storage Config! storage_id: " + key + " in file: " + file.getName());
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error: The storage id cannot contain \"_\" in the id!");
                        continue;
                    }

                    ConfigurationSection sectionStorage = sectionStorages.getConfigurationSection((String)key);

                    //STORAGE SECTION
                    int rows = sectionStorage.getInt("storage.rows",0); //VAR
                    int pages = sectionStorage.getInt("storage.pages",1); //VAR
                    String title = sectionStorage.getString("storage.title","untitled"); //VAR

                    StorageInventoryTypeConfig storageInventoryType = null; //VAR
                    try {
                        storageInventoryType = StorageInventoryTypeConfig.valueOf(sectionStorage.getString("storage.inventory_type",".").toUpperCase());
                    }
                    catch (IllegalArgumentException a){
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Storage Config! storage_id: " + key + " in file: " + file.getName());
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error: InventoryType is null or invalid");
                        continue;
                    }

                    if(storageInventoryType.equals(StorageInventoryTypeConfig.CHEST)){if(rows == 0 || rows > 6){AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Storage Config! storage_id: " + key + " in file: " + file.getName());AdventureUtils.sendMessagePluginConsole(core, "<red>Error: The rows are empty or the number is greater than 6");continue;}}

                    //Storage properties

                    boolean tempStorage = sectionStorage.getBoolean("storage.properties.isTempStorage",false);
                    boolean dropItemsPageOnClose = sectionStorage.getBoolean("storage.properties.dropItemsPageOnClose",false);;

                    StorageProperties storageProperties = new StorageProperties(tempStorage, dropItemsPageOnClose);

                    //SOUND SECTION

                    boolean soundsEnabled = sectionStorage.getBoolean("sounds.enabled",false); //VAR

                    ArrayList<StorageSoundConfig> storageSoundConfigs = new ArrayList<>(); //VAR
                    ConfigurationSection sectionSounds = sectionStorage.getConfigurationSection("sounds.list");
                    if(sectionSounds != null){
                        for(Object soundKey : sectionSounds.getKeys(false).toArray()){
                            ConfigurationSection sectionSound = sectionSounds.getConfigurationSection((String)soundKey);
                            StorageSoundConfig.Type soundType = null;
                            try {soundType = StorageSoundConfig.Type.valueOf(sectionSound.getString("type",".").toUpperCase());}
                            catch (IllegalArgumentException a){AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Storage sound Config! storage_id: " + key + " sound_id: " + soundKey + " in file: " + file.getName());AdventureUtils.sendMessagePluginConsole(core, "<red>Error: soundType is null or invalid");continue;}
                            ArrayList<Integer> soundPages = StorageUtils.configFill(sectionSound.getStringList("pages"));
                            ArrayList<Integer> soundSlots = StorageUtils.configFill(sectionSound.getStringList("slots"));
                            int volume = sectionSound.getInt("volume",100);
                            Double pitch = sectionSound.getDouble("pitch",1D);
                            String sound = sectionSound.getString("sound",".");
                            if(sound.equals(".")){AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Storage sound Config! storage_id: " + key + " sound_id: " + soundKey + " in file: " + file.getName());AdventureUtils.sendMessagePluginConsole(core, "<red>Error: sound is null or invalid");continue;}
                            StorageSoundConfig storageSoundConfig = new StorageSoundConfig((String)soundKey,sound,soundPages,soundType,soundSlots,pitch,volume);
                            storageSoundConfigs.add(storageSoundConfig);
                        }
                    }

                    boolean defaultItemsEnabled = sectionStorage.getBoolean("items.default.enabled", false); //VAR
                    ArrayList<StorageItemConfig> storageDefaultItemsConfigs = new ArrayList<>(); //VAR
                    ConfigurationSection sectionDefaultItemsConfigs = sectionStorage.getConfigurationSection("items.default.list");
                    processItems(sectionDefaultItemsConfigs, storageDefaultItemsConfigs, "default",key,file);

                    boolean whiteListItemsEnabled = sectionStorage.getBoolean("items.whitelist.enabled", false); //VAR
                    String whiteListMessage = sectionStorage.getString("items.whitelist.message");
                    ArrayList<StorageItemConfig> storageWhiteListItemsConfigs = new ArrayList<>(); //VAR
                    ConfigurationSection sectionWhiteListItemsConfigs = sectionStorage.getConfigurationSection("items.whitelist.list");
                    processItems(sectionWhiteListItemsConfigs, storageWhiteListItemsConfigs, "whiteList",key,file);


                    boolean blackListItemsEnabled = sectionStorage.getBoolean("items.blacklist.enabled", false); //VAR
                    String blackListMessage = sectionStorage.getString("items.blacklist.message");
                    ArrayList<StorageItemConfig> storageBlackListItemsConfigs = new ArrayList<>(); //VAR
                    ConfigurationSection sectionBlackListItemsConfigs = sectionStorage.getConfigurationSection("items.blacklist.list");
                    processItems(sectionBlackListItemsConfigs, storageBlackListItemsConfigs, "blackList",key,file);

                    //items interfaces

                    boolean interfacesEnabled = sectionStorage.getBoolean("interfaces.enabled",false); //VAR

                    HashMap<Integer, HashMap<Integer,StorageItemInterfaceConfig>> storageInterfacesConfigs = new HashMap<>(); //VAR
                    ConfigurationSection sectionInterfaces = sectionStorage.getConfigurationSection("interfaces.list");
                    if(sectionInterfaces != null){
                        for(Object interfaceItemKey : sectionInterfaces.getKeys(false).toArray()){
                            ConfigurationSection sectionInterface = sectionInterfaces.getConfigurationSection((String)interfaceItemKey);
                            ArrayList<Integer> interfaceItemPages = StorageUtils.configFill(sectionInterface.getStringList("pages"));
                            ArrayList<Integer> interfaceItemSlots = StorageUtils.configFill(sectionInterface.getStringList("slots"));
                            if(!checkSlots(interfaceItemSlots)){AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Storage " + "interfaceItem" + " item Config! storage_id: " + key + " " + "interfaceItem" + "Item_id: " + interfaceItemKey + " in file: " + file.getName());AdventureUtils.sendMessagePluginConsole(core, "<red>Error: " + "interfaceItem" + " Some slot is wrong the range of slots is: 0 - 63");continue;}
                            String interfaceItem = sectionInterface.getString("item",".");
                            if(interfaceItem.equals(".")){AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Storage interfaceItem Config! storage_id: " + key + " interfaceItem_id: " + interfaceItemKey + " in file: " + file.getName());AdventureUtils.sendMessagePluginConsole(core, "<red>Error: interfaceItem is null or invalid");continue;}
                            if(!core.getManagers().getItemInterfaceManager().existsItemInterface(interfaceItem)){AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Storage interfaceItem Config! storage_id: " + key + " interfaceItem_id: " + interfaceItemKey + " in file: " + file.getName());AdventureUtils.sendMessagePluginConsole(core, "<red>Error: interfaceItem is null or invalid");continue;}
                            StorageItemInterfaceConfig storageInterfacesConfig = new StorageItemInterfaceConfig((String)interfaceItemKey,interfaceItem);
                            for(int page : interfaceItemPages){
                                for(int slot : interfaceItemSlots){
                                    if(!storageInterfacesConfigs.containsKey(page)) storageInterfacesConfigs.put(page, new HashMap<>());
                                    HashMap<Integer, StorageItemInterfaceConfig> map = storageInterfacesConfigs.get(page);
                                    map.put(slot,storageInterfacesConfig);
                                }
                            }
                        }
                    }

                    //stages

                    HashMap<String, StageStorage> stagesHashMap = new HashMap<>();
                    ArrayList<StageStorage> stagesOrder = new ArrayList<>();
                    long refreshTimeStages = sectionStorage.getLong("stages.refresh",0);
                    ConfigurationSection sectionStages = sectionStorage.getConfigurationSection("stages.list");
                    if(sectionStages != null){
                        for(String stageKey : sectionStages.getKeys(false)){
                            if(stagesHashMap.containsKey(stageKey)){
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Stage stage_id: " + stageKey + " Storage Config! storage_id: " + key + " in file: " + file.getName());
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error: Stage is duplicate!");
                                continue;
                            }
                            ConfigurationSection sectionStage = sectionStages.getConfigurationSection(stageKey);
                            String titleStage = sectionStage.getString("title");
                            HashMap<Integer, HashMap<Integer,StorageItemInterfaceConfig>> storageInterfacesStageConfigs = new HashMap<>(); //VAR
                            ConfigurationSection sectionStageInterfaces = sectionStage.getConfigurationSection("interfaces.list");
                            if(sectionStageInterfaces != null){
                                for(String interfaceStageItemKey : sectionStageInterfaces.getKeys(false)){
                                    ConfigurationSection sectionInterface = sectionStageInterfaces.getConfigurationSection(interfaceStageItemKey);
                                    ArrayList<Integer> interfaceItemPages = StorageUtils.configFill(sectionInterface.getStringList("pages"));
                                    ArrayList<Integer> interfaceItemSlots = StorageUtils.configFill(sectionInterface.getStringList("slots"));
                                    if(!checkSlots(interfaceItemSlots)){AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Storage " + "interfaceItem" + " item Config! in Stage_id: " + stageKey + " storage_id: " + key + " " + "interfaceItem" + "Item_id: " + interfaceStageItemKey + " in file: " + file.getName());AdventureUtils.sendMessagePluginConsole(core, "<red>Error: " + "interfaceItem" + " Some slot is wrong the range of slots is: 0 - 63");continue;}
                                    String interfaceItem = sectionInterface.getString("item",".");
                                    if(interfaceItem.equals(".")){AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Storage interfaceItem Config! in Stage_id: " + stageKey +  " storage_id: " + key + " interfaceItem_id: " + interfaceStageItemKey + " in file: " + file.getName());AdventureUtils.sendMessagePluginConsole(core, "<red>Error: interfaceItem is null or invalid");continue;}
                                    if(!core.getManagers().getItemInterfaceManager().existsItemInterface(interfaceItem)){AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Storage interfaceItem Config!  in Stage_id: " + stageKey + " storage_id: " + key + " interfaceItem_id: " + interfaceStageItemKey + " in file: " + file.getName());AdventureUtils.sendMessagePluginConsole(core, "<red>Error: interfaceItem is null or invalid");continue;}
                                    StorageItemInterfaceConfig storageInterfacesConfig = new StorageItemInterfaceConfig(interfaceStageItemKey,interfaceItem);
                                    for(int page : interfaceItemPages){
                                        for(int slot : interfaceItemSlots){
                                            if(!storageInterfacesStageConfigs.containsKey(page)) storageInterfacesStageConfigs.put(page, new HashMap<>());
                                            HashMap<Integer, StorageItemInterfaceConfig> map = storageInterfacesStageConfigs.get(page);
                                            map.put(slot,storageInterfacesConfig);
                                        }
                                    }
                                }
                            }
                            StageStorage stageStorage = new StageStorage(storageInterfacesStageConfigs,titleStage,stageKey);
                            stagesHashMap.put(stageKey,stageStorage);
                            stagesOrder.add(stageStorage);
                        }
                    }

                    boolean storageBlockedEnabled = sectionStorage.getBoolean("items.block.enabled",false);

                    ArrayList<StorageBlockItemConfig> storageBlockItemConfigs = new ArrayList<>();
                    ConfigurationSection sectionBlockedItems = sectionStorage.getConfigurationSection("items.block.list");
                    if(sectionBlockedItems != null){
                        for(Object BlockedItemKey : sectionBlockedItems.getKeys(false).toArray()){
                            ConfigurationSection sectionBlocked = sectionBlockedItems.getConfigurationSection((String)BlockedItemKey);
                            ArrayList<Integer> blockedSlots = StorageUtils.configFill(sectionBlocked.getStringList("slots"));
                            ArrayList<Integer> blockedPages = StorageUtils.configFill(sectionBlocked.getStringList("pages"));
                            String message = sectionBlocked.getString("message");
                            if(blockedSlots.isEmpty()){
                                blockedSlots.addAll(StorageUtils.configFill(Collections.singletonList("0-53")));
                            }
                            StorageBlockItemConfig storageBlockItemConfig = new StorageBlockItemConfig((String)BlockedItemKey,blockedSlots,blockedPages,message);
                            storageBlockItemConfigs.add(storageBlockItemConfig);
                        }
                    }

                    StorageConfig storageConfig = new StorageConfig((String)key,rows,pages,storageInventoryType,title,storageSoundConfigs,soundsEnabled,storageDefaultItemsConfigs,defaultItemsEnabled,storageWhiteListItemsConfigs,whiteListItemsEnabled,storageBlackListItemsConfigs,blackListItemsEnabled,storageInterfacesConfigs,interfacesEnabled,blackListMessage,whiteListMessage,storageBlockItemConfigs,storageBlockedEnabled,storageProperties,stagesOrder,refreshTimeStages,stagesHashMap);
                    storagesConfig.put(storageConfig.getId(),storageConfig);
                }
            }
        }


        AdventureUtils.sendMessagePluginConsole(core, "<aqua> Storages loaded: <yellow>" + storagesConfig.size());

    }

    public boolean checkSlots(ArrayList<Integer> slots){
        for(int s : slots){
            if(s>63) return false;
        }
        return true;
    }


    public void processItems(ConfigurationSection sectionItemsConfigs, ArrayList<StorageItemConfig> storageItemsConfigs, String itemType,Object key,File file) {
        if (sectionItemsConfigs != null) {
            for (Object itemsKey : sectionItemsConfigs.getKeys(false).toArray()) {
                ConfigurationSection sectionItemsConfig = sectionItemsConfigs.getConfigurationSection((String) itemsKey);
                ArrayList<Integer> itemsPages = StorageUtils.configFill(sectionItemsConfig.getStringList("pages"));
                ArrayList<Integer> itemsSlots = StorageUtils.configFill(sectionItemsConfig.getStringList("slots"));
                if(itemsSlots.isEmpty()){
                    itemsSlots.addAll(StorageUtils.configFill(Collections.singletonList("0-53")));
                }

                List<String> items = sectionItemsConfig.getStringList("items");

                float chance = Float.parseFloat(sectionItemsConfig.getString("chance","100.0"));
                if(itemType.contains("default")){
                    if(chance < 0.0f || chance > 100.0f){
                        chance = 100.0f;
                    }
                }
                String amount = sectionItemsConfig.getString("amount", "1");
                if(!checkSlots(itemsSlots)){
                    AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Storage " + itemType + " item Config! storage_id: " + key + " " + itemType + "Item_id: " + itemsKey + " in file: " + file.getName());
                    AdventureUtils.sendMessagePluginConsole(core, "<red>Error: " + itemType + " Some slot is wrong the range of slots is: 0 - 63");
                    continue;
                }
                if(itemType.contains("default")){
                    if(amount.contains("-")){
                        if(Utils.isNumber((amount.split("-"))[0])){
                            if((Integer.parseInt((amount.split("-"))[0])) > 64 || (Integer.parseInt((amount.split("-"))[0])) == 0){
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Storage " + itemType + " item Config! storage_id: " + key + " " + itemType + "Item_id: " + itemsKey + " in file: " + file.getName());
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error: " + itemType + " Item amount 1 - 64 your item amount is: " + amount);
                                continue;
                            }
                        }
                        else {
                            AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Storage " + itemType + " item Config! storage_id: " + key + " " + itemType + "Item_id: " + itemsKey + " in file: " + file.getName());
                            AdventureUtils.sendMessagePluginConsole(core, "<red>Error: " + itemType + " Item amount 1 - 64 your item amount is: " + amount);
                            continue;
                        }
                    }
                    else {
                        if(Utils.isNumber(amount)){
                            if((Integer.parseInt(amount)) > 64 || (Integer.parseInt(amount)) == 0){
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Storage " + itemType + " item Config! storage_id: " + key + " " + itemType + "Item_id: " + itemsKey + " in file: " + file.getName());
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error: " + itemType + " Item amount 1 - 64 your item amount is: " + amount);
                                continue;
                            }
                        }
                        else {
                            AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Storage " + itemType + " item Config! storage_id: " + key + " " + itemType + "Item_id: " + itemsKey + " in file: " + file.getName());
                            AdventureUtils.sendMessagePluginConsole(core, "<red>Error: " + itemType + " Item amount 1 - 64 your item amount is: " + amount);
                            continue;
                        }
                    }
                    //2
                    if(amount.contains("-") && amount.split("-").length > 1 && Utils.isNumber((amount.split("-"))[1])){
                        if((Integer.parseInt((amount.split("-"))[1])) > 64 || (Integer.parseInt((amount.split("-"))[1])) == 0){
                            AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Storage " + itemType + " item Config! storage_id: " + key + " " + itemType + "Item_id: " + itemsKey + " in file: " + file.getName());
                            AdventureUtils.sendMessagePluginConsole(core, "<red>Error: " + itemType + " Item amount 1 - 64 your item amount is: " + amount);
                            continue;
                        }
                    }
                    else {
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Storage " + itemType + " item Config! storage_id: " + key + " " + itemType + "Item_id: " + itemsKey + " in file: " + file.getName());
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error: " + itemType + " Item amount 1 - 64 your item amount is: " + amount);
                        continue;
                    }
                }
                AdapterManager adapterManager = Mechanics.getInstance().getManager().getAdapterManager();
                if (!adapterManager.isItemsValid(items)) {
                    AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Storage " + itemType + " item Config! storage_id: " + key + " " + itemType + "Item_id: " + itemsKey + " in file: " + file.getName());
                    AdventureUtils.sendMessagePluginConsole(core, "<red>Error: " + itemType + "Item is null or invalid");
                    continue;
                }
                List<String> itemsComputed = new ArrayList<>();
                for(String i : items){
                    itemsComputed.add(adapterManager.getAdapterID(adapterManager.getItemStack(i)));
                }
                StorageItemConfig storageItemConfig = new StorageItemConfig((String) itemsKey, amount, itemsSlots, itemsPages, itemsComputed,chance);
                storageItemsConfigs.add(storageItemConfig);
            }
        }
    }

    public Collection<StorageConfig> getStoragesConfig() {
        return storagesConfig.values();
    }

    // Obtener un StorageConfig específico por su ID
    public StorageConfig getStorageConfigById(String id) {
        return storagesConfig.getOrDefault(id,null);
    }

    // Verificar si existe un StorageConfig con la ID dada
    public boolean existsStorageConfig(String id) {
        return storagesConfig.containsKey(id);
    }

}
