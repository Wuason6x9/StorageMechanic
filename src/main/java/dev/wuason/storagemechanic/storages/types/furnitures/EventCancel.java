package dev.wuason.storagemechanic.storages.types.furnitures;

import org.bukkit.event.Cancellable;

public class EventCancel implements Cancellable {

    private boolean cancelled = false;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = true;
    }
}
