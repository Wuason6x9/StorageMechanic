package dev.wuason.storagemechanic.storages;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class StorageItemDataInfo {
    private final ItemStack itemStack;
    private final int page;
    private final int slot;
    private final Storage storage;
    private final UUID id;

    public StorageItemDataInfo(ItemStack itemStack, int page, int slot, Storage storage) {
        this.itemStack = itemStack;
        this.page = page;
        this.slot = slot;
        this.storage = storage;
        this.id = UUID.randomUUID();
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

    public UUID getId() {
        return id;
    }
}
