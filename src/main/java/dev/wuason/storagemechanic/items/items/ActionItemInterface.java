package dev.wuason.storagemechanic.items.items;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.items.ItemInterface;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageManager;
import dev.wuason.storagemechanic.storages.config.StorageConfig;
import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class ActionItemInterface extends ItemInterface {

    private final String actionId;
    private final StorageMechanic core;

    public ActionItemInterface(String item, String displayName, List<String> lore, String actionId, String id) {
        super(item, displayName, lore, id, "ACTION");
        this.actionId = actionId;
        this.core = StorageMechanic.getInstance();
    }

    @Override
    public void execute(Storage storage, StorageInventory storageInventory, InventoryClickEvent event, StorageConfig storageConfig, StorageManager storageManager) {
        Player player = (Player) event.getWhoClicked();
        //ClickStorageItemInterfaceAction eventAction = new ClickStorageItemInterfaceAction(event,this,storageInventory);
        //Bukkit.getScheduler().runTaskAsynchronously(core, () -> {
        //    Action action = core.getManagers().getActionManager().createAction(storage, actionId, player, eventAction);
        //    action.execute();
        //});
    }

    public String getActionId() {
        return actionId;
    }
}
