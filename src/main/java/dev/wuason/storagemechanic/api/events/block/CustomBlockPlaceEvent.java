package dev.wuason.storagemechanic.api.events.block;

import dev.wuason.storagemechanic.customitems.CustomItem;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockPlaceEvent;

public class CustomBlockPlaceEvent extends BlockPlaceEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private CustomItem customItem;
    private boolean cancelled;

    public CustomBlockPlaceEvent(BlockPlaceEvent event, CustomItem customItem) {
        super(event.getBlockPlaced(), event.getBlockReplacedState(), event.getBlockAgainst(), event.getItemInHand(), event.getPlayer(), event.canBuild());
        this.customItem = customItem;
    }

    public CustomItem getCustomBlock() {
        return customItem;
    }

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
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