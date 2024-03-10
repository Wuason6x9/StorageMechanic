package dev.wuason.storagemechanic.storages.inventory;

import dev.wuason.libs.invmechaniclib.events.CloseEvent;
import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.mechanics.utils.Utils;
import dev.wuason.nms.wrappers.NMSManager;
import dev.wuason.storagemechanic.Managers;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.actions.events.def.ClickItemInterfaceActionEvent;
import dev.wuason.storagemechanic.actions.events.def.ClickStoragePageActionEvent;
import dev.wuason.storagemechanic.actions.events.def.CloseStoragePageActionEvent;
import dev.wuason.storagemechanic.actions.events.def.OpenStoragePageActionEvent;
import dev.wuason.storagemechanic.api.events.storage.ClickPageStorageEvent;
import dev.wuason.storagemechanic.items.ItemInterface;
import dev.wuason.storagemechanic.items.ItemInterfaceManager;
import dev.wuason.storagemechanic.items.items.PlaceholderItemInterface;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageOriginContext;
import dev.wuason.storagemechanic.storages.config.*;
import dev.wuason.storagemechanic.storages.types.block.mechanics.BlockMechanicManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class StorageInventory implements InventoryHolder {

    private final Inventory inventory;
    private final Storage storage;
    private int page;
    private BukkitTask animationStagesTask;
    private StageStorage currentStage;


    public StorageInventory(StorageConfig storageConfig, Storage storage, int page) {
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
        this.page = page;
        this.storage = storage;
        inventory = Bukkit.createInventory(this, (rows * 9), AdventureUtils.deserializeLegacy(Utils.replaceVariables(title, replacements), null));
    }

    //*********************************
    //***** INVENTORY  EVENTS *****
    //*********************************


    public void onClose(CloseEvent event) {

        StorageConfig storageConfig = this.storage.getStorageConfig();
        Player player = (Player) event.getEvent().getPlayer();

        //SAVE STORAGE
        this.storage.closeStorage(this.page, player); //TODO: Modify this method

        //call method to close the storage bug
        if(storageConfig.getRefreshTimeStages() > 0){
            NMSManager.getVersionWrapper().sendCloseInventoryPacket(player);
        }

        //events
        CloseStoragePageActionEvent closeStoragePageActionEvent = new CloseStoragePageActionEvent(this, event.getEvent()); //TODO: Modify this event
        StorageMechanic.getInstance().getManagers().getActionManager().callEvent(closeStoragePageActionEvent, this.storage.getId(), this.storage);


        //Hopper event
        if (this.storage.getStorageOriginContext().getContext().isBlockStorage()) {
            List<String> list = this.storage.getStorageOriginContext().getData();
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

    public void onClick(InventoryClickEvent event) {
        StorageConfig storageConfig = this.storage.getStorageConfig();
        Player player = (Player) event.getWhoClicked();
        if (storageConfig.isStorageSoundEnabled()) {
            for (StorageSoundConfig soundConfig : storageConfig.getStorageSounds()) {
                if (soundConfig.getType().equals(StorageSoundConfig.Type.CLICK) && soundConfig.getPagesToSlots().containsKey(this.page)) {
                    if (!soundConfig.getPagesToSlots().get(this.page).isEmpty() && event.getClickedInventory() != null && !event.getClickedInventory().getType().equals(InventoryType.PLAYER) && soundConfig.getPagesToSlots().get(this.page).contains(this.page))
                        player.playSound(player.getLocation(), soundConfig.getSound(), soundConfig.getVolume(), soundConfig.getPitch());
                    else
                        player.playSound(event.getWhoClicked().getLocation(), soundConfig.getSound(), soundConfig.getVolume(), soundConfig.getPitch());
                }
            }
        }
        if (event.getCurrentItem() != null && event.getClickedInventory().getType().equals(InventoryType.PLAYER) && event.getClick().isShiftClick()) {
            shiftClickItemCheck(event);
            if (storageConfig.isStorageBlockItemEnabled() && event.getCurrentItem() != null)
                shiftClickItemBlocked(event);
            if (storageConfig.isStorageItemsWhiteListEnabled() || storageConfig.isStorageItemsBlackListEnabled() && event.getCurrentItem() != null)
                shiftClickItemCheckList(event);
        }
        //ITEMS INTERFACES & Item CheckList
        if (event.getClickedInventory() != null && !event.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
            // Cancel event if clicked item is an interface item
            ItemStack clickedItem = event.getCurrentItem();
            ItemInterfaceManager itemInterfaceManager = StorageMechanic.getInstance().getManagers().getItemInterfaceManager();
            if (clickedItem != null && itemInterfaceManager.isItemInterface(clickedItem)) {
                event.setCancelled(true);
                ItemInterface itemInterface = StorageMechanic.getInstance().getManagers().getItemInterfaceManager().getItemInterfaceByItemStack(clickedItem);
                itemInterface.onClick(storage, this, event, storageConfig);
                //events action
                ClickItemInterfaceActionEvent clickItemInterfaceActionEvent = new ClickItemInterfaceActionEvent(this, event, itemInterface);
                StorageMechanic.getInstance().getManagers().getActionManager().callEvent(clickItemInterfaceActionEvent, storage.getId(), storage);
            }
            clickItemCheck(event);
            if (storageConfig.isStorageBlockItemEnabled()) {
                clickItemBlocked(event);
            }
            if (storageConfig.isStorageItemsWhiteListEnabled() || storageConfig.isStorageItemsBlackListEnabled()) {
                clickItemCheckList(event);
            }
        }
        //Hopper event
        if (storage.getStorageOriginContext().getContext().equals(StorageOriginContext.Context.BLOCK_STORAGE)) {
            List<String> list = storage.getStorageOriginContext().getData();
            BlockMechanicManager.HOPPER_BLOCK_MECHANIC.checkBlockStorageAndTransfer(new String[]{list.get(1), list.get(0), list.get(2)});
        }
        //Events action

        ClickStoragePageActionEvent clickStoragePageActionEvent = new ClickStoragePageActionEvent(this, event);
        StorageMechanic.getInstance().getManagers().getActionManager().callEvent(clickStoragePageActionEvent, storage.getId(), storage);

        //Events Bukkit
        ClickPageStorageEvent clickPageStorageEvent = new ClickPageStorageEvent(event, this);
        Bukkit.getPluginManager().callEvent(clickPageStorageEvent);
    }

    public void onDrag(InventoryDragEvent event) {
        StorageConfig storageConfig = storage.getStorageConfig();
        dragItemCheck(event);
        if (storageConfig.isStorageBlockItemEnabled()) {
            dragItemBlocked(event);
        }
        if (storageConfig.isStorageItemsWhiteListEnabled() || storageConfig.isStorageItemsBlackListEnabled()) {
            dragItemCheckList(event);
        }
    }

    public void onOpen(InventoryOpenEvent event) {
        StorageConfig storageConfig = this.storage.getStorageConfig();
        //Action event
        OpenStoragePageActionEvent openStoragePageActionEvent = new OpenStoragePageActionEvent(this, event);
        StorageMechanic.getInstance().getManagers().getActionManager().callEvent(openStoragePageActionEvent, this.storage.getId(), this.storage);
        //Hopper event
        if (this.storage.getStorageOriginContext().getContext().equals(StorageOriginContext.Context.BLOCK_STORAGE)) {
            List<String> list = this.storage.getStorageOriginContext().getData();
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

    //**********************************
    //********* CLICK ITEM  ************
    //**********************************


    public void clickItemBlocked(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack cursor = event.getCursor();
        if(event.getHotbarButton() != -1) {
            cursor = player.getInventory().getItem(event.getHotbarButton());
        }
        StorageConfig storageConfig = this.storage.getStorageConfig();
        if (cursor != null && !cursor.getType().isAir()) {
            if (storageConfig.isStorageBlockItemEnabled()) {
                StorageBlockItemConfig storageBlockItemConfig = this.storage.getStorageBlockItemConfig(event.getSlot(), this.page);
                if (storageBlockItemConfig != null) {
                    if (storageBlockItemConfig.getMessage() != null) {
                        AdventureUtils.playerMessage(storageBlockItemConfig.getMessage(), player);
                    }
                    event.setCancelled(true);
                }
            }
        }
    }

    public void clickItemCheck(InventoryClickEvent event) {
        ItemStack cursor = event.getCursor();
        if(event.getHotbarButton() != -1) {
            cursor = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
        }
        Managers managers = StorageMechanic.getInstance().getManagers();
        if (cursor != null && !cursor.getType().isAir()) {
            //ITEM STORAGE
            if (managers.getItemStorageManager().isItemStorage(cursor)) {
                String[] src = managers.getItemStorageManager().getDataFromItemStack(cursor).split(":");
                if (src[1].equals(this.storage.getId())) {
                    event.setCancelled(true);
                    return;
                }
                if (!managers.getItemStorageConfigManager().getItemStorageConfig(src[0]).getItemStoragePropertiesConfig().isStorageable()) {
                    event.setCancelled(true);
                    return;
                }
            }
            //FURNITURE STORAGE
            if (managers.getFurnitureStorageManager().isShulker(cursor)) {
                String[] src = managers.getFurnitureStorageManager().getShulkerData(cursor).split(":");
                managers.getFurnitureStorageConfigManager().findFurnitureStorageConfigById(src[1]).ifPresent(furnitureStorageC -> {
                    if (!furnitureStorageC.getFurnitureStorageProperties().isStorageable()) {
                        event.setCancelled(true);
                        return;
                    }
                });
            }
            //BLOCK STORAGE
            if (managers.getBlockStorageManager().isShulker(cursor)) {
                String[] src = managers.getBlockStorageManager().getShulkerData(cursor).split(":");
                managers.getBlockStorageConfigManager().findBlockStorageConfigById(src[1]).ifPresent(blockStorageC -> {
                    if (!blockStorageC.getBlockStorageProperties().isStorageable()) {
                        event.setCancelled(true);
                        return;
                    }
                });
            }
        }
    }

    public void clickItemCheckList(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack cursor = event.getCursor();
        if(event.getHotbarButton() != -1) {
            cursor = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
        }
        StorageConfig storageConfig = this.storage.getStorageConfig();
        if (cursor != null && !cursor.getType().isAir()) {
            if (storageConfig.isStorageItemsWhiteListEnabled()) {
                if (!storage.isItemInList(cursor, event.getSlot(), this.page, Storage.ListType.WHITELIST, storageConfig)) {
                    AdventureUtils.playerMessage(storageConfig.getWhiteListMessage(), player);
                    event.setCancelled(true);
                }
            }
            if (storageConfig.isStorageItemsBlackListEnabled()) {
                if (storage.isItemInList(cursor, event.getSlot(), this.page, Storage.ListType.BLACKLIST, storageConfig)) {
                    AdventureUtils.playerMessage(storageConfig.getBlackListMessage(), player);
                    event.setCancelled(true);
                }
            }
        }
    }

    //**********************************
    //********* SHIFT ITEM  ************
    //**********************************

    public void shiftClickItemCheck(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        Managers managers = StorageMechanic.getInstance().getManagers();
        if (!clickedItem.getType().isAir()) {
            //ITEM STORAGE
            if (managers.getItemStorageManager().isItemStorage(clickedItem)) {
                String[] src = managers.getItemStorageManager().getDataFromItemStack(clickedItem).split(":");
                if (src[1].equals(this.storage.getId())) {
                    event.setCancelled(true);
                    return;
                }
                if (!managers.getItemStorageConfigManager().getItemStorageConfig(src[0]).getItemStoragePropertiesConfig().isStorageable()) {
                    event.setCancelled(true);
                    return;
                }
            }
            //FURNITURE STORAGE
            if (managers.getFurnitureStorageManager().isShulker(clickedItem)) {
                String[] src = managers.getFurnitureStorageManager().getShulkerData(clickedItem).split(":");
                managers.getFurnitureStorageConfigManager().findFurnitureStorageConfigById(src[1]).ifPresent(furnitureStorageC -> {
                    if (!furnitureStorageC.getFurnitureStorageProperties().isStorageable()) {
                        event.setCancelled(true);
                        return;
                    }
                });
            }
            //BLOCK STORAGE
            if (managers.getBlockStorageManager().isShulker(clickedItem)) {
                String[] src = managers.getBlockStorageManager().getShulkerData(clickedItem).split(":");
                managers.getBlockStorageConfigManager().findBlockStorageConfigById(src[1]).ifPresent(blockStorageC -> {
                    if (!blockStorageC.getBlockStorageProperties().isStorageable()) {
                        event.setCancelled(true);
                        return;
                    }
                });
            }
        }
    }

    public void shiftClickItemCheckList(InventoryClickEvent event) {
        int slotFree = this.inventory.firstEmpty();
        if (slotFree == -1) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        ;
        StorageConfig storageConfig = this.storage.getStorageConfig();
        if (!clickedItem.getType().isAir()) {
            if (storageConfig.isStorageItemsWhiteListEnabled()) {
                if (!storage.isItemInList(clickedItem, slotFree, this.page, Storage.ListType.WHITELIST, storageConfig)) {
                    AdventureUtils.playerMessage(storageConfig.getWhiteListMessage(), player);
                    event.setCancelled(true);
                }
            }
            if (storageConfig.isStorageItemsBlackListEnabled()) {
                if (storage.isItemInList(clickedItem, slotFree, this.page, Storage.ListType.BLACKLIST, storageConfig)) {
                    AdventureUtils.playerMessage(storageConfig.getBlackListMessage(), player);
                    event.setCancelled(true);
                }
            }
        }
    }

    public void shiftClickItemBlocked(InventoryClickEvent event) {
        int slotFree = this.inventory.firstEmpty();
        if (slotFree == -1) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        StorageConfig storageConfig = this.storage.getStorageConfig();
        if (!clickedItem.getType().isAir()) {
            if (storageConfig.isStorageBlockItemEnabled()) {
                StorageBlockItemConfig storageBlockItemConfig = this.storage.getStorageBlockItemConfig(slotFree, this.page);
                if (storageBlockItemConfig != null) {
                    if (storageBlockItemConfig.getMessage() != null) {
                        AdventureUtils.playerMessage(storageBlockItemConfig.getMessage(), player);
                    }
                    event.setCancelled(true);
                }
            }
        }
    }

    //**********************************
    //********* DRAG ITEM  *************
    //**********************************

    public void dragItemCheck(InventoryDragEvent event) {
        ItemStack cursor = event.getCursor();
        Managers managers = StorageMechanic.getInstance().getManagers();
        if (cursor == null) {
            cursor = (ItemStack) (event.getNewItems().values().toArray())[0];
        }
        if (cursor != null && !cursor.getType().equals(Material.AIR)) {
            //ITEM STORAGE
            if (managers.getItemStorageManager().isItemStorage(cursor)) {
                String[] src = managers.getItemStorageManager().getDataFromItemStack(cursor).split(":");
                if (src[1].equals(this.storage.getId())) {
                    event.setCancelled(true);
                    return;
                }
                if (!managers.getItemStorageConfigManager().getItemStorageConfig(src[0]).getItemStoragePropertiesConfig().isStorageable()) {
                    event.setCancelled(true);
                    return;
                }
            }
            //FURNITURE STORAGE
            if (managers.getFurnitureStorageManager().isShulker(cursor)) {
                String[] src = managers.getFurnitureStorageManager().getShulkerData(cursor).split(":");
                managers.getFurnitureStorageConfigManager().findFurnitureStorageConfigById(src[1]).ifPresent(furnitureStorageC -> {
                    if (!furnitureStorageC.getFurnitureStorageProperties().isStorageable()) {
                        event.setCancelled(true);
                        return;
                    }
                });
            }
            //BLOCK STORAGE
            if (managers.getBlockStorageManager().isShulker(cursor)) {
                String[] src = managers.getBlockStorageManager().getShulkerData(cursor).split(":");
                managers.getBlockStorageConfigManager().findBlockStorageConfigById(src[1]).ifPresent(blockStorageC -> {
                    if (!blockStorageC.getBlockStorageProperties().isStorageable()) {
                        event.setCancelled(true);
                        return;
                    }
                });
            }
        }
    }

    public void dragItemBlocked(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        StorageConfig storageConfig = this.storage.getStorageConfig();
        ItemStack cursor = event.getCursor();
        if (cursor == null) {
            cursor = (ItemStack) (event.getNewItems().values().toArray())[0];
        }
        if (cursor != null && !cursor.getType().equals(Material.AIR)) {
            if (storageConfig.isStorageBlockItemEnabled()) {
                for (Integer s : event.getRawSlots()) {
                    StorageBlockItemConfig storageBlockItemConfig = this.storage.getStorageBlockItemConfig(s, this.page);
                    if (storageBlockItemConfig != null) {
                        if (storageBlockItemConfig.getMessage() != null) {
                            AdventureUtils.playerMessage(storageBlockItemConfig.getMessage(), player);
                        }
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    public void dragItemCheckList(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        StorageConfig storageConfig = this.storage.getStorageConfig();
        ItemStack cursor = event.getCursor();
        if (cursor == null) {
            cursor = (ItemStack) (event.getNewItems().values().toArray())[0];
        }
        if (cursor != null && !cursor.getType().equals(Material.AIR)) {
            if (storageConfig.isStorageItemsWhiteListEnabled()) {
                for (Integer s : event.getRawSlots()) {
                    if (!this.storage.isItemInList(cursor, s, this.page, Storage.ListType.WHITELIST, storageConfig)) {
                        AdventureUtils.playerMessage(storageConfig.getWhiteListMessage(), player);
                        event.setCancelled(true);
                    }
                }
            }
            if (storageConfig.isStorageItemsBlackListEnabled()) {
                for (Integer s : event.getRawSlots()) {
                    if (this.storage.isItemInList(cursor, s, this.page, Storage.ListType.BLACKLIST, storageConfig)) {
                        AdventureUtils.playerMessage(storageConfig.getBlackListMessage(), player);
                        event.setCancelled(true);
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

        if (animationStagesTask != null && !animationStagesTask.isCancelled()) return;
        StorageConfig storageConfig = storage.getStorageConfig();
        ArrayList<StageStorage> stages = storageConfig.getStagesOrder();
        if (storageConfig.getRefreshTimeStages() < 1 || stages.isEmpty()) return;

        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(StorageMechanic.getInstance(), () -> {
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


}
