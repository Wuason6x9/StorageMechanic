package dev.wuason.storagemechanic.api.events.storage;

import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CloseStorageEvent extends Event {

    private Player player;
    private StorageInventory storageInventory;

    public CloseStorageEvent(Player player, StorageInventory storageInventory) {
        this.player = player;
        this.storageInventory = storageInventory;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public StorageInventory getStorageInventory() {
        return storageInventory;
    }

    public Player getPlayer() {
        return player;
    }
}
