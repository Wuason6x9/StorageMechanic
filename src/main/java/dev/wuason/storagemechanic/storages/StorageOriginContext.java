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
        STORAGE;


        public boolean isBlockStorage() {
            return this == BLOCK_STORAGE;
        }

        public boolean isFurnitureStorage() {
            return this == FURNITURE_STORAGE;
        }

        public boolean isEntityStorage() {
            return this == ENTITY_STORAGE;
        }

        public boolean isItemStorage() {
            return this == ITEM_STORAGE;
        }

        public boolean isApi() {
            return this == API;
        }

        public boolean isStorage() {
            return this == STORAGE;
        }
    }
}
