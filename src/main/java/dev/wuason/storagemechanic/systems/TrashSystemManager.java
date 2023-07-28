package dev.wuason.storagemechanic.systems;

import dev.wuason.mechanics.data.Data;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.data.DataManager;
import dev.wuason.storagemechanic.data.player.PlayerData;
import dev.wuason.storagemechanic.data.storage.type.block.BlockStorageData;
import dev.wuason.storagemechanic.data.storage.type.furniture.FurnitureStorageData;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageManager;
import dev.wuason.storagemechanic.storages.types.block.BlockStorage;
import dev.wuason.storagemechanic.storages.types.block.BlockStorageManager;
import dev.wuason.storagemechanic.storages.types.block.config.BlockStorageConfigManager;
import dev.wuason.storagemechanic.storages.types.furnitures.FurnitureStorage;
import dev.wuason.storagemechanic.storages.types.furnitures.FurnitureStorageManager;
import dev.wuason.storagemechanic.storages.types.furnitures.config.FurnitureStorageConfigManager;
import dev.wuason.storagemechanic.storages.types.item.ItemStorageManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TrashSystemManager implements Listener {
    private StorageMechanic core;
    private DataManager dataManager;
    private BlockStorageConfigManager blockStorageConfigManager;
    FurnitureStorageConfigManager furnitureStorageConfigManager;

    public TrashSystemManager(StorageMechanic core, DataManager dataManager, BlockStorageConfigManager blockStorageConfigManager, FurnitureStorageConfigManager furnitureStorageConfigManager){
        this.core = core;
        this.dataManager = dataManager;
        this.blockStorageConfigManager = blockStorageConfigManager;
        this.furnitureStorageConfigManager = furnitureStorageConfigManager;
    }
    public void checkTrashOnChunk(PersistentDataContainer persistentDataContainer){
        checkTrashOnChunkBlockStorage(persistentDataContainer); //BLOCKSTORAGE
        checkTrashOnChunkCustomBlock(persistentDataContainer); //CUSTOM BLOCKS
        checkTrashOnChunkFurnitureStorage(persistentDataContainer); //FURNITURE
    }
    public void checkTrashOnChunkCustomBlock(PersistentDataContainer persistentDataContainer){

        for(NamespacedKey namespacedKey : persistentDataContainer.getKeys()){

            if(namespacedKey.getKey().contains("storagemechanicb")){

                String customBlockId = persistentDataContainer.get(namespacedKey,PersistentDataType.STRING);

                if(!core.getManagers().getCustomBlockManager().customBlockExists(customBlockId)){
                    persistentDataContainer.remove(namespacedKey);
                }

            }

        }

    }
    public void checkTrashOnChunkBlockStorage(PersistentDataContainer persistentDataContainer){

        for(NamespacedKey namespacedKey : persistentDataContainer.getKeys()){

            if(namespacedKey.getKey().contains("blockstorage")){

                String[] blockStorageSrc = persistentDataContainer.get(namespacedKey, PersistentDataType.STRING).split(":");

                if(!blockStorageConfigManager.blockStorageConfigExists(blockStorageSrc[1])){
                    BlockStorage blockStorage = dataManager.getStorageManagerData().getBlockStorageManagerData().loadBlockStorageData(blockStorageSrc[0]);
                    if(blockStorage != null){
                        blockStorage.delete();
                        BlockStorageManager blockStorageManager = core.getManagers().getBlockStorageManager();
                        blockStorageManager.removeBlockStorage(blockStorage.getId());
                    }
                    persistentDataContainer.remove(namespacedKey);
                }

            }

        }

    }
    public void checkTrashOnChunkFurnitureStorage(PersistentDataContainer persistentDataContainer){

        for(NamespacedKey namespacedKey : persistentDataContainer.getKeys()){

            if(namespacedKey.getKey().contains("furniturestorage")){

                String[] furnitureStorageSrc = persistentDataContainer.get(namespacedKey, PersistentDataType.STRING).split(":");

                if(!furnitureStorageConfigManager.furnitureStorageConfigExists(furnitureStorageSrc[1])){
                    FurnitureStorage furnitureStorage = dataManager.getStorageManagerData().getFurnitureStorageManagerData().loadFurnitureStorageData(furnitureStorageSrc[0]);
                    if(furnitureStorage != null){
                        furnitureStorage.delete();
                        FurnitureStorageManager furnitureStorageManager = core.getManagers().getFurnitureStorageManager();
                        furnitureStorageManager.removeFurnitureStorage(furnitureStorage.getId());
                    }
                    persistentDataContainer.remove(namespacedKey);
                }

            }

        }

    }

    public void cleanTrash(){
        AdventureUtils.sendMessagePluginConsole(core,"<red> Starting Anti-Trash System.");
        cleanTrashBlockStorages();
        cleanTrashFurnitureStorages();
        cleanTrashItemsStorages();
    }
    public void cleanTrashBlockStorages(){
        Bukkit.getScheduler().runTaskAsynchronously(core,() ->{
            AdventureUtils.sendMessagePluginConsole(core,"<red> Anti-Trash -> Waiting BlockStorage Config...");
            core.getManagers().getConfigManager().isConfigLoaded();
            AdventureUtils.sendMessagePluginConsole(core,"<red> Anti-Trash -> Checking BlockStorages...");
            ArrayList<BlockStorageData> trashData = new ArrayList<>();

            Data[] datas = dataManager.getAllData(BlockStorageData.class.getSimpleName());

            if(datas == null) return;

            if(datas.length == 0) return;

            BlockStorageData[] blockStoragesData = Arrays.stream(datas).map(data -> (BlockStorageData) data.getDataObject()).toArray(BlockStorageData[]::new);

            if(blockStoragesData.length == 0) return;

            for(BlockStorageData blockData : blockStoragesData){

                String configId = blockData.getBlockStorageConfigID();
                if(!blockStorageConfigManager.blockStorageConfigExists(configId)){
                    trashData.add(blockData);
                }
            }
            AdventureUtils.sendMessagePluginConsole(core,"<red> Anti-Trash -> Cleaning <aqua>" + datas.length + " <red> BlockStorages.");

            for(BlockStorageData blockStorageDataTrash : trashData){
                BlockStorage blockStorage = dataManager.getStorageManagerData().getBlockStorageManagerData().loadBlockStorageData(blockStorageDataTrash.getBlockStorageID());

                blockStorage.delete();
                core.getManagers().getBlockStorageManager().removeBlockStorage(blockStorage.getId());

            }
            AdventureUtils.sendMessagePluginConsole(core,"<red> Anti-Trash -> Cleaned <aqua>" + trashData.size() + " <red> BlockStorages.");

        });
    }

    public void cleanTrashFurnitureStorages(){
        Bukkit.getScheduler().runTaskAsynchronously(core,() ->{
            AdventureUtils.sendMessagePluginConsole(core,"<red> Anti-Trash -> Waiting FurnitureStorage Config...");
            core.getManagers().getConfigManager().isConfigLoaded();
            AdventureUtils.sendMessagePluginConsole(core,"<red> Anti-Trash -> Checking FurnitureStorages...");
            ArrayList<FurnitureStorageData> trashData = new ArrayList<>();

            Data[] datas = dataManager.getAllData(FurnitureStorageData.class.getSimpleName());

            if(datas == null) return;

            if(datas.length == 0) return;

            FurnitureStorageData[] furnitureStoragesData = Arrays.stream(datas).map(data -> (FurnitureStorageData) data.getDataObject()).toArray(FurnitureStorageData[]::new);

            if(furnitureStoragesData.length == 0) return;

            for(FurnitureStorageData furnitureData : furnitureStoragesData){

                String configId = furnitureData.getFurnitureStorageConfigID();
                if(!furnitureStorageConfigManager.furnitureStorageConfigExists(configId)){
                    trashData.add(furnitureData);
                }
            }
            AdventureUtils.sendMessagePluginConsole(core,"<red> Anti-Trash -> Cleaning <aqua>" + datas.length + " <red> FurnitureStorages.");

            for(FurnitureStorageData furnitureStorageDataTrash : trashData){
                FurnitureStorage furnitureStorage = dataManager.getStorageManagerData().getFurnitureStorageManagerData().loadFurnitureStorageData(furnitureStorageDataTrash.getFurnitureStorageID());

                furnitureStorage.delete();
                core.getManagers().getFurnitureStorageManager().removeFurnitureStorage(furnitureStorage.getId());

            }
            AdventureUtils.sendMessagePluginConsole(core,"<red> Anti-Trash -> Cleaned <aqua>" + trashData.size() + " <red> FurnitureStorages.");

        });
    }

    public void cleanTrashItemsStorages(){
        Bukkit.getScheduler().runTaskAsynchronously(core,() ->{
            AdventureUtils.sendMessagePluginConsole(core,"<red> Anti-Trash -> Waiting ItemsStorage Config...");
            core.getManagers().getConfigManager().isConfigLoaded();
            AdventureUtils.sendMessagePluginConsole(core,"<red> Anti-Trash -> Checking ItemsStorages...");
            Map<PlayerData, String> trashData = new HashMap<>();
            Data[] datas = dataManager.getAllData(PlayerData.class.getSimpleName());
            if(datas == null) return;
            if(datas.length == 0) return;
            PlayerData[] playersData = Arrays.stream(datas).map(data -> (PlayerData) data.getDataObject()).toArray(PlayerData[]::new);
            if(playersData.length == 0) return;
            Map<PlayerData, Map.Entry<String,String>> toCheck = new HashMap<>();
            for(PlayerData playerData : playersData){
                for(Map.Entry<String,String> entry : playerData.getStorages().entrySet()){
                    PlayerData pd = dataManager.getPlayerDataManager().getPlayerData(playerData.getUuid());
                    if(entry.getValue().contains(ItemStorageManager.STORAGE_CONTEXT)) toCheck.put(pd, entry);
                }
            }
            AdventureUtils.sendMessagePluginConsole(core,"<red> Anti-Trash -> Cleaning <aqua>" + toCheck.size() + " <red> ItemsStorages.");
            for(Map.Entry<PlayerData,Map.Entry<String,String>> entry : toCheck.entrySet()){
                String itemConfigId = (entry.getValue().getValue().split("_"))[2];
                if(!core.getManagers().getItemStorageConfigManager().existItemStorageConfig(itemConfigId)){
                    trashData.put(entry.getKey(),entry.getValue().getKey());
                }
            }
            for(Map.Entry<PlayerData, String> entry : trashData.entrySet()){
                StorageManager storageManager = core.getManagers().getStorageManager();
                entry.getKey().getStorages().remove(entry.getValue());
                System.out.println(entry.getValue());
                storageManager.removeStorage(entry.getValue());
                if(!Bukkit.getOfflinePlayer(entry.getKey().getUuid()).isOnline()){
                    dataManager.getPlayerDataManager().savePlayerData(entry.getKey().getUuid());
                }
            }
            AdventureUtils.sendMessagePluginConsole(core,"<red> Anti-Trash -> Cleaned <aqua>" + trashData.size() + " <red> ItemsStorages.");
        });
    }


    @EventHandler
    public void BlockStorageLoadChunk(ChunkLoadEvent event){
        PersistentDataContainer persistentDataContainer = event.getChunk().getPersistentDataContainer();

        Bukkit.getScheduler().runTaskAsynchronously(core,() ->{

            try{
                core.getManagers().getTrashSystemManager().checkTrashOnChunk(persistentDataContainer);
            }catch (Exception e){
                e.printStackTrace();
            }

        });
    }

    @EventHandler
    public void BlockStorageUnLoadChunk(ChunkUnloadEvent event){

        PersistentDataContainer persistentDataContainer = event.getChunk().getPersistentDataContainer();

        Bukkit.getScheduler().runTaskAsynchronously(core,() ->{

            core.getManagers().getTrashSystemManager().checkTrashOnChunk(persistentDataContainer);

        });

    }

}
