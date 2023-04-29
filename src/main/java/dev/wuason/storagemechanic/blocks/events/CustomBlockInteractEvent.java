package dev.wuason.storagemechanic.blocks.events;


import dev.wuason.storagemechanic.blocks.CustomBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerInteractEvent;

public class CustomBlockInteractEvent extends PlayerInteractEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private CustomBlock customBlock;
    private boolean cancelled;

    public CustomBlockInteractEvent(PlayerInteractEvent event, CustomBlock customBlock) {
        super(event.getPlayer(), event.getAction(), event.getItem(), event.getClickedBlock(), event.getBlockFace(), event.getHand());
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