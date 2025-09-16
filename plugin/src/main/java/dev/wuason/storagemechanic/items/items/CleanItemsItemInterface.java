package dev.wuason.storagemechanic.items.items;

import dev.wuason.storagemechanic.items.ItemInterface;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.config.StorageConfig;
import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public class CleanItemsItemInterface extends ItemInterface {

    private ArrayList<Integer> pages;
    private ArrayList<Integer> slots;

    public CleanItemsItemInterface(String item, String displayName, List<String> lore, ArrayList<Integer> pages, ArrayList<Integer> slots, String id) {
        super(item, displayName, lore, id, "CLEAN_ITEM");
        this.pages = pages;
        this.slots = slots;
    }

    @Override
    public void onClick(Storage storage, StorageInventory storageInventory, InventoryClickEvent event, StorageConfig storageConfig) {
        Player player = (Player) event.getWhoClicked();
        for (int page : pages) {
            for (int slot : slots) {
                storage.clearSlotWithRestrictions(page, slot);
            }
        }
    }

    public ArrayList<Integer> getPages() {
        return pages;
    }

    public ArrayList<Integer> getSlots() {
        return slots;
    }
}
