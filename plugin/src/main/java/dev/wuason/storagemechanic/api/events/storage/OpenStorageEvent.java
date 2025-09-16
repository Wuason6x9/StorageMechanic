package dev.wuason.storagemechanic.api.events.storage;

import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class OpenStorageEvent extends Event {
    private Player player;
    private StorageInventory storageInventory;
    private static final HandlerList HANDLERS = new HandlerList();

    public OpenStorageEvent(Player player, StorageInventory storageInventory) {
        this.player = player;
        this.storageInventory = storageInventory;
    }

    public StorageInventory getStorageInventory() {
        return storageInventory;
    }

    public Player getPlayer() {
        return player;
    }


    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }
}
