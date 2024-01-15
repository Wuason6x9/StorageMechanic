package dev.wuason.storagemechanic.storages;

import java.io.Serializable;
import java.util.List;

public class StorageOriginContext implements Serializable {
    private Context context;
    private List<String> data;

    public StorageOriginContext(Context context, List<String> data) {
        this.context = context;
        this.data = data;
    }

    public Context getContext() {
        return context;
    }

    public List<String> getData() {
        return data;
    }

    public enum Context implements Serializable {
        BLOCK_STORAGE,
        FURNITURE_STORAGE,
        ENTITY_STORAGE,
        ITEM_STORAGE,
        API,
        STORAGE
    }
}
