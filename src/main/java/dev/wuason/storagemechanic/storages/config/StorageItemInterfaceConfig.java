package dev.wuason.storagemechanic.storages.config;

import dev.wuason.mechanics.utils.Adapter;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.items.ItemInterface;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class StorageItemInterfaceConfig {
    private String id;
    private ArrayList<Integer> slots = new ArrayList<>();
    private ArrayList<Integer> pages = new ArrayList<>();
    private String item;
    private ItemInterface itemInterface;

    public StorageItemInterfaceConfig(String id, ArrayList<Integer> slots, ArrayList<Integer> pages, String item) {
        this.id = id;
        this.slots = slots;
        this.pages = pages;
        this.item = item;
        itemInterface = StorageMechanic.getInstance().getManagers().getItemInterfaceManager().getItemInterfaceById(item);
    }

    public String getId() {
        return id;
    }

    public ArrayList<Integer> getSlots() {
        return slots;
    }

    public ArrayList<Integer> getPages() {
        return pages;
    }

    public String getItem() {
        return item;
    }

    public ItemInterface getitemInterface() {
        return itemInterface;
    }
}
