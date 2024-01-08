package dev.wuason.storagemechanic.storages.config;

import java.util.ArrayList;
import java.util.HashMap;

public class StorageConfig {
    private String id;
    private int rows;
    private int pages;
    private StorageProperties storageProperties;
    private StorageInventoryTypeConfig inventoryType;
    private String title;
    private HashMap<String, StageStorage> stagesHashMap = new HashMap<>();
    private ArrayList<StageStorage> stagesOrder = new ArrayList<>();
    private long refreshTimeStages = 0L;
    private ArrayList<StorageSoundConfig> storageSounds = new ArrayList<>();
    private boolean storageSoundEnabled = false;
    private ArrayList<StorageItemConfig> storageItemsDefaultConfig = new ArrayList<>();
    private boolean storageItemsDefaultEnabled = false;
    private ArrayList<StorageItemConfig> storageItemsWhiteListConfig = new ArrayList<>();
    private boolean storageItemsWhiteListEnabled = false;
    private ArrayList<StorageItemConfig> storageItemsBlackListConfig = new ArrayList<>();
    private boolean storageItemsBlackListEnabled = false;
    private HashMap<Integer, HashMap<Integer,StorageItemInterfaceConfig>> storageItemsInterfaceConfig = new HashMap<>();
    private boolean storageItemsInterfaceEnabled = false;

    private ArrayList<StorageBlockItemConfig> storageBlockedItemsConfig = new ArrayList<>();
    private boolean storageBlockItemEnabled = false;
    private String whiteListMessage = null;
    private String blackListMessage = null;
    private int maxViewers = -1;


    public StorageConfig(String id, int rows, int pages, StorageInventoryTypeConfig inventoryType, String title, ArrayList<StorageSoundConfig> storageSounds, boolean storageSoundEnabled, ArrayList<StorageItemConfig> storageItemsDefaultConfig, boolean storageItemsDefaultEnabled, ArrayList<StorageItemConfig> storageItemsWhiteListConfig, boolean storageItemsWhiteListEnabled, ArrayList<StorageItemConfig> storageItemsBlackListConfig, boolean storageItemsBlackListEnabled, HashMap<Integer, HashMap<Integer,StorageItemInterfaceConfig>> storageItemsInterfaceConfig, boolean storageItemsInterfaceEnabled, String blackListMessage, String whiteListMessage, ArrayList<StorageBlockItemConfig> storageBlockedItemsConfig, boolean storageBlockItemEnabled, StorageProperties storageProperties, ArrayList<StageStorage> stagesOrder, long refreshTimeStages, HashMap<String, StageStorage> stagesHashMap, int maxViewers) {
        this.id = id;
        this.rows = rows;
        this.pages = pages;
        this.inventoryType = inventoryType;
        this.title = title;
        this.storageSounds = storageSounds;
        this.storageSoundEnabled = storageSoundEnabled;
        this.storageItemsDefaultConfig = storageItemsDefaultConfig;
        this.storageItemsDefaultEnabled = storageItemsDefaultEnabled;
        this.storageItemsWhiteListConfig = storageItemsWhiteListConfig;
        this.storageItemsWhiteListEnabled = storageItemsWhiteListEnabled;
        this.storageItemsBlackListConfig = storageItemsBlackListConfig;
        this.storageItemsBlackListEnabled = storageItemsBlackListEnabled;
        this.storageItemsInterfaceConfig = storageItemsInterfaceConfig;
        this.storageItemsInterfaceEnabled = storageItemsInterfaceEnabled;
        this.whiteListMessage = whiteListMessage;
        this.blackListMessage = blackListMessage;
        this.storageBlockedItemsConfig = storageBlockedItemsConfig;
        this.storageBlockItemEnabled = storageBlockItemEnabled;
        this.storageProperties = storageProperties;
        this.refreshTimeStages = refreshTimeStages;
        this.stagesHashMap = stagesHashMap;
        this.stagesOrder = stagesOrder;
        this.maxViewers = maxViewers;
    }

    public String getId() {
        return id;
    }

    public int getRows() {
        return rows;
    }

    public int getPages() {
        return pages;
    }

    public StorageInventoryTypeConfig getInventoryType() {
        return inventoryType;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<StorageSoundConfig> getStorageSounds() {
        return storageSounds;
    }

    public boolean isStorageSoundEnabled() {
        return storageSoundEnabled;
    }

    public ArrayList<StorageItemConfig> getStorageItemsDefaultConfig() {
        return storageItemsDefaultConfig;
    }

    public boolean isStorageItemsDefaultEnabled() {
        return storageItemsDefaultEnabled;
    }

    public ArrayList<StorageItemConfig> getStorageItemsWhiteListConfig() {
        return storageItemsWhiteListConfig;
    }

    public boolean isStorageItemsWhiteListEnabled() {
        return storageItemsWhiteListEnabled;
    }

    public ArrayList<StorageItemConfig> getStorageItemsBlackListConfig() {
        return storageItemsBlackListConfig;
    }

    public boolean isStorageItemsBlackListEnabled() {
        return storageItemsBlackListEnabled;
    }

    public HashMap<Integer, HashMap<Integer, StorageItemInterfaceConfig>> getStorageItemsInterfaceConfig() {
        return storageItemsInterfaceConfig;
    }

    public boolean isStorageItemsInterfaceEnabled() {
        return storageItemsInterfaceEnabled;
    }

    public String getWhiteListMessage() {
        return whiteListMessage;
    }

    public String getBlackListMessage() {
        return blackListMessage;
    }

    public ArrayList<StorageBlockItemConfig> getStorageBlockedItemsConfig() {
        return storageBlockedItemsConfig;
    }

    public boolean isStorageBlockItemEnabled() {
        return storageBlockItemEnabled;
    }

    public StorageProperties getStorageProperties() {
        return storageProperties;
    }

    public HashMap<String, StageStorage> getStagesHashMap() {
        return stagesHashMap;
    }

    public ArrayList<StageStorage> getStagesOrder() {
        return stagesOrder;
    }

    public long getRefreshTimeStages() {
        return refreshTimeStages;
    }

    public int getMaxViewers() {
        return maxViewers;
    }
}
