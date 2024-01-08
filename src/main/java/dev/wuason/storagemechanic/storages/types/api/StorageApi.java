package dev.wuason.storagemechanic.storages.types.api;

import dev.wuason.storagemechanic.storages.Storage;

public class StorageApi {
    private StorageApiType type;
    private Storage storage;
    private String id;

    public StorageApi(StorageApiType type, Storage storage, String id) {
        this.type = type;
        this.storage = storage;
        this.id = id;
    }

    public StorageApiType getType() {
        return type;
    }

    public Storage getStorage() {
        return storage;
    }

    public String getId() {
        return id;
    }
}
