package dev.wuason.storagemechanic.items.items;

import dev.wuason.storagemechanic.items.ItemInterface;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageManager;
import dev.wuason.storagemechanic.storages.config.StorageConfig;
import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class BackPageItemInterface extends ItemInterface {


    public BackPageItemInterface(String item, String displayName, List<String> lore, String id) {
        super(item, displayName, lore, id, "BACK_PAGE");
    }

    @Override
    public void onClick(Storage storage, StorageInventory storageInventory, InventoryClickEvent event, StorageConfig storageConfig, StorageManager storageManager) {
        if(storageInventory.getPage() > 0){
            storage.openStorageR((Player) event.getWhoClicked(),storageInventory.getPage() - 1);
        }
    }
}
