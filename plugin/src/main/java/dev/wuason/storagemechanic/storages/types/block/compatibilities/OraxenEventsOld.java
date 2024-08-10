package dev.wuason.storagemechanic.storages.types.block.compatibilities;

import dev.wuason.storagemechanic.storages.types.block.BlockStorageManager;
import io.th0rgal.oraxen.api.events.noteblock.OraxenNoteBlockInteractEvent;
import io.th0rgal.oraxen.api.events.stringblock.OraxenStringBlockInteractEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;

public class OraxenEventsOld implements Listener {
    private BlockStorageManager blockStorageManager;

    public OraxenEventsOld(BlockStorageManager blockStorageManager) {
        this.blockStorageManager = blockStorageManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteractOraxenBlock(OraxenNoteBlockInteractEvent event) {
        if (event.getPlayer().isSneaking()) return;
        if (event.getHand() == null || !event.getHand().equals(EquipmentSlot.HAND)) return;
        String adapterId = "or:" + event.getMechanic().getItemID();
        blockStorageManager.onBlockInteract(event.getBlock(), event.getItemInHand(), event.getPlayer(), event, Action.RIGHT_CLICK_BLOCK, adapterId);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteractOraxenBlockString(OraxenStringBlockInteractEvent event) {
        if (event.getPlayer().isSneaking()) return;
        if (event.getHand() == null || !event.getHand().equals(EquipmentSlot.HAND)) return;
        String adapterId = "or:" + event.getMechanic().getItemID();
        blockStorageManager.onBlockInteract(event.getBlock(), event.getItemInHand(), event.getPlayer(), event, Action.RIGHT_CLICK_BLOCK, adapterId);
    }
}
