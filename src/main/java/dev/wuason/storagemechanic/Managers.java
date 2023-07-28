package dev.wuason.storagemechanic;

import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.storagemechanic.compatibilities.MythicMobs;
import dev.wuason.storagemechanic.customblocks.CustomBlockManager;
import dev.wuason.storagemechanic.data.DataManager;
import dev.wuason.storagemechanic.items.ItemInterface;
import dev.wuason.storagemechanic.items.ItemInterfaceManager;
import dev.wuason.storagemechanic.storages.StorageManager;
import dev.wuason.storagemechanic.storages.config.StorageConfigManager;
import dev.wuason.storagemechanic.storages.inventory.StorageInventoryManager;
import dev.wuason.storagemechanic.storages.types.block.BlockStorageManager;
import dev.wuason.storagemechanic.storages.types.block.config.BlockStorageConfigManager;
import dev.wuason.storagemechanic.storages.types.entity.EntityMythicManager;
import dev.wuason.storagemechanic.storages.types.furnitures.FurnitureStorageManager;
import dev.wuason.storagemechanic.storages.types.furnitures.config.FurnitureStorageConfigManager;
import dev.wuason.storagemechanic.storages.types.item.ItemStorageManager;
import dev.wuason.storagemechanic.storages.types.item.config.ItemStorageConfigManager;
import dev.wuason.storagemechanic.systems.TrashSystemManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.UUID;

public class Managers {
    private StorageMechanic core;

    private CustomBlockManager customBlockManager;
    private ItemInterfaceManager itemInterfaceManager;
    private ConfigManager configManager;
    private StorageConfigManager storageConfigManager;
    private CommandManager commandManager;
    private StorageInventoryManager storageInventoryManager;
    private StorageManager storageManager;
    private BlockStorageManager blockStorageManager;
    private BlockStorageConfigManager blockStorageConfigManager;
    private DataManager dataManager;
    private TrashSystemManager trashSystemManager;
    private ItemStorageConfigManager itemStorageConfigManager;
    private ItemStorageManager itemStorageManager;
    private EntityMythicManager entityMythicManager;
    private FurnitureStorageManager furnitureStorageManager;
    private FurnitureStorageConfigManager furnitureStorageConfigManager;

    public Managers(StorageMechanic core) {
        this.core = core;
    }

    public void loadManagers(){
        AdventureUtils.sendMessagePluginConsole(core," Starting Managers!");
        customBlockManager = new CustomBlockManager(core); //1
        itemInterfaceManager = new ItemInterfaceManager(core);//2
        storageConfigManager = new StorageConfigManager(core);//3
        blockStorageConfigManager = new BlockStorageConfigManager(core); //4
        itemStorageConfigManager = new ItemStorageConfigManager(core); //5
        furnitureStorageConfigManager = new FurnitureStorageConfigManager(core); // 6

        configManager = new ConfigManager(core); //7

        Bukkit.getScheduler().runTaskAsynchronously(core,() -> configManager.load());

        dataManager = new DataManager(core); //6
        commandManager = new CommandManager(core);
        commandManager.loadCommand();
        storageInventoryManager = new StorageInventoryManager();
        storageManager = new StorageManager(core, dataManager);
        blockStorageManager = new BlockStorageManager(core, dataManager);
        itemStorageManager = new ItemStorageManager(core);
        entityMythicManager = new EntityMythicManager(core);
        furnitureStorageManager = new FurnitureStorageManager(core, dataManager);


        trashSystemManager = new TrashSystemManager(core,dataManager,blockStorageConfigManager,furnitureStorageConfigManager); //7
        try {
            trashSystemManager.cleanTrash();
        }catch (Exception e){
            e.printStackTrace();
        }




        PluginManager pm = Bukkit.getPluginManager();
        if(MythicMobs.isExistMythic()){
            AdventureUtils.sendMessagePluginConsole(core," <yellow>MythicMobs hooked!");
            pm.registerEvents(entityMythicManager,core);
        }
        pm.registerEvents(customBlockManager, core);
        pm.registerEvents(storageManager,core);
        pm.registerEvents(blockStorageManager,core);
        pm.registerEvents(itemStorageManager,core);

    }

    public void stop(){
        AdventureUtils.sendMessagePluginConsole(core,"<red> Stopping StorageMechanic...");
        //storages types

        //BLOCKSTORAGE
        AdventureUtils.sendMessagePluginConsole(core,"<red> Stopping BlockStorageManager...");
        blockStorageManager.stop();
        //FURNITURESTORAGE
        AdventureUtils.sendMessagePluginConsole(core,"<red> Stopping FurnitureManager...");
        furnitureStorageManager.stop();
        //storages
        AdventureUtils.sendMessagePluginConsole(core,"<red> Stopping StorageManager...");
        storageManager.stop();
        //data stop
        AdventureUtils.sendMessagePluginConsole(core,"<red> Stopping DataManger...");
        dataManager.stop();
    }

    public CustomBlockManager getCustomBlockManager() {
        return customBlockManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ItemInterfaceManager getItemInterfaceManager() {
        return itemInterfaceManager;
    }

    public StorageConfigManager getStorageConfigManager() {
        return storageConfigManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public StorageInventoryManager getStorageInventoryManager() {
        return storageInventoryManager;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public BlockStorageManager getBlockStorageManager() {
        return blockStorageManager;
    }

    public BlockStorageConfigManager getBlockStorageConfigManager() {
        return blockStorageConfigManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public TrashSystemManager getTrashSystemManager() {
        return trashSystemManager;
    }

    public ItemStorageConfigManager getItemStorageConfigManager() {
        return itemStorageConfigManager;
    }

    public ItemStorageManager getItemStorageManager() {
        return itemStorageManager;
    }

    public EntityMythicManager getEntityMythicManager() {
        return entityMythicManager;
    }

    public FurnitureStorageManager getFurnitureStorageManager() {
        return furnitureStorageManager;
    }

    public FurnitureStorageConfigManager getFurnitureStorageConfigManager() {
        return furnitureStorageConfigManager;
    }
}
