package dev.wuason.storagemechanic.storages;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.storages.config.StorageConfig;
import dev.wuason.storagemechanic.storages.config.StorageInventoryTypeConfig;
import dev.wuason.storagemechanic.storages.config.StorageItemInterfaceConfig;
import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Storage implements Serializable {
    private Map<Integer, StorageInventory> inventories = new HashMap<>();
    private Map<Integer,ItemStack[]> items = new HashMap<>();

    private String id;
    private String storageIdConfig;

    public Storage() {
        this.id = UUID.randomUUID().toString();
    }

    public Storage(String id, Map<Integer,ItemStack[]> items, String storageIdConfig) {
        this.id = id;
        this.items = items;
        this.storageIdConfig = storageIdConfig;
    }

    public void openStorage(Player player, int page) {

        StorageConfig storageConfig = StorageMechanic.getInstance().getManagers().getStorageConfigManager().getStorageConfigById(storageIdConfig);

        if (!items.containsKey(page)) {
            int slots = storageConfig.getInventoryType().getSize();
            if(storageConfig.getInventoryType().equals(StorageInventoryTypeConfig.CHEST)){
                slots = storageConfig.getRows() * 9;
            }
            items.put(page, new ItemStack[slots]); //SLOTS
        }

        if (!inventories.containsKey(page)) {
            StorageInventory inventory = StorageMechanic.getInstance().getManagers().getStorageInventoryManager().createStorageInventory(storageConfig);
            inventory.getInventory().setContents(items.get(page));

            if(storageConfig.isStorageItemsInterfaceEnabled()){
                for(StorageItemInterfaceConfig itemInterface : storageConfig.getStorageItemsInterfaceConfig()){

                    if(itemInterface.getPages().indexOf(page) != (-1)){

                        for(int s : itemInterface.getSlots()){

                            inventory.getInventory().setItem(s,itemInterface.getitemInterface().getItemStack());

                        }

                    }

                }
            }

            inventories.put(page, inventory);
        }
        inventories.get(page).open(player);
    }


    public Map<Integer, StorageInventory> getInventories() {
        return inventories;
    }

    public Map<Integer, ItemStack[]> getItems() {
        return items;
    }

    public String getId() {
        return id;
    }

    public String getStorageIdConfig() {
        return storageIdConfig;
    }
}
