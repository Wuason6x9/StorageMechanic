package dev.wuason.storagemechanic.data.storage.type.api;

import dev.wuason.mechanics.data.Data;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.data.DataManager;
import dev.wuason.storagemechanic.data.SaveCause;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.types.api.StorageApi;

public class StorageApiManagerData {
    private DataManager dataManager;
    private StorageMechanic core;
    final public String DATA_TYPE = StorageApiData.class.getSimpleName();

    public StorageApiManagerData(DataManager dataManager, StorageMechanic core) {
        this.core = core;
        this.dataManager = dataManager;
    }

    public Data[] getAllStoragesApiData() {
        return dataManager.getAllData(DATA_TYPE);
    }

    public String[] getAllStoragesApiDataIds() {
        return dataManager.getAllDataIds(DATA_TYPE);
    }

    public void saveStorageApi(StorageApi storageApi) {
        if (storageApi == null) return;
        core.getManagers().getStorageManager().saveStorage(storageApi.getStorage(), SaveCause.STOPPING_SAVE);
        saveStorageApiData(StorageApiToStorageApiData(storageApi));
    }

    public void saveStorageApiData(StorageApiData storageApiData) {
        if (storageApiData == null) return;
        Data data = null;
        if (existStorageApiData(storageApiData.getId())) {
            data = dataManager.getData(DATA_TYPE, storageApiData.getId());
        }
        if (data == null) {
            data = new Data(storageApiData.getId());
        }
        data.setDataObject(storageApiData);
        dataManager.saveData(data);
    }

    public void removeStorageApiData(String id) {
        if (existStorageApiData(id)) {
            StorageApiData storageApiData = getStorageApiData(id);
            if (storageApiData == null) return;
            core.getManagers().getStorageManager().removeStorage(id);
            dataManager.removeData(DATA_TYPE, id);
        }
    }

    public StorageApi loadStorageApi(String id) {
        StorageApiData storageApiData = getStorageApiData(id);
        if (storageApiData == null) return null;
        return StorageApiDataToStorageApi(storageApiData);
    }

    public StorageApiData getStorageApiData(String id) {
        if (existStorageApiData(id)) {
            return (StorageApiData) dataManager.getData(DATA_TYPE, id).getDataObject();
        }
        return null;
    }

    public boolean existStorageApiData(String id) {
        return dataManager.existData(DATA_TYPE, id);
    }

    public StorageApi StorageApiDataToStorageApi(StorageApiData storageApiData) {
        if (storageApiData == null) return null;
        Storage storage = core.getManagers().getStorageManager().getStorage(storageApiData.getStorageId());
        if (storage == null) return null;
        return new StorageApi(storageApiData.getType(), storage, storageApiData.getId());
    }

    public StorageApiData StorageApiToStorageApiData(StorageApi storageApi) {
        if (storageApi == null) return null;
        return new StorageApiData(storageApi.getId(), storageApi.getStorage().getId(), storageApi.getType());
    }

}
