package dev.wuason.storagemechanic.storages.config;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.items.ItemInterface;

import java.util.*;

public class StorageItemInterfaceConfig {
    private String id;
    private Map<Integer, Set<Integer>> pagesToSlots;
    private String item;
    private ItemInterface itemInterface;

    public StorageItemInterfaceConfig(String id, List<Integer> slots, List<Integer> pages, String item) {
        this.id = id;
        this.item = item;
        this.pagesToSlots = new HashMap<>();
        HashSet<Integer> hashSet = new HashSet<>(slots);

        for(Integer i : pages){
            pagesToSlots.put(i,hashSet);
        }

        itemInterface = StorageMechanic.getInstance().getManagers().getItemInterfaceManager().getItemInterfaceById(item);
    }

    public String getId() {
        return id;
    }

    public Map<Integer, Set<Integer>> getPagesToSlots() {
        return pagesToSlots;
    }

    public String getItem() {
        return item;
    }

    public ItemInterface getItemInterface() {
        return itemInterface;
    }
}
