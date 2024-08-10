package dev.wuason.storagemechanic.storages.types.block.compatibilities;

import dev.lone.itemsadder.api.Events.CustomBlockPlaceEvent;
import dev.wuason.storagemechanic.storages.types.block.BlockStorageManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ItemsAdderEvents implements Listener {
    private BlockStorageManager blockStorageManager;

    public ItemsAdderEvents(BlockStorageManager blockStorageManager) {
        this.blockStorageManager = blockStorageManager;
    }


    @EventHandler
    public void onPlaceIaBlock(CustomBlockPlaceEvent event){

        blockStorageManager.onBlockPlace(event.getBlock(), event.getPlayer(),event.getItemInHand());

    }
}
