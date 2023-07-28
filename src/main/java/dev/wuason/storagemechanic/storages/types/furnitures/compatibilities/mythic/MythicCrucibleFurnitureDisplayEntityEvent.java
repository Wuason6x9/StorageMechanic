package dev.wuason.storagemechanic.storages.types.furnitures.compatibilities.mythic;

import io.lumine.mythiccrucible.MythicCrucible;
import io.lumine.mythiccrucible.items.furniture.FurnitureDataKeys;
import org.bukkit.entity.Interaction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class MythicCrucibleFurnitureDisplayEntityEvent implements Listener {
    private MythicCrucibleFurnitureManager mcFurnitureManager;

    public MythicCrucibleFurnitureDisplayEntityEvent(MythicCrucibleFurnitureManager mcFurnitureManager) {
        this.mcFurnitureManager = mcFurnitureManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onFurnitureInteractHitbox(PlayerInteractEntityEvent event) {
        Interaction hitbox = (Interaction)event.getRightClicked();
        PersistentDataContainer data = hitbox.getPersistentDataContainer();
        if (data.has(FurnitureDataKeys.FURNITURE_BASE, PersistentDataType.STRING)) {
            MythicCrucible.inst().getItemManager().getFurnitureManager().getFurniture(hitbox).ifPresent((furniture) -> {
                mcFurnitureManager.onMythicFurnitureInteract(furniture.getFurnitureData(),event.getPlayer(),furniture.getEntity().getBukkitEntity(),event.getPlayer().getInventory().getItemInMainHand(),event);
            });
        }
    }

}
