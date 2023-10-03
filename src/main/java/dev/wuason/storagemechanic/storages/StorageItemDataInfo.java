package dev.wuason.storagemechanic.storages;

import org.bukkit.inventory.ItemStack;

public class StorageItemDataInfo {
    private ItemStack itemStack;
    private int page;
    private int slot;
    private Storage storage;

    public StorageItemDataInfo(ItemStack itemStack, int page, int slot, Storage storage) {
        this.itemStack = itemStack;
        this.page = page;
        this.slot = slot;
        this.storage = storage;
    }

    public void remove(){
        storage.clearSlotWithRestrictions(page,slot);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getPage() {
        return page;
    }

    public int getSlot() {
        return slot;
    }

    public Storage getStorage() {
        return storage;
    }
}
