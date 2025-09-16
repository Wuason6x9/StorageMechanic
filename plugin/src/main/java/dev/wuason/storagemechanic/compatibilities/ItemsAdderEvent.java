package dev.wuason.storagemechanic.compatibilities;

import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import dev.wuason.storagemechanic.StorageMechanic;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.IOException;

public class ItemsAdderEvent implements Listener {
    @EventHandler
    public void loadEvent(ItemsAdderLoadDataEvent itemsAdderLoadDataEvent) {
        if (itemsAdderLoadDataEvent.getCause().equals(ItemsAdderLoadDataEvent.Cause.FIRST_LOAD)) {
            try {
                StorageMechanic.getInstance().getManagers().getConfigManager().loadConfig();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
