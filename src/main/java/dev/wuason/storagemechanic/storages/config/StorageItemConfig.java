package dev.wuason.storagemechanic.storages.config;

import java.util.*;

public class StorageItemConfig {
    private String id;
    private String amount;
    private Map<Integer, Set<Integer>> pagesToSlots;
    private Set<String> items;
    private float chance;

    public StorageItemConfig(String id, String amount, List<Integer> slots, List<Integer> pages, List<String> items, float chance) {
        this.id = id;
        this.amount = amount;
        this.items = new HashSet<>(items);
        this.chance = chance;

        this.pagesToSlots = new HashMap<>();
        HashSet<Integer> hashSet = new HashSet<>(slots);

        for(Integer i : pages){
            pagesToSlots.put(i,hashSet);
        }
    }

    public String getId() {
        return id;
    }

    public String getAmount() {
        return amount;
    }

    public Map<Integer, Set<Integer>> getPagesToSlots() {
        return pagesToSlots;
    }

    public Set<String> getItems() {
        return items;
    }

    public float getChance() {
        return chance;
    }
}
