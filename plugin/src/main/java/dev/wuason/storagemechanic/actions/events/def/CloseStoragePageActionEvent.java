package dev.wuason.storagemechanic.actions.events.def;

import dev.wuason.mechanics.actions.Action;
import dev.wuason.mechanics.actions.events.EventAction;
import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Locale;

public class CloseStoragePageActionEvent implements EventAction {
    private final StorageInventory storageInventory;
    private final InventoryCloseEvent event;

    public CloseStoragePageActionEvent(StorageInventory storageInventory, InventoryCloseEvent event) {
        this.storageInventory = storageInventory;
        this.event = event;
    }

    @Override
    public void registerPlaceholders(Action action) {
        action.registerPlaceholder("$page$", storageInventory.getPage());
        action.registerPlaceholder("$storageInventory$", storageInventory);
        action.registerPlaceholder("$inventory$", storageInventory.getInventory());
        action.registerPlaceholder("$bukkitEvent$", event);
        action.registerPlaceholder("$player$", event.getPlayer());
        action.registerPlaceholder("$bukkitEventName$", event.getEventName());
        action.registerPlaceholder("$player_name$", event.getPlayer().getName());
    }

    @Override
    public String getId() {
        return "close_storage_page".toUpperCase(Locale.ENGLISH);
    }

    public StorageInventory getStorageInventory() {
        return storageInventory;
    }

    public InventoryCloseEvent getEvent() {
        return event;
    }
}
