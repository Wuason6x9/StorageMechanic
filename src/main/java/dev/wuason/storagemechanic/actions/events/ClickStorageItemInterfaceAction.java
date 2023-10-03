package dev.wuason.storagemechanic.actions.events;

import dev.wuason.storagemechanic.items.ItemInterface;
import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import org.bukkit.event.Event;

import java.util.HashMap;
import java.util.Locale;

public class ClickStorageItemInterfaceAction extends EventAction {

    private ItemInterface itemInterface;
    private StorageInventory storageInventory;


    public ClickStorageItemInterfaceAction(Event event, ItemInterface itemInterface, StorageInventory storageInventory) {
        super(ClickStorageItemInterfaceAction.class.getSimpleName(), event);
        this.itemInterface = itemInterface;
        this.storageInventory = storageInventory;
    }

    public ItemInterface getItemInterface() {
        return itemInterface;
    }

    public StorageInventory getStorageInventory() {
        return storageInventory;
    }

    @Override
    public void registerPlaceholders(HashMap<String, Object> currentPlaceholders) {
        currentPlaceholders.put("$currentPage$".toUpperCase().intern(), storageInventory.getPage());
        currentPlaceholders.put("$currentBukkitInventoryStorage$".toUpperCase().intern(), storageInventory.getInventory());
        currentPlaceholders.put("$currentStorageInventory$".toUpperCase().intern(), storageInventory);
        currentPlaceholders.put("$currentStorageInventory_Id$".toUpperCase().intern(), storageInventory.getId().intern());

        currentPlaceholders.put("$clickedItemInterface$".toUpperCase().intern(), itemInterface);
        currentPlaceholders.put("$clickedItemInterface_Id$".toUpperCase().intern(), itemInterface.getId().intern().intern());
        currentPlaceholders.put("$clickedItemInterface_type$".toUpperCase().intern(), itemInterface.getItemInterfaceType().toString().intern());
    }
}
