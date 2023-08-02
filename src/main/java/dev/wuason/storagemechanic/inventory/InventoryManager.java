package dev.wuason.storagemechanic.inventory;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.inventory.config.InventoryConfig;
import dev.wuason.storagemechanic.inventory.config.InventoryConfigManager;
import dev.wuason.storagemechanic.inventory.config.InventoryMechanicType;
import org.bukkit.event.inventory.InventoryType;

import java.util.HashMap;
import java.util.UUID;

public class InventoryManager {
    private StorageMechanic core;
    private HashMap<String,InventoryMechanic> inventories = new HashMap<>();
    private HashMap<String,InventoryMechanicType> inventoriesType = new HashMap<>();

    public InventoryManager(StorageMechanic core) {
        this.core = core;
    }

    public InventoryMechanic createInventoryMechanic(String configId, InventoryMechanicType inventoryMechanicType){

        InventoryConfig inventoryConfig = core.getManagers().getInventoryConfigManager().getInventories().getOrDefault(configId,null);

        InventoryMechanic inventoryMechanic = null;
        if(inventoryConfig.getInventoryType().equals(InventoryType.CHEST)){
            inventoryMechanic = new InventoryMechanic(inventoryConfig.getRows(),inventoryConfig.getTitle());
        }
        if(inventoryMechanic == null){
            inventoryMechanic = new InventoryMechanic(inventoryConfig.getInventoryType(),inventoryConfig.getTitle());
        }


        return inventoryMechanic;
    }
}
