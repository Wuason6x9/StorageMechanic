package dev.wuason.storagemechanic.actions.events.def;

import dev.wuason.mechanics.actions.Action;
import dev.wuason.mechanics.actions.events.EventAction;
import dev.wuason.storagemechanic.items.ItemInterface;
import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Locale;

public class ClickItemInterfaceTypeActionEvent implements EventAction {
    private final StorageInventory storageInventory;
    private final InventoryClickEvent event;
    private final ItemInterface itemInterface;

    public ClickItemInterfaceTypeActionEvent(StorageInventory storageInventory, InventoryClickEvent event, ItemInterface itemInterface) {
        this.storageInventory = storageInventory;
        this.event = event;
        this.itemInterface = itemInterface;
    }

    @Override
    public void registerPlaceholders(Action action) {
        action.registerPlaceholder("$page$", storageInventory.getPage());
        action.registerPlaceholder("$storageInventory$", storageInventory);
        action.registerPlaceholder("$inventory$", storageInventory.getInventory());
        action.registerPlaceholder("$bukkitEvent$", event);
        action.registerPlaceholder("$player$", event.getWhoClicked());
        action.registerPlaceholder("$player_name$", event.getWhoClicked().getName());
        action.registerPlaceholder("$itemInterface$", itemInterface);
        action.registerPlaceholder("$itemInterface_id$", itemInterface.getId());
        action.registerPlaceholder("$itemInterface_itemStack$", itemInterface.getItemStack());
        action.registerPlaceholder("$itemInterface_name$", itemInterface.getName());
    }

    @Override
    public String getId() {
        return "click_item_interface_item".toUpperCase(Locale.ENGLISH);
    }

    public StorageInventory getStorageInventory() {
        return storageInventory;
    }

    public InventoryClickEvent getEvent() {
        return event;
    }

    public ItemInterface getItemInterface() {
        return itemInterface;
    }
}
