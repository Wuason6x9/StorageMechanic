package dev.wuason.storagemechanic.storages.types.block.compatibilities;

import dev.wuason.storagemechanic.storages.types.block.BlockStorageManager;
import net.momirealms.craftengine.bukkit.api.event.CustomBlockInteractEvent;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;

public class CraftEngineEvents implements Listener {
    private BlockStorageManager blockStorageManager;

    public CraftEngineEvents(BlockStorageManager blockStorageManager) {
        this.blockStorageManager = blockStorageManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteractCraftEngineBlock(CustomBlockInteractEvent event) {
        if (event.getPlayer().isSneaking()) return;

        if (event.hand() != InteractionHand.MAIN_HAND) return;
        String adapterId = "ce:" + event.customBlock().id().toString();
        blockStorageManager.onBlockInteract(event.bukkitBlock(), event.player().getInventory().getItemInMainHand(), event.getPlayer(), event, Action.RIGHT_CLICK_BLOCK, adapterId);
    }
}
