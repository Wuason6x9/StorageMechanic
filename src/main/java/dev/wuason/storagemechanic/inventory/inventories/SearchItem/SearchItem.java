package dev.wuason.storagemechanic.inventory.inventories.SearchItem;

import dev.wuason.storagemechanic.storages.Storage;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class SearchItem {
    private int slot;
    private int page;
    private ItemStack itemStack;
    private Storage storage;
    private String id;

    public SearchItem(int slot, int page, ItemStack itemStack, Storage storage) {
        this.id = UUID.randomUUID().toString();
        this.slot = slot;
        this.page = page;
        this.itemStack = itemStack;
        this.storage = storage;
    }

    public String getId() {
        return id;
    }

    public int getSlot() {
        return slot;
    }

    public int getPage() {
        return page;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public Storage getStorage() {
        return storage;
    }

    public boolean exist(){
        ItemStack[] content = storage.getItemsFromPageSlots(page);
        if(content[slot] == itemStack) {
            return true;
        }
        return false;
    }

    public void removeItemFromStorage(){
        storage.removeItemStackAtSlot(page,slot,itemStack);
    }
}


