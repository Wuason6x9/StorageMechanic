package dev.wuason.storagemechanic.customblocks.events;


import dev.wuason.storagemechanic.customblocks.CustomBlock;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;

public class CustomBlockDestroyEvent extends BlockBreakEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private CustomBlock customBlock;
    private boolean cancelled;

    public CustomBlockDestroyEvent(BlockBreakEvent event, CustomBlock customBlock) {
        super(event.getBlock(), event.getPlayer());
        this.customBlock = customBlock;
    }

    public CustomBlock getCustomBlock() {
        return customBlock;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}