package dev.wuason.storagemechanic.storages.config;

import java.util.HashMap;

public class StageStorage {
    private HashMap<Integer, HashMap<Integer, StorageItemInterfaceConfig>> storageItemsInterfaceConfig;
    private String title;
    private String id;

    public StageStorage(HashMap<Integer, HashMap<Integer, StorageItemInterfaceConfig>> storageItemsInterfaceConfig, String title, String id) {
        this.storageItemsInterfaceConfig = storageItemsInterfaceConfig;
        this.title = title;
        this.id = id;
    }

    public HashMap<Integer, HashMap<Integer, StorageItemInterfaceConfig>> getStorageItemsInterfaceConfig() {
        return storageItemsInterfaceConfig;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }
}
