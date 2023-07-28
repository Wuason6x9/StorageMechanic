package dev.wuason.storagemechanic.storages.config;

import java.util.ArrayList;

public class StorageConfig {
    private String id;
    private int rows;
    private int pages;
    private StorageInventoryTypeConfig inventoryType;
    private String title;
    private ArrayList<StorageSoundConfig> storageSounds = new ArrayList<>();
    private boolean storageSoundEnabled = false;
    private ArrayList<StorageItemConfig> storageItemsDefaultConfig = new ArrayList<>();
    private boolean storageItemsDefaultEnabled = false;
    private ArrayList<StorageItemConfig> storageItemsWhiteListConfig = new ArrayList<>();
    private boolean storageItemsWhiteListEnabled = false;
    private ArrayList<StorageItemConfig> storageItemsBlackListConfig = new ArrayList<>();
    private boolean storageItemsBlackListEnabled = false;
    private ArrayList<StorageItemInterfaceConfig> storageItemsInterfaceConfig = new ArrayList<>();
    private boolean storageItemsInterfaceEnabled = false;

    private ArrayList<StorageBlockItemConfig> storageBlockedItemsConfig = new ArrayList<>();
    private boolean storageBlockItemEnabled = false;
    private String whiteListMessage = null;
    private String blackListMessage = null;

    public StorageConfig(String id, int rows, int pages, StorageInventoryTypeConfig inventoryType, String title, ArrayList<StorageSoundConfig> storageSounds, boolean storageSoundEnabled, ArrayList<StorageItemConfig> storageItemsDefaultConfig, boolean storageItemsDefaultEnabled, ArrayList<StorageItemConfig> storageItemsWhiteListConfig, boolean storageItemsWhiteListEnabled, ArrayList<StorageItemConfig> storageItemsBlackListConfig, boolean storageItemsBlackListEnabled, ArrayList<StorageItemInterfaceConfig> storageItemsInterfaceConfig, boolean storageItemsInterfaceEnabled, String blackListMessage, String whiteListMessage, ArrayList<StorageBlockItemConfig> storageBlockedItemsConfig, boolean storageBlockItemEnabled) {
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

    public ArrayList<StorageItemInterfaceConfig> getStorageItemsInterfaceConfig() {
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

}
