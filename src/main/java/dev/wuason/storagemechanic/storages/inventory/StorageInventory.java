package dev.wuason.storagemechanic.storages.inventory;

import dev.wuason.libs.invmechaniclib.events.CloseEvent;
import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.mechanics.utils.Utils;
import dev.wuason.nms.wrappers.NMSManager;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.actions.events.def.CloseStoragePageActionEvent;
import dev.wuason.storagemechanic.actions.events.def.OpenStoragePageActionEvent;
import dev.wuason.storagemechanic.items.items.PlaceholderItemInterface;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageOriginContext;
import dev.wuason.storagemechanic.storages.config.*;
import dev.wuason.storagemechanic.storages.types.block.mechanics.BlockMechanicManager;
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

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class StorageInventory implements InventoryHolder {

    private final Inventory inventory;
    private final String id;
    private final Storage storage;
    private int page;
    private BukkitTask animationStagesTask;
    private StageStorage currentStage;
    private AtomicBoolean closed = new AtomicBoolean(false);


    public StorageInventory(StorageConfig storageConfig, Storage storage, int page) {
        this.id = UUID.randomUUID().toString();
        this.page = page;
        this.storage = storage;
        Map<String, String> replacements = new HashMap<>() {{
            put("%MAX_PAGES%", "" + storageConfig.getPages());
            put("%ACTUAL_PAGE%", "" + (page + 1));
            put("%STORAGE_ID%", storage.getId());
        }};
        if (storageConfig.getInventoryType().equals(StorageInventoryTypeConfig.CHEST)) {
            inventory = Bukkit.createInventory(this, (storageConfig.getRows() * 9), AdventureUtils.deserializeLegacy(Utils.replaceVariables(storageConfig.getTitle(), replacements), null));
            return;
        }
        inventory = Bukkit.createInventory(this, InventoryType.valueOf(storageConfig.getInventoryType().toString()), AdventureUtils.deserializeLegacy(storageConfig.getTitle(), null));
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

    //*********************************
    //***** INVENTORY  EVENTS *****
    //*********************************


    public void onClose(CloseEvent event) { //TODO: Add close event

        StorageConfig storageConfig = storage.getStorageConfig();
        Player player = (Player) event.getEvent().getPlayer();

        //SAVE STORAGE
        storage.closeStorage(this.page, player); //TODO: Modify this method

        //events
        CloseStoragePageActionEvent closeStoragePageActionEvent = new CloseStoragePageActionEvent(this, event); //TODO: Modify this event
        StorageMechanic.getInstance().getManagers().getActionManager().callEvent(closeStoragePageActionEvent, storage.getId(), storage);


        //Hopper event
        if (storage.getStorageOriginContext().getContext().equals(StorageOriginContext.Context.BLOCK_STORAGE)) {
            List<String> list = storage.getStorageOriginContext().getData();
            BlockMechanicManager.HOPPER_BLOCK_MECHANIC.checkBlockStorageAndTransfer(new String[]{list.get(1), list.get(0), list.get(2)});
        }

        //SOUNDS
        if (storageConfig.isStorageSoundEnabled()) {
            for (StorageSoundConfig soundConfig : storageConfig.getStorageSounds()) {
                if (soundConfig.getType().equals(StorageSoundConfig.Type.CLOSE)) {
                    if (soundConfig.getPagesToSlots().containsKey(this.page)) {
                        player.playSound(player.getLocation(), soundConfig.getSound(), soundConfig.getVolume(), soundConfig.getPitch());
                    }
                }
            }
        }
    }

    public void onClick(InventoryClickEvent event) { //TODO: Add click event

    }

    public void onDrag(InventoryDragEvent event) { //TODO: Add drag event

        StorageConfig storageConfig = storage.getStorageConfig();

        DragItemCheck(storage, storageInventory, event, storageConfig);

        if (storageConfig.isStorageBlockItemEnabled()) {
            DragItemBlocked(storage, storageInventory, event, storageConfig);
        }

        if (storageConfig.isStorageItemsWhiteListEnabled() || storageConfig.isStorageItemsBlackListEnabled()) {
            DragItemCheckList(storage, storageInventory, event, storageConfig);
        }
    }

    public void onOpen(InventoryOpenEvent event) {
        StorageConfig storageConfig = storage.getStorageConfig();

        //Action event
        OpenStoragePageActionEvent openStoragePageActionEvent = new OpenStoragePageActionEvent(this, event);
        StorageMechanic.getInstance().getManagers().getActionManager().callEvent(openStoragePageActionEvent, storage.getId(), storage);

        //Hopper event
        if (storage.getStorageOriginContext().getContext().equals(StorageOriginContext.Context.BLOCK_STORAGE)) {
            List<String> list = storage.getStorageOriginContext().getData();
            BlockMechanicManager.HOPPER_BLOCK_MECHANIC.checkBlockStorageAndTransfer(new String[]{list.get(1), list.get(0), list.get(2)});
        }

        //SOUNDS
        if (storageConfig.isStorageSoundEnabled()) {
            for (StorageSoundConfig soundConfig : storageConfig.getStorageSounds()) {
                if (soundConfig.getType().equals(StorageSoundConfig.Type.OPEN)) {
                    if (soundConfig.getPagesToSlots().containsKey(this.page)) {
                        ((Player) event.getPlayer()).playSound(event.getPlayer().getLocation(), soundConfig.getSound(), soundConfig.getVolume(), soundConfig.getPitch());
                    }
                }
            }
        }
    }

    //*********************************
    //***** INVENTORY METHODS *****
    //*********************************

    /**
     * Closes all open inventories.
     */
    public void closeInventoryAll() {
        while (!inventory.getViewers().isEmpty()) {
            inventory.getViewers().get(0).closeInventory();
        }
    }

    /**
     * Sets the title of the inventory to the specified title for the specified player or all viewers.
     *
     * @param title  The new title of the inventory.
     * @param player The player for whom to set the title. Pass null to set the title for all viewers.
     */
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

    //*********************************
    //***** INVENTORY HOLDER METHODS *****
    //*********************************

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Returns the InventoryHolder associated with this StorageInventory instance.
     *
     * @return The InventoryHolder associated with this StorageInventory instance.
     */
    public InventoryHolder getInventoryHolder() {
        return this;
    }

    /**
     * Opens the storage inventory for the specified player.
     *
     * @param player The player to open the inventory for.
     */
    public void open(Player player) {
        player.openInventory(inventory);
    }

    /**
     * Returns the ID of the StorageInventory.
     *
     * @return The ID of the StorageInventory.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the storage object associated with this StorageInventory.
     *
     * @return The storage object.
     */
    public Storage getStorage() {
        return storage;
    }

    /**
     * Retrieves the current page number.
     *
     * @return The current page number.
     */
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
            System.out.println(isClosed());
            if (isClosed()) return;
            if (currentStage != null && (stages.indexOf(currentStage) + 1) < stages.size())
                currentStage = stages.get(stages.indexOf(currentStage) + 1);
            else currentStage = stages.get(0);
            setStage(currentStage);

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

    public StageStorage getCurrentStage() {
        return currentStage;
    }

    public void setClosed(boolean closed) {
        this.closed.set(closed);
    }

    public boolean isClosed() {
        return closed.get();
    }


}
