package dev.wuason.storagemechanic.storages;

import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class StorageSerializable implements Serializable {
    private Map<Integer, ItemStack[]> items;

    String id;
    String storageIdConfig;

    public StorageSerializable(Map<Integer, ItemStack[]> items, String id, String storageIdConfig) {
        this.items = items;
        this.id = id;
        this.storageIdConfig = storageIdConfig;
    }

    public Map<Integer, ItemStack[]> getItems() {
        return items;
    }

    public String getId() {
        return id;
    }

    public String getStorageIdConfig() {
        return storageIdConfig;
    }
}
