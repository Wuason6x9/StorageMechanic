package dev.wuason.storagemechanic.data.storage.type.api;

import dev.wuason.storagemechanic.storages.types.api.StorageApiType;

import java.io.Serializable;

public class StorageApiData implements Serializable {
    private static final long serialVersionUID = 1102L;
    private String id;
    private String storageId;
    private StorageApiType type;

    public StorageApiData(String id, String storageId, StorageApiType type) {
        this.id = id;
        this.storageId = storageId;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getStorageId() {
        return storageId;
    }

    public StorageApiType getType() {
        return type;
    }
}
