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

    public void removeWithRestrictions(){
        storage.clearSlotWithRestrictions(page,slot);
    }

    public void remove(){
        storage.clearSlotPage(page,slot);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public boolean isSimilar(ItemStack itemStack){
        return this.itemStack.isSimilar(itemStack);
    }

    public boolean exists(){
        return storage.getItem(page, slot) == itemStack;
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
