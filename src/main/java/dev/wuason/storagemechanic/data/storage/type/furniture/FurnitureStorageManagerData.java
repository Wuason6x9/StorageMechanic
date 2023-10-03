package dev.wuason.storagemechanic.data.storage.type.furniture;

import dev.wuason.mechanics.data.Data;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.data.DataManager;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageManager;
import dev.wuason.storagemechanic.storages.types.furnitures.FurnitureStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class FurnitureStorageManagerData {
    private DataManager dataManager;
    private StorageMechanic core;
    final public String DATA_TYPE = FurnitureStorageData.class.getSimpleName();
    public FurnitureStorageManagerData(DataManager dataManager, StorageMechanic core){
        this.core = core;
        this.dataManager = dataManager;
    }

    //basics
    public void saveFurnitureStorage(FurnitureStorage furnitureStorage){
        if(furnitureStorage == null) return;
        saveFurnitureStorageData(furnitureStorageToFurnitureStorageData(furnitureStorage));
    }
    public void saveFurnitureStorageData(FurnitureStorageData furnitureStorageData){
        if(furnitureStorageData == null) return;
        Data data = null;
        if(existFurnitureStorageData(furnitureStorageData.getFurnitureStorageID())){
            data = dataManager.getData(DATA_TYPE, furnitureStorageData.getFurnitureStorageID());
        }
        if(data == null){
            data = new Data(furnitureStorageData.getFurnitureStorageID());
        }
        data.setDataObject(furnitureStorageData);
        dataManager.saveData(data);
    }
    public void removeFurnitureStorageData(String id){
        if(existFurnitureStorageData(id)){
            dataManager.removeData(DATA_TYPE,id);
        }
    }
    public FurnitureStorage loadFurnitureStorageData(String id){
        FurnitureStorageData furnitureStorageData = getFurnitureStorageData(id);
        if(furnitureStorageData == null) return null;
        return furnitureStorageDataToFurnitureStorage(furnitureStorageData);
    }
    public FurnitureStorageData getFurnitureStorageData(String id){
        if(existFurnitureStorageData(id)){
            return (FurnitureStorageData) dataManager.getData(DATA_TYPE,id).getDataObject();
        }
        return null;
    }
    public boolean existFurnitureStorageData(String id){
        return dataManager.existData(DATA_TYPE,id);
    }

    //serialize
    public FurnitureStorageData furnitureStorageToFurnitureStorageData(FurnitureStorage furnitureStorage){
        if(furnitureStorage == null) return null;
        UUID ownerUUID = furnitureStorage.getOwnerUUID();
        HashMap<String,String> hashMap = new HashMap<>();
        String furnitureStorageID = furnitureStorage.getId();
        String furnitureStorageConfigID = furnitureStorage.getFurnitureStorageConfigID();
        furnitureStorage.getStorages().forEach((s, storage) -> hashMap.put(s,storage.getId()));

        String[] locs = new String[furnitureStorage.getLocs().size()];

        for(int i=0;i<furnitureStorage.getLocs().size();i++){

            Location location = furnitureStorage.getLocs().get(i);
            World world = location.getWorld();
            String loc = world.getUID() + "_" + location.getX() + "_" + location.getY() + "_" + location.getZ();

            locs[i] = loc;

        }

        return new FurnitureStorageData(ownerUUID, hashMap, furnitureStorageID, furnitureStorageConfigID,locs);
    }

    public FurnitureStorage furnitureStorageDataToFurnitureStorage(FurnitureStorageData furnitureStorageData){
        if(furnitureStorageData == null) return null;
        StorageManager storageManager = core.getManagers().getStorageManager();
        UUID ownerUUID = furnitureStorageData.getOwnerUUID();
        HashMap<String, Storage> hashMap = new HashMap<>();
        String furnitureStorageID = furnitureStorageData.getFurnitureStorageID();
        String furnitureStorageConfigID = furnitureStorageData.getFurnitureStorageConfigID();
        furnitureStorageData.getStoragesID().forEach((s, s2) -> {
            if(storageManager.storageExists(s2)) hashMap.put(s, storageManager.getStorage(s2));
        });
        ArrayList<Location> locs = new ArrayList<>();
        String[] locsSerializable = furnitureStorageData.getLocs();
        for(int i=0;i<furnitureStorageData.getLocs().length;i++){

            String[] loc = locsSerializable[i].split("_");
            World world = Bukkit.getWorld(UUID.fromString(loc[0]));
            double x = Double.parseDouble(loc[1]);
            double y = Double.parseDouble(loc[2]);
            double z = Double.parseDouble(loc[3]);
            if(world == null){
                AdventureUtils.sendMessagePluginConsole(core,"<red> Error loading FurnitureStorage. plz contact wuason at discord.");
            }
            Location location = new Location(world,x,y,z);
            locs.add(location);

        }

        return new FurnitureStorage(furnitureStorageID, furnitureStorageConfigID, hashMap, ownerUUID,locs);
    }

}
