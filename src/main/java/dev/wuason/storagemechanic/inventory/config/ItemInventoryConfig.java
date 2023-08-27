package dev.wuason.storagemechanic.inventory.config;

import java.util.List;

public class ItemInventoryConfig {
    private String id;
    private String item;
    private int amount;
    private String display;
    private String type;
    private List<String> lore;
    private int slot = -1;

    public ItemInventoryConfig(String id, String item, int amount, String display, List<String> lore, String type) {
        this.id = id;
        this.item = item;
        this.amount = amount;
        this.display = display;
        this.lore = lore;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public List<String> getLore() {
        return lore;
    }

    public String getItem() {
        return item;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public int getAmount() {
        return amount;
    }

    public String getDisplayName() {
        return display;
    }

}
