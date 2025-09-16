package dev.wuason.storagemechanic.storages.types.block.compatibilities;

import dev.lone.itemsadder.api.Events.CustomBlockBreakEvent;
import dev.lone.itemsadder.api.Events.CustomBlockPlaceEvent;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.storages.types.block.BlockStorageManager;
import dev.wuason.storagemechanic.storages.types.block.config.BlockStorageConfig;
import dev.wuason.storagemechanic.storages.types.block.config.BlockStorageType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ItemsAdderEvents implements Listener {
    private BlockStorageManager blockStorageManager;
    private final StorageMechanic core;

    public ItemsAdderEvents(BlockStorageManager blockStorageManager, StorageMechanic core) {
        this.blockStorageManager = blockStorageManager;
        this.core = core;
    }


    @EventHandler
    public void onPlaceIaBlock(CustomBlockPlaceEvent event) {

        blockStorageManager.onBlockPlace(event.getBlock(), event.getPlayer(), event.getItemInHand());

    }

    @EventHandler
    public void onBlockBreak(CustomBlockBreakEvent event) {
        String adapterId = "ia:" + event.getNamespacedID();
        if (!blockStorageManager.isBlockStorageByBlock(event.getBlock())) {
            BlockStorageConfig blockStorageConfig = core.getManagers().getBlockStorageConfigManager().findBlockStorageConfigByItemID(adapterId);
            if (blockStorageConfig != null && blockStorageConfig.getBlockStorageType() == BlockStorageType.SHULKER) {
                event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), event.getCustomBlockItem());
            }
        }
    }
}
