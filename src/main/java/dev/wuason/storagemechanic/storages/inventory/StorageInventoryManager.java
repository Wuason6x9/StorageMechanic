package dev.wuason.storagemechanic.storages.inventory;

import dev.wuason.storagemechanic.storages.config.StorageConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StorageInventoryManager {

    private Map<String, StorageInventory> storageInventories;

    public StorageInventoryManager() {
        storageInventories = new HashMap<>();
    }

    public StorageInventory createStorageInventory(StorageConfig storageConfig) {
        StorageInventory storageInventory = new StorageInventory(storageConfig);
        storageInventories.put(storageInventory.getId(), storageInventory);
        return storageInventory;
    }

    public StorageInventory createStorageInventory(InventoryType inventoryType, String title) {
        StorageInventory storageInventory = new StorageInventory(inventoryType, title);
        storageInventories.put(storageInventory.getId(), storageInventory);
        return storageInventory;
    }

    public StorageInventory createStorageInventory(int rows, String title) {
        StorageInventory storageInventory = new StorageInventory(rows, title);
        storageInventories.put(storageInventory.getId(), storageInventory);
        return storageInventory;
    }

    public StorageInventory getStorageInventory(String id) {
        return storageInventories.get(id);
    }

    public StorageInventory getStorageInventory(InventoryHolder holder) {
        Optional<StorageInventory> optionalStorageInventory = storageInventories.values().stream()
                .filter(storageInventory -> storageInventory.getInventoryHolder().equals(holder))
                .findFirst();
        return optionalStorageInventory.orElse(null);
    }

    public void removeStorageInventory(String id) {
        storageInventories.remove(id);
    }

    public void openStorageInventory(Player player, String id) {
        StorageInventory storageInventory = getStorageInventory(id);
        if (storageInventory != null) {
            player.openInventory(storageInventory.getInventory());
        }
    }
}