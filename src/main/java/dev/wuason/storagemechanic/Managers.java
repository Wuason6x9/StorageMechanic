package dev.wuason.storagemechanic;

import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.storagemechanic.actions.ActionManager;
import dev.wuason.storagemechanic.compatibilities.Compatibilities;
import dev.wuason.storagemechanic.customitems.CustomItemsManager;
import dev.wuason.storagemechanic.data.DataManager;
import dev.wuason.storagemechanic.inventory.InventoryConfigManager;
import dev.wuason.storagemechanic.items.ItemInterfaceManager;
import dev.wuason.storagemechanic.recipes.RecipesManager;
import dev.wuason.storagemechanic.storages.StorageManager;
import dev.wuason.storagemechanic.storages.config.StorageConfigManager;
import dev.wuason.storagemechanic.storages.inventory.StorageInventoryManager;
import dev.wuason.storagemechanic.storages.types.api.PlaceHolderApiStorageApi;
import dev.wuason.storagemechanic.storages.types.api.StorageApiManager;
import dev.wuason.storagemechanic.storages.types.block.BlockStorageManager;
import dev.wuason.storagemechanic.storages.types.block.config.BlockStorageConfigManager;
import dev.wuason.storagemechanic.storages.types.block.mechanics.BlockMechanicManager;
import dev.wuason.storagemechanic.storages.types.entity.MythicManager;
import dev.wuason.storagemechanic.storages.types.furnitures.FurnitureStorageManager;
import dev.wuason.storagemechanic.storages.types.furnitures.config.FurnitureStorageConfigManager;
import dev.wuason.storagemechanic.storages.types.item.ItemStorageManager;
import dev.wuason.storagemechanic.storages.types.item.config.ItemStorageConfigManager;
import dev.wuason.storagemechanic.systems.TrashSystemManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitTask;

public class Managers {
    private StorageMechanic core;

    private CustomItemsManager customItemsManager;
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
    private MythicManager mythicManager;
    private FurnitureStorageManager furnitureStorageManager;
    private FurnitureStorageConfigManager furnitureStorageConfigManager;
    private BlockMechanicManager blockMechanicManager;
    private ActionManager actionManager;
    private StorageApiManager storageApiManager;
    private RecipesManager recipesManager;
    private BukkitTask saveDataTask;
    private InventoryConfigManager inventoryConfigManager;


    public Managers(StorageMechanic core) {
        this.core = core;
    }

    public void loadManagers() {

        core.getDataFolder().mkdirs();

        //NEW MECHANICS
        this.blockMechanicManager = new BlockMechanicManager(core);

        //CONFIGS
        loadConfigManagers();

        //PlaceHolderApi
        placeHolderApiHook();
        //DEBUGS

        //MANAGERS
        //********* DATA *********
        dataManager = new DataManager(core);
        //********* COMMANDS *********
        commandManager = new CommandManager(core);
        commandManager.loadCommand();
        //********* STORAGES *********
        storageInventoryManager = new StorageInventoryManager();
        storageManager = new StorageManager(core, dataManager, this);
        blockStorageManager = new BlockStorageManager(core, dataManager, blockMechanicManager);
        itemStorageManager = new ItemStorageManager(core);
        if (Compatibilities.isMythicMobsLoaded()) {
            AdventureUtils.sendMessagePluginConsole(core, " <yellow>MythicMobs hooked!");
            mythicManager = new MythicManager(core);
        }
        furnitureStorageManager = new FurnitureStorageManager(core, dataManager);
        storageApiManager = new StorageApiManager(core);
        //********* INVENTORIES *********
        inventoryConfigManager = new InventoryConfigManager(core); //change this line

        //********* ANTI TRASH *********

        trashSystemManager = new TrashSystemManager(core, dataManager, blockStorageConfigManager, furnitureStorageConfigManager); //7
        try {
            trashSystemManager.cleanTrash();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //********* REGISTER EVENTS *********
        registerEvents();

    }

    public void loadConfigManagers() {
        customItemsManager = new CustomItemsManager(core); //1
        //********* ACTIONS *********
        actionManager = new ActionManager(core);
        itemInterfaceManager = new ItemInterfaceManager(core);//2
        storageConfigManager = new StorageConfigManager(core);//3
        blockStorageConfigManager = new BlockStorageConfigManager(core); //4
        itemStorageConfigManager = new ItemStorageConfigManager(core); //5
        furnitureStorageConfigManager = new FurnitureStorageConfigManager(core); // 6
        inventoryConfigManager = new InventoryConfigManager(core); //7
        recipesManager = new RecipesManager(core); //8
        configManager = new ConfigManager(core); //9
        configManager.load(); //Load config
    }

    public void registerEvents() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(customItemsManager, core);
        pm.registerEvents(storageManager, core);
        pm.registerEvents(blockStorageManager, core);
        pm.registerEvents(itemStorageManager, core);
        pm.registerEvents(storageApiManager, core);
        if (Compatibilities.isMythicMobsLoaded()) {
            pm.registerEvents(mythicManager, core);
        }
    }

    public void saveAllData() {
        blockStorageManager.saveAllBlockStorages();
        furnitureStorageManager.saveAllFurnitureStorages();
        storageManager.saveAllStorages();
        dataManager.saveAllData();
    }

    public void runDataSaveTask() {
        saveDataTask = Bukkit.getScheduler().runTaskTimerAsynchronously(core, () -> {
            if (configManager.getMainConfig().getBoolean("logs.show_save_logs", true)) {
                AdventureUtils.sendMessagePluginConsole(core, "<red> Starting save data...");
            }
            storageManager.antiTrashTask();
            saveAllData();
            if (configManager.getMainConfig().getBoolean("logs.show_save_logs", true)) {
                AdventureUtils.sendMessagePluginConsole(core, "<green> Data saved!");
            }
        }, (20 * 60) * 1, (20 * 60) * configManager.getMainConfig().getInt("data.save_all_data_in", 15));
    }

    public void stopAndRemoveDataSaveTask() {
        if (saveDataTask != null) saveDataTask.cancel();
        saveDataTask = null;
    }

    public void placeHolderApiHook() {
        //PlaceHolderApi
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            //message to console placeholder hooked
            AdventureUtils.sendMessagePluginConsole(core, "<yellow>PlaceholderAPI hooked!");
            new PlaceHolderApiStorageApi(core).register();
        }
    }

    public void stop() {
        saveDataTask.cancel();
        AdventureUtils.sendMessagePluginConsole(core, "<red> Stopping StorageMechanic...");

        //storages types

        //BLOCKSTORAGE
        AdventureUtils.sendMessagePluginConsole(core, "<red> Stopping BlockStorageManager...");
        blockStorageManager.stop();
        //FURNITURESTORAGE
        AdventureUtils.sendMessagePluginConsole(core, "<red> Stopping FurnitureManager...");
        furnitureStorageManager.stop();
        //StorageApi
        AdventureUtils.sendMessagePluginConsole(core, "<red> Stopping StorageApiManager...");
        storageApiManager.stop();
        //storages
        AdventureUtils.sendMessagePluginConsole(core, "<red> Stopping StorageManager...");
        storageManager.stop();
        //data stop
        AdventureUtils.sendMessagePluginConsole(core, "<red> Stopping DataManger...");
        dataManager.stop();
    }

    public CustomItemsManager getCustomItemsManager() {
        return customItemsManager;
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

    public MythicManager getMythicManager() {
        return mythicManager;
    }

    public FurnitureStorageManager getFurnitureStorageManager() {
        return furnitureStorageManager;
    }

    public InventoryConfigManager getInventoryConfigManager() {
        return inventoryConfigManager;
    }

    public FurnitureStorageConfigManager getFurnitureStorageConfigManager() {
        return furnitureStorageConfigManager;
    }

    public BlockMechanicManager getBlockMechanicManager() {
        return blockMechanicManager;
    }

    public ActionManager getActionManager() {
        return actionManager;
    }

    public StorageApiManager getStorageApiManager() {
        return storageApiManager;
    }

    public RecipesManager getRecipesManager() {
        return recipesManager;
    }
}
