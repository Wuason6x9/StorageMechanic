package dev.wuason.storagemechanic.data.storage;

import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class StorageData implements Serializable {
    private static final long serialVersionUID = 1101L;
    private Map<Integer, ItemStack[]> items;
    private String id;
    private String storageIdConfig;
    private Date date;
    private String storageOriginContext;
    private String[] storageOriginContextData;

    public StorageData(Map<Integer, ItemStack[]> items, String id, String storageIdConfig, Date date, String storageOriginContext, List<String> storageOriginContextData) {
        this.items = items;
        this.id = id;
        this.storageIdConfig = storageIdConfig;
        this.date = date;
        this.storageOriginContext = storageOriginContext;
        this.storageOriginContextData = storageOriginContextData.toArray(new String[0]);
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

    public String getStorageOriginContext() {
        return storageOriginContext;
    }
    public List<String> getStorageOriginContextData() {
        return List.of(storageOriginContextData);
    }
}
