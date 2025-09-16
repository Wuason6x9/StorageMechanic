package dev.wuason.storagemechanic.data.storage;

import dev.wuason.mechanics.data.Data;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.data.DataManager;
import dev.wuason.storagemechanic.data.storage.type.api.StorageApiManagerData;
import dev.wuason.storagemechanic.data.storage.type.block.BlockStorageManagerData;
import dev.wuason.storagemechanic.data.storage.type.furniture.FurnitureStorageManagerData;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageOriginContext;

public class StorageManagerData {

    private DataManager dataManager;
    final public String DATA_TYPE = StorageData.class.getSimpleName();

    private BlockStorageManagerData blockStorageManagerData;
    private FurnitureStorageManagerData furnitureStorageManagerData;
    private StorageApiManagerData storageApiManager;

    public StorageManagerData(DataManager dataManager, StorageMechanic core) {
        this.dataManager = dataManager;
        blockStorageManagerData = new BlockStorageManagerData(dataManager, core);
        furnitureStorageManagerData = new FurnitureStorageManagerData(dataManager, core);
        storageApiManager = new StorageApiManagerData(dataManager, core);
    }


    //basics load,remove,exist,save
    public Storage loadStorageData(String id) {
        if (dataManager.existData(DATA_TYPE, id)) {
            StorageData storageData = (StorageData) dataManager.getData(DATA_TYPE, id).getDataObject();
            return storageDataToStorage(storageData);
        }
        return null;
    }

    public boolean existStorageData(String id) {
        return dataManager.existData(DATA_TYPE, id);
    }

    public void removeStorageData(String id) {
        if (existStorageData(id)) {
            dataManager.removeData(DATA_TYPE, id);
        }
    }

    public void removeStorageData(Storage storage) {
        removeStorageData(storage.getId());
    }

    public void removeStorageData(StorageData storageData) {
        removeStorageData(storageData.getId());
    }

    public void saveStorageData(StorageData storageData) {
        if (storageData == null) return;
        Data data = null;
        String id = storageData.getId();
        if (existStorageData(id)) {
            data = dataManager.getData(DATA_TYPE, id);
        }
        if (data == null) {
            data = new Data(id);
        }
        data.setDataObject(storageData);
        dataManager.saveData(data);
    }

    public void saveStorageData(Storage storage) {
        StorageData storageData = storageToStorageData(storage);
        saveStorageData(storageData);
    }

    public StorageData getStorageData(String id) {
        if (existStorageData(id)) {
            return (StorageData) dataManager.getData(DATA_TYPE, id).getDataObject();
        }
        return null;
    }

    //Serialize
    public Storage storageDataToStorage(StorageData storageData) {
        if (storageData != null) {
            Storage storage = new Storage(storageData.getId(), storageData.getItems(), storageData.getStorageIdConfig(), storageData.getDate(), new StorageOriginContext(StorageOriginContext.Context.valueOf(storageData.getStorageOriginContext()), storageData.getStorageOriginContextData()), storageData.getLastOpenDate());
            return storage;
        }
        return null;
    }

    public StorageData storageToStorageData(Storage storage) {
        if (storage != null) {
            return new StorageData(storage.getItems(), storage.getId(), storage.getStorageIdConfig(), storage.getCreationDate(), storage.getStorageOriginContext().getContext().toString(), storage.getStorageOriginContext().getData(), storage.getLastOpen());
        }
        return null;
    }

    public String getDATA_TYPE() {
        return DATA_TYPE;
    }

    public BlockStorageManagerData getBlockStorageManagerData() {
        return blockStorageManagerData;
    }

    public FurnitureStorageManagerData getFurnitureStorageManagerData() {
        return furnitureStorageManagerData;
    }

    public StorageApiManagerData getStorageApiManager() {
        return storageApiManager;
    }
}
