package dev.wuason.storagemechanic.storages.types.furnitures.compatibilities;

import com.nexomc.nexo.api.events.furniture.NexoFurnitureBreakEvent;
import com.nexomc.nexo.api.events.furniture.NexoFurnitureInteractEvent;
import com.nexomc.nexo.api.events.furniture.NexoFurniturePlaceEvent;
import dev.wuason.storagemechanic.storages.types.furnitures.EventCancel;
import dev.wuason.storagemechanic.storages.types.furnitures.FurnitureStorageManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


public class NexoFurnitureEvents implements Listener {

    private final String ADAPTER_TYPE = "nexo:";
    private FurnitureStorageManager furnitureStorageManager;

    public NexoFurnitureEvents(FurnitureStorageManager furnitureStorageManager) {
        this.furnitureStorageManager = furnitureStorageManager;
    }

    @EventHandler
    public void onFurnitureInteract(NexoFurnitureInteractEvent event) {
        String adapterId = ADAPTER_TYPE + event.getMechanic().getItemID();
        EventCancel eventCancel = new EventCancel();
        furnitureStorageManager.onFurnitureInteract(adapterId, event.getPlayer(), event.getBaseEntity(), event.getItemInHand(), eventCancel);
        if (eventCancel.isCancelled()) {
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onFurniturePlace(NexoFurniturePlaceEvent event) {
        String adapterId = ADAPTER_TYPE + event.getMechanic().getItemID();
        EventCancel eventCancel = new EventCancel();
        furnitureStorageManager.onFurniturePlace(adapterId, event.getPlayer(), event.getBaseEntity(), event.getItemInHand(), eventCancel);
        if (eventCancel.isCancelled()) {
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onFurnitureBreak(NexoFurnitureBreakEvent event) {
        String adapterId = ADAPTER_TYPE + event.getMechanic().getItemID();
        EventCancel eventCancel = new EventCancel();
        furnitureStorageManager.onFurnitureBreak(adapterId, event.getPlayer(), event.getBaseEntity(), event.getPlayer().getInventory().getItemInMainHand(), eventCancel);
        if (eventCancel.isCancelled()) {
            event.setCancelled(true);
        }

    }
}
