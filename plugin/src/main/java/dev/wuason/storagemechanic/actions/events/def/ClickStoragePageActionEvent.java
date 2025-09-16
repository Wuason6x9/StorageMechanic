package dev.wuason.storagemechanic.actions.events.def;

import dev.wuason.mechanics.actions.Action;
import dev.wuason.mechanics.actions.events.EventAction;
import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Locale;

public class ClickStoragePageActionEvent implements EventAction {
    private final StorageInventory storageInventory;
    private final InventoryClickEvent event;

    public ClickStoragePageActionEvent(StorageInventory storageInventory, InventoryClickEvent event) {
        this.storageInventory = storageInventory;
        this.event = event;
    }

    @Override
    public void registerPlaceholders(Action action) {
        action.registerPlaceholder("$page$", storageInventory.getPage());
        action.registerPlaceholder("$storageInventory$", storageInventory);
        action.registerPlaceholder("$inventory$", storageInventory.getInventory());
        action.registerPlaceholder("$bukkitEvent$", event);
        action.registerPlaceholder("$player$", event.getWhoClicked());
        action.registerPlaceholder("$player_name$", event.getWhoClicked().getName());
    }

    @Override
    public String getId() {
        return "click_storage_page".toUpperCase(Locale.ENGLISH);
    }

    public StorageInventory getStorageInventory() {
        return storageInventory;
    }
}
