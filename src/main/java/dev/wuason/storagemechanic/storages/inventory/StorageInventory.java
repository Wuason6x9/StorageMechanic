package dev.wuason.storagemechanic.storages.inventory;

import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.config.StorageConfig;
import dev.wuason.storagemechanic.storages.config.StorageInventoryTypeConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class StorageInventory implements InventoryHolder {

    private Inventory inventory;
    private String id;
    private Storage storage;

    private int page;


    public StorageInventory(StorageConfig storageConfig, Storage storage, int page) {
        this.id = UUID.randomUUID().toString();
        this.page = page;
        this.storage = storage;
        inventory = Bukkit.createInventory(this, InventoryType.valueOf(storageConfig.getInventoryType().toString()),storageConfig.getTitle());
        if(storageConfig.getInventoryType().equals(StorageInventoryTypeConfig.CHEST)){
            inventory = Bukkit.createInventory(this,(storageConfig.getRows() * 9), AdventureUtils.deserializeLegacy(storageConfig.getTitle().replace("%MAX_PAGES%","" + storageConfig.getPages()).replace("%ACTUAL_PAGE%","" + (page + 1)).replace("%STORAGE_ID%",storage.getId())));
        }

    }
    public StorageInventory(InventoryType inventoryType, String title, Storage storage, int page) {
        this.id = UUID.randomUUID().toString();
        this.page = page;
        this.storage = storage;
        inventory = Bukkit.createInventory(this, inventoryType,AdventureUtils.deserializeLegacy(title.replace("%MAX_PAGES%","" + StorageMechanic.getInstance().getManagers().getStorageConfigManager().getStorageConfigById(storage.getStorageIdConfig()).getPages()).replace("%ACTUAL_PAGE%","" + (page + 1)).replace("%STORAGE_ID%",storage.getId())));
    }

    public StorageInventory(int rows, String title, Storage storage, int page) {
        this.id = UUID.randomUUID().toString();
        this.page = page;
        this.storage = storage;
        inventory = Bukkit.createInventory(this,(rows * 9),AdventureUtils.deserializeLegacy(title.replace("%MAX_PAGES%","" + StorageMechanic.getInstance().getManagers().getStorageConfigManager().getStorageConfigById(storage.getStorageIdConfig()).getPages()).replace("%ACTUAL_PAGE%","" + (page + 1)).replace("%STORAGE_ID%",storage.getId())));
    }
    public void closeInventoryAll(){
        while (!inventory.getViewers().isEmpty()){
            inventory.getViewers().get(0).closeInventory();
        }
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

    public Storage getStorage() {
        return storage;
    }

    public int getPage() {
        return page;
    }
}
