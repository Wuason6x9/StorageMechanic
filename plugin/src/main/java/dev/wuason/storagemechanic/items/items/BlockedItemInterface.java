package dev.wuason.storagemechanic.items.items;

import dev.wuason.storagemechanic.items.ItemInterface;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.config.StorageConfig;
import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class BlockedItemInterface extends ItemInterface {
    public BlockedItemInterface(String item, String displayName, List<String> lore, String id) {
        super(item, displayName, lore, id, "BLOCKED_ITEM");
    }

    @Override
    public void onClick(Storage storage, StorageInventory storageInventory, InventoryClickEvent event, StorageConfig storageConfig) {
    }
}
