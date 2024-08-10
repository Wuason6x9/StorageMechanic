package dev.wuason.storagemechanic.api.events.storage;

import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ClickPageStorageEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private InventoryClickEvent inventoryClickEvent;
    private StorageInventory storageInventory;

    public ClickPageStorageEvent(InventoryClickEvent inventoryClickEvent, StorageInventory storageInventory) {
        this.inventoryClickEvent = inventoryClickEvent;
        this.storageInventory = storageInventory;
    }

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public InventoryClickEvent getInventoryClickEvent() {
        return inventoryClickEvent;
    }

    public StorageInventory getStorageInventory() {
        return storageInventory;
    }
}
