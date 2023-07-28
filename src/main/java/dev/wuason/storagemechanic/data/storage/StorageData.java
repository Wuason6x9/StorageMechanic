package dev.wuason.storagemechanic.data.storage;

import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class StorageData implements Serializable {
    private Map<Integer, ItemStack[]> items;

    private String id;
    private String storageIdConfig;
    private Date date;

    public StorageData(Map<Integer, ItemStack[]> items, String id, String storageIdConfig, Date date) {
        this.items = items;
        this.id = id;
        this.storageIdConfig = storageIdConfig;
        this.date = date;
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

    public Date getDate() {
        return date;
    }
}
