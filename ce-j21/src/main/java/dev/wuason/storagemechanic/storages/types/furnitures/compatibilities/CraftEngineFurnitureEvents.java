package dev.wuason.storagemechanic.storages.types.furnitures.compatibilities;

import dev.wuason.storagemechanic.storages.types.furnitures.EventCancel;
import dev.wuason.storagemechanic.storages.types.furnitures.FurnitureStorageManager;
import net.momirealms.craftengine.bukkit.api.event.FurnitureBreakEvent;
import net.momirealms.craftengine.bukkit.api.event.FurnitureInteractEvent;
import net.momirealms.craftengine.bukkit.api.event.FurniturePlaceEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


public class CraftEngineFurnitureEvents implements Listener {

    private final String ADAPTER_TYPE = "ce:";
    private FurnitureStorageManager furnitureStorageManager;

    public CraftEngineFurnitureEvents(FurnitureStorageManager furnitureStorageManager) {
        this.furnitureStorageManager = furnitureStorageManager;
    }

    @EventHandler
    public void onFurnitureInteract(FurnitureInteractEvent event) {
        String adapterId = ADAPTER_TYPE + event.furniture().id().toString();
        EventCancel eventCancel = new EventCancel();
        furnitureStorageManager.onFurnitureInteract(adapterId, event.getPlayer(), event.furniture().baseEntity(), event.player().getInventory().getItemInMainHand(), eventCancel);
        if (eventCancel.isCancelled()) {
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onFurniturePlace(FurniturePlaceEvent event) {
        String adapterId = ADAPTER_TYPE + event.furniture().id().toString();
        EventCancel eventCancel = new EventCancel();
        furnitureStorageManager.onFurniturePlace(adapterId, event.getPlayer(), event.furniture().baseEntity(), event.player().getInventory().getItemInMainHand(), eventCancel);
        if (eventCancel.isCancelled()) {
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onFurnitureBreak(FurnitureBreakEvent event) {
        String adapterId = ADAPTER_TYPE + event.furniture().id().toString();
        EventCancel eventCancel = new EventCancel();
        furnitureStorageManager.onFurnitureBreak(adapterId, event.getPlayer(), event.furniture().baseEntity(), event.player().getInventory().getItemInMainHand(), eventCancel);
        if (eventCancel.isCancelled()) {
            event.setCancelled(true);
        }

    }
}
