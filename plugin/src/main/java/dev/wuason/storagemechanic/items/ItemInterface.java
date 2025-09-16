package dev.wuason.storagemechanic.items;

import dev.wuason.mechanics.items.ItemBuilder;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.config.StorageConfig;
import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class ItemInterface {
    private final List<Object> data = new ArrayList<>();
    private final String id;
    private final String name;
    private final String displayName;
    private final List<String> lore;
    private final String item;

    public ItemInterface(String item, String displayName, List<String> lore, String id, String name) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.lore = lore;
        this.item = item;
    }

    public ItemStack getItemStack() {
        return new ItemBuilder(item, 1).setNameWithMiniMessage(displayName).setLore(lore).addPersistentData(ItemInterfaceManager.NAMESPACED_KEY, id).build();
    }

    public String getId() {
        return id;
    }

    public void setData(List<Object> data) {
        this.data.clear();
        this.data.addAll(data);
    }

    public List<Object> getData() {
        return data;
    }

    public String getName() {
        return name;
    }

    public void addData(Object data) {
        this.data.add(data);
    }

    public void removeData(Object data) {
        this.data.remove(data);
    }

    public abstract void onClick(Storage storage, StorageInventory storageInventory, InventoryClickEvent event, StorageConfig storageConfig);
}
