package dev.wuason.storagemechanic.storages.types.furnitures.compatibilities;


import dev.lone.itemsadder.api.CustomFurniture;
import dev.lone.itemsadder.api.Events.FurnitureBreakEvent;
import dev.lone.itemsadder.api.Events.FurnitureInteractEvent;
import dev.lone.itemsadder.api.Events.FurniturePlacedEvent;
import dev.wuason.storagemechanic.storages.types.furnitures.EventCancel;
import dev.wuason.storagemechanic.storages.types.furnitures.FurnitureStorageManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ItemsAdderFurnitureEvents implements Listener {

    private final String ADAPTER_TYPE = "ia:";
    private FurnitureStorageManager furnitureStorageManager;

    public ItemsAdderFurnitureEvents(FurnitureStorageManager furnitureStorageManager) {
        this.furnitureStorageManager = furnitureStorageManager;
    }

    @EventHandler
    public void onFurnitureInteract(FurnitureInteractEvent event) {
        if (event.getPlayer() == null) return;
        String adapterId = ADAPTER_TYPE + event.getNamespacedID();
        EventCancel eventCancel = new EventCancel();
        furnitureStorageManager.onFurnitureInteract(adapterId, event.getPlayer(), event.getBukkitEntity(), event.getPlayer().getInventory().getItemInMainHand(), eventCancel);
        if (eventCancel.isCancelled()) {
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onFurniturePlace(FurniturePlacedEvent event) {
        if (event.getPlayer() == null) return;
        String adapterId = ADAPTER_TYPE + event.getNamespacedID();
        EventCancel eventCancel = new EventCancel();
        furnitureStorageManager.onFurniturePlace(adapterId, event.getPlayer(), event.getBukkitEntity(), event.getPlayer().getInventory().getItemInMainHand(), eventCancel);
        if (eventCancel.isCancelled()) {
            CustomFurniture.remove(event.getBukkitEntity(), false);
        }
    }

    @EventHandler
    public void onFurnitureBreak(FurnitureBreakEvent event) {
        if (event.getPlayer() == null) return;
        String adapterId = ADAPTER_TYPE + event.getNamespacedID();
        EventCancel eventCancel = new EventCancel();
        furnitureStorageManager.onFurnitureBreak(adapterId, event.getPlayer(), event.getBukkitEntity(), event.getPlayer().getInventory().getItemInMainHand(), eventCancel);
        if (eventCancel.isCancelled()) {
            event.setCancelled(true);
        }
    }

}
