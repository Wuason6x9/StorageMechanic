package dev.wuason.storagemechanic.storages.types.api;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.data.SaveCause;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageOriginContext;
import org.bukkit.event.Listener;

import java.util.*;

public class StorageApiManager implements Listener {
   private StorageMechanic core;

   private HashMap<String,StorageApi> storageApis = new HashMap<>();

    public StorageApiManager(StorageMechanic core) {
        this.core = core;
    }

    public StorageApi getStorageApi(String id){
        if(storageApis.containsKey(id)){
            return storageApis.get(id);
        }
        StorageApi storageApi = core.getManagers().getDataManager().getStorageManagerData().getStorageApiManager().loadStorageApi(id);
        if(storageApi == null) return null;
        storageApis.put(id,storageApi);
        return storageApi;
    }

    public boolean existStorageApi(String id){
        if(storageApis.containsKey(id)){
            return true;
        }
        return core.getManagers().getDataManager().getStorageManagerData().getStorageApiManager().existStorageApiData(id);
    }

    public StorageApi createStorageApi(String id, StorageApiType type, String storageConfigId){
        StorageOriginContext storageOriginContext = new StorageOriginContext(StorageOriginContext.context.API, new ArrayList<>(){{
            add(id);
            add(storageConfigId);
            add(type.toString());
        }});

        Storage storage = core.getManagers().getStorageManager().createStorage(storageConfigId,storageOriginContext);
        if(storage == null) return null;
        StorageApi storageApi = new StorageApi(type,storage,id);
        storageApis.put(id,storageApi);
        return storageApi;
    }

    public void removeStorageApi(String id){
        core.getManagers().getStorageManager().removeStorage(getStorageApi(id).getStorage().getId());
        if(storageApis.containsKey(id)){
            StorageApi storageApi = storageApis.get(id);
            storageApi.getStorage().closeAllInventory();
            core.getManagers().getStorageManager().removeStorage(storageApi.getStorage().getId());
        }
        else {
            StorageApi storageApi = core.getManagers().getDataManager().getStorageManagerData().getStorageApiManager().loadStorageApi(id);
            core.getManagers().getStorageManager().removeStorage(storageApi.getStorage().getId());
        }
        storageApis.remove(id);
        core.getManagers().getDataManager().getStorageManagerData().getStorageApiManager().removeStorageApiData(id);
    }
    public void saveStorageApi(String id){
        if(!storageApis.containsKey(id)) return;
        StorageApi storageApi = storageApis.get(id);
        core.getManagers().getStorageManager().saveStorage(storageApi.getStorage(), SaveCause.NORMAL_SAVE);
        storageApis.remove(id);
        core.getManagers().getDataManager().getStorageManagerData().getStorageApiManager().saveStorageApi(storageApi);
    }

    public HashMap<String, StorageApi> getStorageApis() {
        return storageApis;
    }

    public void stop(){
        while (!storageApis.isEmpty()){
            System.out.println(storageApis.size());
            String id = storageApis.keySet().iterator().next();
            saveStorageApi(id);
        }
    }

    public Set<String> getAllStoragesId(){
        Set<String> storagesId = new HashSet<>();
        storagesId.addAll(storageApis.keySet());
        storagesId.addAll(Arrays.stream(core.getManagers().getDataManager().getStorageManagerData().getStorageApiManager().getAllStoragesApiDataIds()).toList());
        return storagesId;
    }
}
