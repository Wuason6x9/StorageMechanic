package dev.wuason.storagemechanic.items;

import dev.wuason.mechanics.items.ItemBuilderMechanic;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageManager;
import dev.wuason.storagemechanic.storages.config.StorageConfig;
import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class ItemInterface {
    private final ItemStack itemStack;
    private List<Object> data = new ArrayList<>();
    private final String id;
    private final String name;

    public ItemInterface(String item, String displayName, List<String> lore, String id, String name) {
        itemStack = new ItemBuilderMechanic(item,1).setNameWithMiniMessage(displayName).setLore(lore).addPersistentData(ItemInterfaceManager.NAMESPACED_KEY,id).build();
        this.id = id;
        this.name = name;
    }

    public ItemStack getItemStack() {
        return ItemBuilderMechanic.copyOf(itemStack).addPersistentData(ItemInterfaceManager.NAMESPACED_KEY, id).build();
    }

    public String getId() {
        return id;
    }

    public void setData(List<Object> data) {
        this.data = data;
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
