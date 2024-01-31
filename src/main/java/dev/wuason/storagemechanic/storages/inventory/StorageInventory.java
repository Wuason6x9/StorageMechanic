package dev.wuason.storagemechanic.storages.inventory;

import dev.wuason.libs.invmechaniclib.events.CloseEvent;
import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.mechanics.utils.Utils;
import dev.wuason.nms.wrappers.NMSManager;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.items.items.PlaceholderItemInterface;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.config.StageStorage;
import dev.wuason.storagemechanic.storages.config.StorageConfig;
import dev.wuason.storagemechanic.storages.config.StorageInventoryTypeConfig;
import dev.wuason.storagemechanic.storages.config.StorageItemInterfaceConfig;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StorageInventory implements InventoryHolder {

    private Inventory inventory;
    private String id;
    private Storage storage;
    private int page;
    private BukkitTask animationStagesTask;
    private StageStorage currentStage;


    public StorageInventory(StorageConfig storageConfig, Storage storage, int page) {
        this.id = UUID.randomUUID().toString();
        this.page = page;
        this.storage = storage;
        Map<String, String> replacements = new HashMap<>() {{
            put("%MAX_PAGES%", "" + storageConfig.getPages());
            put("%ACTUAL_PAGE%", "" + (page + 1));
            put("%STORAGE_ID%", storage.getId());
        }};
        inventory = Bukkit.createInventory(this, InventoryType.valueOf(storageConfig.getInventoryType().toString()), AdventureUtils.deserializeLegacy(storageConfig.getTitle(), null));
        if (storageConfig.getInventoryType().equals(StorageInventoryTypeConfig.CHEST)) {
            inventory = Bukkit.createInventory(this, (storageConfig.getRows() * 9), AdventureUtils.deserializeLegacy(Utils.replaceVariables(storageConfig.getTitle(), replacements), null));
        }

    }

    public StorageInventory(InventoryType inventoryType, String title, Storage storage, int page) {
        this.id = UUID.randomUUID().toString();
        this.page = page;
        this.storage = storage;
        Map<String, String> replacements = new HashMap<>() {{
            put("%MAX_PAGES%", "" + storage.getStorageConfig().getPages());
            put("%ACTUAL_PAGE%", "" + (page + 1));
            put("%STORAGE_ID%", storage.getId());
        }};
        inventory = Bukkit.createInventory(this, inventoryType, AdventureUtils.deserializeLegacy(Utils.replaceVariables(title, replacements), null));
    }

    public StorageInventory(int rows, String title, Storage storage, int page) {
        Map<String, String> replacements = new HashMap<>() {{
            put("%MAX_PAGES%", "" + storage.getStorageConfig().getPages());
            put("%ACTUAL_PAGE%", "" + (page + 1));
            put("%STORAGE_ID%", storage.getId());
        }};
        this.id = UUID.randomUUID().toString();
        this.page = page;
        this.storage = storage;
        inventory = Bukkit.createInventory(this, (rows * 9), AdventureUtils.deserializeLegacy(Utils.replaceVariables(title, replacements), null));
    }

    public void closeInventoryAll() {
        while (!inventory.getViewers().isEmpty()) {
            inventory.getViewers().get(0).closeInventory();
        }
    }

    public void setTitleInventory(String title, @Nullable Player player) {
        Map<String, String> replacements = new HashMap<>() {{
            put("%MAX_PAGES%", "" + storage.getStorageConfig().getPages());
            put("%ACTUAL_PAGE%", "" + (page + 1));
            put("%STORAGE_ID%", storage.getId());
        }};
        if (player != null) {
            NMSManager.getVersionWrapper().updateCurrentInventoryTitle(AdventureUtils.deserializeJson(Utils.replaceVariables(title, replacements), player), player);
            return;
        }
        if (inventory != null) {
            for (HumanEntity human : inventory.getViewers()) {
                NMSManager.getVersionWrapper().updateCurrentInventoryTitle(AdventureUtils.deserializeJson(Utils.replaceVariables(title, replacements), (Player) human), (Player) human);
            }
        }
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public InventoryHolder getInventoryHolder() {
        return this;
    }

    public void open(Player player) {
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

    //*********************************
    //***** ANIMATION STAGES TASK *****
    //*********************************

    public BukkitTask getAnimationStagesTask() {
        return animationStagesTask;
    }

    public void setAnimationStagesTask(BukkitTask animationStagesTask) {
        this.animationStagesTask = animationStagesTask;
    }

    public void stopAnimationStages() {
        if (animationStagesTask == null || animationStagesTask.isCancelled()) return;
        animationStagesTask.cancel();
        animationStagesTask = null;
    }

    public void startAnimationStages() {

        StorageConfig storageConfig = storage.getStorageConfig();
        ArrayList<StageStorage> stages = storageConfig.getStagesOrder();
        if (storageConfig.getRefreshTimeStages() < 1 || stages.isEmpty()) return;

        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(StorageMechanic.getInstance(), () -> {

            if (currentStage != null && stages.indexOf(currentStage)) {
            } else {
                currentStage = stages.get(0);
            }

            setStage(currentStage);

            StageStorage stage = null;
            if (currentStages.containsKey(page)) {

                stage = currentStages.get(page);
                if (stages.indexOf(stage) + 1 < stages.size()) stage = stages.get(stages.indexOf(stage) + 1);
                else {
                    stage = stages.get(0);
                }
            }
            if (!currentStages.containsKey(page)) stage = storageConfig.getStagesOrder().get(0);
            setStage(stage, page);

        }, 0, storageConfig.getRefreshTimeStages());


        setAnimationStagesTask(bukkitTask);
    }

    //*********************************
    //***** ANIMATION STAGES TASK *****
    //*********************************

    /**
     * Sets the stage of the storage to the specified stage ID and page number.
     *
     * @param stageId The ID of the stage to set.
     */
    public void setStage(String stageId) {
        StorageConfig storageConfig = storage.getStorageConfig();
        if (!storageConfig.getStagesHashMap().containsKey(stageId)) return;
        StageStorage stage = storageConfig.getStagesHashMap().get(stageId);
        setStage(stage);
    }

    /**
     * Sets the stage of the storage on the specified page.
     *
     * @param stage The stage to set for the storage.
     */
    public void setStage(StageStorage stage) {
        if (stage.getTitle() != null) setTitleInventory(stage.getTitle(), null);
        if (stage.getStorageItemsInterfaceConfig().containsKey(page)) {
            storage.removeItemsInterface(page);
            for (Map.Entry<Integer, StorageItemInterfaceConfig> entry : stage.getStorageItemsInterfaceConfig().get(page).entrySet()) {
                if (PlaceholderItemInterface.isPlaceholderItem(inventory.getItem(entry.getKey()))) continue;
                inventory.setItem(entry.getKey(), entry.getValue().getItemInterface().getItemStack());
            }
        }
    }

    public void onClose(InventoryCloseEvent event) {

    }

    public void onOpen(InventoryOpenEvent event) {

    }

    public void onClick(InventoryClickEvent event) {
    }

    public void onDrag(InventoryDragEvent event) {
    }


}
