package dev.wuason.storagemechanic.storages.config;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.items.ItemInterface;

public class StorageItemInterfaceConfig {
    private String id;
    private String item;
    private ItemInterface itemInterface;

    public StorageItemInterfaceConfig(String id, String item) {
        this.id = id;
        this.item = item;
        itemInterface = StorageMechanic.getInstance().getManagers().getItemInterfaceManager().getItemInterfaceById(item);
    }

    public String getId() {
        return id;
    }

    public String getItem() {
        return item;
    }

    public ItemInterface getItemInterface() {
        return itemInterface;
    }
}
