package dev.wuason.storagemechanic.storages.config;

import dev.wuason.mechanics.utils.Adapter;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class StorageItemConfig {
    private String id;
    private int amount;
    private ArrayList<Integer> slots = new ArrayList<>();
    private ArrayList<Integer> pages = new ArrayList<>();
    private List<String> items;


    public StorageItemConfig(String id, int amount, ArrayList<Integer> slots, ArrayList<Integer> pages, List<String> items) {
        this.id = id;
        this.amount = amount;
        this.slots = slots;
        this.pages = pages;
        this.items = items;

    }

    public String getId() {
        return id;
    }

    public int getAmount() {
        return amount;
    }

    public ArrayList<Integer> getSlots() {
        return slots;
    }

    public ArrayList<Integer> getPages() {
        return pages;
    }

    public List<String> getItems() {
        return items;
    }


}
