package dev.wuason.storagemechanic.storages;

import java.io.Serializable;
import java.util.List;

public class StorageOriginContext implements Serializable {
    private context context;
    private List<String> data;

    public StorageOriginContext(StorageOriginContext.context context, List<String> data) {
        this.context = context;
        this.data = data;
    }

    public StorageOriginContext.context getContext() {
        return context;
    }

    public List<String> getData() {
        return data;
    }

    public enum context implements Serializable{
        BLOCK_STORAGE,
        FURNITURE_STORAGE,
        ENTITY_STORAGE,
        ITEM_STORAGE,
        API,
        STORAGE
    }
}
