package dev.wuason.storagemechanic.storages.inventory;

import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.mechanics.utils.Utils;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.config.StorageConfig;
import dev.wuason.storagemechanic.storages.config.StorageInventoryTypeConfig;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StorageInventory implements InventoryHolder {

    private Inventory inventory;
    private String id;
    private Storage storage;
    private int page;
    private BukkitTask animationStagesTask;



    public StorageInventory(StorageConfig storageConfig, Storage storage, int page) {
        this.id = UUID.randomUUID().toString();
        this.page = page;
        this.storage = storage;
        Map<String, String> replacements = new HashMap<>(){{
            put("%MAX_PAGES%", "" + storageConfig.getPages());
            put("%ACTUAL_PAGE%", "" + (page + 1));
            put("%STORAGE_ID%", storage.getId());
        }};
        inventory = Bukkit.createInventory(this, InventoryType.valueOf(storageConfig.getInventoryType().toString()),AdventureUtils.deserializeLegacy(storageConfig.getTitle(),null));
        if(storageConfig.getInventoryType().equals(StorageInventoryTypeConfig.CHEST)){
            inventory = Bukkit.createInventory(this,(storageConfig.getRows() * 9), AdventureUtils.deserializeLegacy(Utils.replaceVariables(storageConfig.getTitle(),replacements),null));
        }

    }
    public StorageInventory(InventoryType inventoryType, String title, Storage storage, int page) {
        this.id = UUID.randomUUID().toString();
        this.page = page;
        this.storage = storage;
        Map<String, String> replacements = new HashMap<>(){{
            put("%MAX_PAGES%", "" + storage.getStorageConfig().getPages());
            put("%ACTUAL_PAGE%", "" + (page + 1));
            put("%STORAGE_ID%", storage.getId());
        }};
        inventory = Bukkit.createInventory(this, inventoryType,AdventureUtils.deserializeLegacy(Utils.replaceVariables(title,replacements),null));
    }

    public StorageInventory(int rows, String title, Storage storage, int page) {
        Map<String, String> replacements = new HashMap<>(){{
            put("%MAX_PAGES%", "" + storage.getStorageConfig().getPages());
            put("%ACTUAL_PAGE%", "" + (page + 1));
            put("%STORAGE_ID%", storage.getId());
        }};
        this.id = UUID.randomUUID().toString();
        this.page = page;
        this.storage = storage;
        inventory = Bukkit.createInventory(this,(rows * 9),AdventureUtils.deserializeLegacy(Utils.replaceVariables(title,replacements),null));
    }
    public void closeInventoryAll(){
        while (!inventory.getViewers().isEmpty()){
            inventory.getViewers().get(0).closeInventory();
        }
    }

    public void setTitleInventory(String title, @Nullable Player player){
        if(player != null){
            Mechanics.getInstance().getServerNmsVersion().getVersionWrapper().updateCurrentInventoryTitle(AdventureUtils.deserializeJson(title, player),player);
            return;
        }
        if(inventory != null){
            for(HumanEntity human : inventory.getViewers()){
                Mechanics.getInstance().getServerNmsVersion().getVersionWrapper().updateCurrentInventoryTitle(AdventureUtils.deserializeJson(title, (Player) human),(Player) human);
            }
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

    public BukkitTask getAnimationStagesTask() {
        return animationStagesTask;
    }

    public void setAnimationStagesTask(BukkitTask animationStagesTask) {
        this.animationStagesTask = animationStagesTask;
    }
}
