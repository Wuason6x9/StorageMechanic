package dev.wuason.storagemechanic.storages.config;

import java.util.ArrayList;
import java.util.HashMap;

public class StorageConfig {
    private final String id;
    private final int rows;
    private final int pages;
    private final StorageProperties storageProperties;
    private final StorageInventoryTypeConfig inventoryType;
    private final String title;
    private final HashMap<String, StageStorage> stagesHashMap;
    private final ArrayList<StageStorage> stagesOrder;
    private final long refreshTimeStages;
    private final ArrayList<StorageSoundConfig> storageSounds;
    private final boolean storageSoundEnabled;
    private final ArrayList<StorageItemConfig> storageItemsDefaultConfig;
    private final boolean storageItemsDefaultEnabled;
    private final ArrayList<StorageItemConfig> storageItemsWhiteListConfig;
    private final boolean storageItemsWhiteListEnabled;
    private final ArrayList<StorageItemConfig> storageItemsBlackListConfig;
    private final boolean storageItemsBlackListEnabled;
    private final HashMap<Integer, HashMap<Integer, StorageItemInterfaceConfig>> storageItemsInterfaceConfig;
    private final boolean storageItemsInterfaceEnabled;

    private final ArrayList<StorageBlockItemConfig> storageBlockedItemsConfig;
    private final boolean storageBlockItemEnabled;
    private final String whiteListMessage;
    private final String blackListMessage;
    private final int maxViewers;


    public StorageConfig(String id, int rows, int pages, StorageInventoryTypeConfig inventoryType, String title, ArrayList<StorageSoundConfig> storageSounds, boolean storageSoundEnabled, ArrayList<StorageItemConfig> storageItemsDefaultConfig, boolean storageItemsDefaultEnabled, ArrayList<StorageItemConfig> storageItemsWhiteListConfig, boolean storageItemsWhiteListEnabled, ArrayList<StorageItemConfig> storageItemsBlackListConfig, boolean storageItemsBlackListEnabled, HashMap<Integer, HashMap<Integer, StorageItemInterfaceConfig>> storageItemsInterfaceConfig, boolean storageItemsInterfaceEnabled, String blackListMessage, String whiteListMessage, ArrayList<StorageBlockItemConfig> storageBlockedItemsConfig, boolean storageBlockItemEnabled, StorageProperties storageProperties, ArrayList<StageStorage> stagesOrder, long refreshTimeStages, HashMap<String, StageStorage> stagesHashMap, int maxViewers) {
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
