package dev.wuason.storagemechanic.inventory.inventories.SearchItem;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.inventory.InventoriesManager;
import dev.wuason.storagemechanic.inventory.config.InventoryConfig;
import dev.wuason.storagemechanic.inventory.inventories.SearchItem.anvil.AnvilInventory;
import dev.wuason.storagemechanic.inventory.inventories.SearchItem.anvil.InventoryAnvilManager;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

public class SearchItemInventoryManager {
    private InventoryAnvilManager inventoryAnvilManager;
    private StorageMechanic core;
    private InventoriesManager inventoriesManager;
    private StorageManager storageManager;

    public SearchItemInventoryManager(InventoriesManager inventoriesManager, StorageMechanic core, StorageManager storageManager){
        this.core = core;
        this.inventoriesManager = inventoriesManager;
        inventoryAnvilManager = new InventoryAnvilManager(core, storageManager);
        Bukkit.getPluginManager().registerEvents(inventoryAnvilManager,core);
        this.storageManager = storageManager;
    }

    public void openSearchItemMenu(Player player, String configId, String configIdInvResult, Storage storage, SearchType searchType){
        player.closeInventory();
        InventoryConfig inventoryConfig = core.getManagers().getInventoryConfigManager().getInventories().get(configId);

        if(inventoryConfig.getInventoryType().equals(InventoryType.ANVIL)){
            AnvilInventory anvilInventory = inventoryAnvilManager.createAnvilInventory(player,configId,storage,configIdInvResult,searchType);
            anvilInventory.open();
            return;
        }

    }

    public void stop(){
        inventoryAnvilManager.stop();
    }
}
