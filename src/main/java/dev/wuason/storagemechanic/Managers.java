package dev.wuason.storagemechanic;

import dev.wuason.mechanics.Mechanics;
import dev.wuason.storagemechanic.customblocks.CustomBlockManager;
import dev.wuason.storagemechanic.items.ItemInterface;
import dev.wuason.storagemechanic.items.ItemInterfaceManager;
import dev.wuason.storagemechanic.storages.StorageManager;
import dev.wuason.storagemechanic.storages.config.StorageConfigManager;
import dev.wuason.storagemechanic.storages.inventory.StorageInventoryManager;
import dev.wuason.storagemechanic.storages.types.block.BlockStorageManager;
import dev.wuason.storagemechanic.storages.types.block.config.BlockStorageConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

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

    public Managers(StorageMechanic core) {
        this.core = core;
        Mechanics.getInstance().getMechanicsManager().getMechanic(core).setManagersClass(this);
    }

    public void loadManagers(){

        customBlockManager = new CustomBlockManager(core); //1
        itemInterfaceManager = new ItemInterfaceManager(core);//2
        storageConfigManager = new StorageConfigManager(core);//3
        blockStorageConfigManager = new BlockStorageConfigManager(core); //4
        configManager = new ConfigManager(core); //
        commandManager = new CommandManager(core);
        commandManager.loadCommand();
        storageInventoryManager = new StorageInventoryManager();
        storageManager = new StorageManager(core);
        blockStorageManager = new BlockStorageManager(core);





        PluginManager pm = Bukkit.getPluginManager();

        pm.registerEvents(customBlockManager, core);
        pm.registerEvents(storageManager,core);
        pm.registerEvents(blockStorageManager,core);

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
}
