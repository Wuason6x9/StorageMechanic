package dev.wuason.storagemechanic.actions.events;

import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import org.bukkit.event.Event;

import java.util.HashMap;

public class OpenStoragePageAction extends EventAction {
    private StorageInventory storageInventory;
    public OpenStoragePageAction(Event event, StorageInventory storageInventory) {
        super(OpenStoragePageAction.class.getSimpleName(), event);
        this.storageInventory = storageInventory;
    }

    @Override
    public void registerPlaceholders(HashMap<String, Object> currentPlaceholders) {
        currentPlaceholders.put("$currentPage$".toUpperCase().intern(), storageInventory.getPage());
        currentPlaceholders.put("$currentBukkitInventoryStorage$".toUpperCase().intern(), storageInventory.getInventory());
        currentPlaceholders.put("$currentStorageInventory$".toUpperCase().intern(), storageInventory);
        currentPlaceholders.put("$currentStorageInventory_Id$".toUpperCase().intern(), storageInventory.getId().intern());
    }
}
