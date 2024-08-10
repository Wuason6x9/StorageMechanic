package dev.wuason.storagemechanic.storages.config;

import java.util.*;

public class StorageBlockItemConfig {
    private String id;
    private Map<Integer, Set<Integer>> pagesToSlots;
    private String message;

    public StorageBlockItemConfig(String id, List<Integer> slots, List<Integer> pages, String message) {
        this.id = id;
        this.message = message;

        this.pagesToSlots = new HashMap<>();
        HashSet<Integer> hashSet = new HashSet<>(slots);

        for(Integer i : pages){
            pagesToSlots.put(i,hashSet);
        }
    }

    public String getId() {
        return id;
    }

    public Map<Integer, Set<Integer>> getPagesToSlots() {
        return pagesToSlots;
    }

    public String getMessage() {
        return message;
    }
}
