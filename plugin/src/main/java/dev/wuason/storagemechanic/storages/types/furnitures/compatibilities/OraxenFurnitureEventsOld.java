package dev.wuason.storagemechanic.storages.types.furnitures.compatibilities;

import dev.wuason.storagemechanic.storages.types.furnitures.EventCancel;
import dev.wuason.storagemechanic.storages.types.furnitures.FurnitureStorageManager;
import io.th0rgal.oraxen.api.events.furniture.OraxenFurnitureBreakEvent;
import io.th0rgal.oraxen.api.events.furniture.OraxenFurnitureInteractEvent;
import io.th0rgal.oraxen.api.events.furniture.OraxenFurniturePlaceEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class OraxenFurnitureEventsOld implements Listener {

    private final String ADAPTER_TYPE = "or:";
    private FurnitureStorageManager furnitureStorageManager;

    public OraxenFurnitureEventsOld(FurnitureStorageManager furnitureStorageManager) {
        this.furnitureStorageManager = furnitureStorageManager;
    }

    @EventHandler
    public void onFurnitureInteract(OraxenFurnitureInteractEvent event){
        String adapterId = ADAPTER_TYPE + event.getMechanic().getItemID();
        EventCancel eventCancel = new EventCancel();
        furnitureStorageManager.onFurnitureInteract(adapterId, event.getPlayer(),event.getBaseEntity(),event.getItemInHand(),eventCancel);
        if(eventCancel.isCancelled()){
            event.setCancelled(true);
        }

    }
    @EventHandler
    public void onFurniturePlace(OraxenFurniturePlaceEvent event){
        String adapterId = ADAPTER_TYPE + event.getMechanic().getItemID();
        EventCancel eventCancel = new EventCancel();
        furnitureStorageManager.onFurniturePlace(adapterId, event.getPlayer(),event.getBaseEntity(),event.getItemInHand(),eventCancel);
        if(eventCancel.isCancelled()){
            event.setCancelled(true);
        }

    }
    @EventHandler
    public void onFurnitureBreak(OraxenFurnitureBreakEvent event){
        String adapterId = ADAPTER_TYPE + event.getMechanic().getItemID();
        EventCancel eventCancel = new EventCancel();
        furnitureStorageManager.onFurnitureBreak(adapterId, event.getPlayer(),event.getBaseEntity(),event.getPlayer().getInventory().getItemInMainHand(),eventCancel);
        if(eventCancel.isCancelled()){
            event.setCancelled(true);
        }

    }
}
