package dev.wuason.storagemechanic.storages;

import dev.wuason.storagemechanic.items.items.PlaceholderItemInterface;
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

    public ItemStack getCopyItemStack() {
        return itemStack.clone();
    }

    public boolean isSimilar(ItemStack itemStack){
        return this.itemStack.isSimilar(itemStack);
    }

    public boolean exists(){
        if(storage.getItem(slot,page) == null) return false;
        if(storage.getItem(slot, page) == itemStack) return true;
        if(PlaceholderItemInterface.isPlaceholderItem(storage.getItem(slot,page))){
            return itemStack.equals(PlaceholderItemInterface.getOriginalItemStack(storage.getItem(slot,page)));
        }
        return false;
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
