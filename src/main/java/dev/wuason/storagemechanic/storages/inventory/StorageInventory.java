package dev.wuason.storagemechanic.storages.inventory;

import dev.wuason.storagemechanic.storages.config.StorageConfig;
import dev.wuason.storagemechanic.storages.config.StorageInventoryTypeConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class StorageInventory implements InventoryHolder {

    private Inventory inventory;
    private String id;

    public StorageInventory(StorageConfig storageConfig) {
        this.id = UUID.randomUUID().toString();
        if(storageConfig.getInventoryType().equals(StorageInventoryTypeConfig.CHEST)){
            inventory = Bukkit.createInventory(this,(storageConfig.getRows() * 9),storageConfig.getTitle());
        }
        inventory = Bukkit.createInventory(this, InventoryType.valueOf(storageConfig.getInventoryType().toString()),storageConfig.getTitle());
    }
    public StorageInventory(InventoryType inventoryType, String title) {
        this.id = UUID.randomUUID().toString();
        inventory = Bukkit.createInventory(this, inventoryType,title);
    }

    public StorageInventory(int rows, String title) {
        this.id = UUID.randomUUID().toString();
        inventory = Bukkit.createInventory(this,(rows * 9),title);
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public InventoryHolder getInventoryHolder(){
        return this;
    }

    public void open(Player player){
        player.openInventory(inventory);
    }

    public String getId() {
        return id;
    }
}
