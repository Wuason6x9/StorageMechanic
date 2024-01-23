package dev.wuason.storagemechanic.inventory.config;

import org.bukkit.event.inventory.InventoryType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class InventoryConfig {
    private InventoryType inventoryType;
    private int rows;
    private String title;
    private Set<Integer> blockedSlots = new HashSet<>();
    private Set<Integer> dataSlots = new HashSet<>();
    private HashMap<Integer,ItemInventoryConfig> itemsInventory = new HashMap<>();
    private String id;

    public InventoryConfig(InventoryType inventoryType, int rows, String title, Set<Integer> blockedSlots, Set<Integer> dataSlots, HashMap<Integer, ItemInventoryConfig> itemsInventory, String id) {
        this.inventoryType = inventoryType;
        this.rows = rows;
        this.title = title;
        this.blockedSlots = blockedSlots;
        this.dataSlots = dataSlots;
        this.itemsInventory = itemsInventory;
        this.id = id;
    }

    public HashMap<Integer, ItemInventoryConfig> getItemsInventory() {
        return itemsInventory;
    }

    public InventoryType getInventoryType() {
        return inventoryType;
    }

    public int getRows() {
        return rows;
    }

    public String getTitle() {
        return title;
    }

    public Set<Integer> getBlockedSlots() {
        return blockedSlots;
    }

    public Set<Integer> getDataSlots() {
        return dataSlots;
    }

    public String getId() {
        return id;
    }
}
