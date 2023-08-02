package dev.wuason.storagemechanic.inventory.config;

import java.util.List;

public class ItemInventoryConfig {
    private String id;
    private String item;
    private int amount;
    private String display;
    private List<String> lore;

    public ItemInventoryConfig(String id, String item, int amount, String display, List<String> lore) {
        this.id = id;
        this.item = item;
        this.amount = amount;
        this.display = display;
        this.lore = lore;
    }

    public String getId() {
        return id;
    }

    public String getItem() {
        return item;
    }

    public int getAmount() {
        return amount;
    }

    public String getDisplay() {
        return display;
    }

    public List<String> getList() {
        return lore;
    }

}
