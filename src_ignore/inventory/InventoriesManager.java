package dev.wuason.storagemechanic.inventory;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.inventory.inventories.SearchItem.SearchItemInventoryManager;
import dev.wuason.storagemechanic.storages.StorageManager;

public class InventoriesManager {
    private StorageMechanic core;
    private SearchItemInventoryManager searchItemInventoryManager;
    private StorageManager storageManager;

    public InventoriesManager(StorageMechanic core, StorageManager storageManager) {
        this.core = core;
        this.searchItemInventoryManager = new SearchItemInventoryManager(this,core, storageManager);
        this.storageManager = storageManager;
    }

    public SearchItemInventoryManager getSearchItemInventoryManager() {
        return searchItemInventoryManager;
    }

    public void stop(){
        searchItemInventoryManager.stop();
    }
}
