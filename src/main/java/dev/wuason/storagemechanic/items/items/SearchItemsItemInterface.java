package dev.wuason.storagemechanic.items.items;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.inventory.inventories.SearchItem.SearchType;
import dev.wuason.storagemechanic.items.ItemInterface;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageManager;
import dev.wuason.storagemechanic.storages.config.StorageConfig;
import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class SearchItemsItemInterface extends ItemInterface {

    private String invId;
    private String invResultId;
    private SearchType searchType;

    public SearchItemsItemInterface(String item, String displayName, List<String> lore, String id, String invId, String invResultId, SearchType searchType) {
        super(item, displayName, lore, id, "SEARCH_ITEM");
        this.invId = invId;
        this.invResultId = invResultId;
        this.searchType = searchType;
    }

    @Override
    public void execute(Storage storage, StorageInventory storageInventory, InventoryClickEvent event, StorageConfig storageConfig, StorageManager storageManager) {
        Player player = (Player) event.getWhoClicked();
        StorageMechanic.getInstance().getManagers().getInventoryManager().getSearchItemInventoryManager().openSearchItemMenu(player, invId, invResultId, storage, searchType);
    }

    public String getInvId() {
        return invId;
    }

    public String getInvResultId() {
        return invResultId;
    }

    public SearchType getSearchType() {
        return searchType;
    }
}
