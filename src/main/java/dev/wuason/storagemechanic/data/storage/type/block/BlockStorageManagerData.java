package dev.wuason.storagemechanic.data.storage.type.block;

import dev.wuason.mechanics.data.Data;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.data.DataManager;
import dev.wuason.storagemechanic.data.storage.StorageData;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageManager;
import dev.wuason.storagemechanic.storages.types.block.BlockStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class BlockStorageManagerData {
    private DataManager dataManager;
    private StorageMechanic core;
    final public String DATA_TYPE = BlockStorageData.class.getSimpleName();
    public BlockStorageManagerData(DataManager dataManager, StorageMechanic core){
        this.core = core;
        this.dataManager = dataManager;
    }

    //basics
    public void saveBlockStorage(BlockStorage blockStorage){
        if(blockStorage == null) return;
        saveBlockStorageData(blockStorageToblockStorageData(blockStorage));
    }
    public void saveBlockStorageData(BlockStorageData blockStorageData){
        if(blockStorageData == null) return;
        Data data = null;
        if(existBlockStorageData(blockStorageData.getBlockStorageID())){
            data = dataManager.getData(DATA_TYPE, blockStorageData.getBlockStorageID());
        }
        if(data == null){
            data = new Data(blockStorageData.getBlockStorageID());
        }
        data.setDataObject(blockStorageData);
        dataManager.saveData(data);
    }
    public void removeBlockStorageData(String id){
        if(existBlockStorageData(id)){
            dataManager.removeData(DATA_TYPE,id);
        }
    }
    public BlockStorage loadBlockStorageData(String id){
        BlockStorageData blockStorageData = getBlockStorageData(id);
        if(blockStorageData == null) return null;
        return blockStorageDataToBlockStorage(blockStorageData);
    }
    public BlockStorageData getBlockStorageData(String id){
        if(existBlockStorageData(id)){
            return (BlockStorageData) dataManager.getData(DATA_TYPE,id).getDataObject();
        }
        return null;
    }
    public boolean existBlockStorageData(String id){
        return dataManager.existData(DATA_TYPE,id);
    }



    //serialize
    public BlockStorageData blockStorageToblockStorageData(BlockStorage blockStorage){
        if(blockStorage == null) return null;
        UUID ownerUUID = blockStorage.getOwnerUUID();
        HashMap<String,String> hashMap = new HashMap<>();
        String blockStorageID = blockStorage.getId();
        String blockStorageConfigID = blockStorage.getBlockStorageConfigID();
        blockStorage.getStorages().forEach((s, storage) -> hashMap.put(s,storage.getId()));

        String[] locs = new String[blockStorage.getLocs().size()];

        for(int i=0;i<blockStorage.getLocs().size();i++){

            Location location = blockStorage.getLocs().get(i);
            World world = location.getWorld();
            String loc = world.getUID() + "_" + location.getX() + "_" + location.getY() + "_" + location.getZ();

            locs[i] = loc;

        }

        return new BlockStorageData(ownerUUID, hashMap, blockStorageID, blockStorageConfigID,locs);
    }

    public BlockStorage blockStorageDataToBlockStorage(BlockStorageData blockStorageData){
        if(blockStorageData == null) return null;
        StorageManager storageManager = core.getManagers().getStorageManager();
        UUID ownerUUID = blockStorageData.getOwnerUUID();
        HashMap<String, Storage> hashMap = new HashMap<>();
        String blockStorageID = blockStorageData.getBlockStorageID();
        String blockStorageConfigID = blockStorageData.getBlockStorageConfigID();
        blockStorageData.getStoragesID().forEach((s, s2) -> {
            if(storageManager.storageExists(s2)) hashMap.put(s, storageManager.getStorage(s2));
        });

        ArrayList<Location> locs = new ArrayList<>();
        String[] locsSerializable = blockStorageData.getLocs();
        for(int i=0;i<blockStorageData.getLocs().length;i++){

            String[] loc = locsSerializable[i].split("_");
            World world = Bukkit.getWorld(UUID.fromString(loc[0]));
            double x = Double.parseDouble(loc[1]);
            double y = Double.parseDouble(loc[2]);
            double z = Double.parseDouble(loc[3]);
            if(world == null){
                AdventureUtils.sendMessagePluginConsole(core,"<red> Error loading BlockStorage. plz contact wuason at discord.");
            }
            Location location = new Location(world,x,y,z);
            locs.add(location);

        }

        return new BlockStorage(blockStorageID, blockStorageConfigID, hashMap, ownerUUID,locs);
    }

}
