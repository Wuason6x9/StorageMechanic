package dev.wuason.storagemechanic.storages.types.item.config;

public class ItemStorageConfig {
    private String id;
    private String itemAdapter;
    private ItemStorageClickType itemStorageClickType;
    private String storageConfigID;
    private ItemStoragePropertiesConfig itemStoragePropertiesConfig;

    public ItemStorageConfig(String id, String itemAdapter, ItemStorageClickType itemStorageClickType, String storageConfigID, ItemStoragePropertiesConfig itemStoragePropertiesConfig) {
        this.id = id;
        this.itemAdapter = itemAdapter;
        this.itemStorageClickType = itemStorageClickType;
        this.storageConfigID = storageConfigID;
        this.itemStoragePropertiesConfig = itemStoragePropertiesConfig;
    }

    public String getId() {
        return id;
    }

    public String getItemAdapter() {
        return itemAdapter;
    }

    public ItemStorageClickType getItemStorageClickType() {
        return itemStorageClickType;
    }

    public String getStorageConfigID() {
        return storageConfigID;
    }

    public ItemStoragePropertiesConfig getItemStoragePropertiesConfig() {
        return itemStoragePropertiesConfig;
    }
}
