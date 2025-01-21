package dev.wuason.storagemechanic.storages.types.block.compatibilities;

import com.nexomc.nexo.api.events.custom_block.noteblock.NexoNoteBlockInteractEvent;
import com.nexomc.nexo.api.events.custom_block.stringblock.NexoStringBlockInteractEvent;
import dev.wuason.storagemechanic.storages.types.block.BlockStorageManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;

public class NexoEvents implements Listener {
    private BlockStorageManager blockStorageManager;

    public NexoEvents(BlockStorageManager blockStorageManager) {
        this.blockStorageManager = blockStorageManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteractNexoBlock(NexoNoteBlockInteractEvent event) {
        if (event.getPlayer().isSneaking()) return;
        if (event.getHand() == null || !event.getHand().equals(EquipmentSlot.HAND)) return;
        String adapterId = "nexo:" + event.getMechanic().getItemID();
        blockStorageManager.onBlockInteract(event.getBlock(), event.getItemInHand(), event.getPlayer(), event, Action.RIGHT_CLICK_BLOCK, adapterId);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteractNexoBlockString(NexoStringBlockInteractEvent event) {
        if (event.getPlayer().isSneaking()) return;
        if (event.getHand() == null || !event.getHand().equals(EquipmentSlot.HAND)) return;
        String adapterId = "nexo:" + event.getMechanic().getItemID();
        blockStorageManager.onBlockInteract(event.getBlock(), event.getItemInHand(), event.getPlayer(), event, Action.RIGHT_CLICK_BLOCK, adapterId);
    }
}
