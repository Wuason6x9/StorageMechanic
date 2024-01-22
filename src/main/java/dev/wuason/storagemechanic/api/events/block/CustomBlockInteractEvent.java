package dev.wuason.storagemechanic.api.events.block;


import dev.wuason.storagemechanic.customitems.CustomItem;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerInteractEvent;

public class CustomBlockInteractEvent extends PlayerInteractEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private CustomItem customItem;
    private boolean cancelled;

    public CustomBlockInteractEvent(PlayerInteractEvent event, CustomItem customItem) {
        super(event.getPlayer(), event.getAction(), event.getItem(), event.getClickedBlock(), event.getBlockFace(), event.getHand());
        this.customItem = customItem;
    }

    public CustomItem getCustomBlock() {
        return customItem;
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