package dev.wuason.storagemechanic.storages;

import dev.wuason.libs.adapter.Adapter;
import dev.wuason.mechanics.items.ItemBuilder;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.mechanics.utils.MathUtils;
import dev.wuason.storagemechanic.Debug;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.api.events.storage.CloseStorageEvent;
import dev.wuason.storagemechanic.api.events.storage.OpenStorageEvent;
import dev.wuason.storagemechanic.items.ItemInterfaceManager;
import dev.wuason.storagemechanic.items.items.PlaceholderItemInterface;
import dev.wuason.storagemechanic.storages.config.*;
import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import org.apache.commons.lang3.function.TriFunction;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.*;

public class Storage {
    private final Map<Integer, StorageInventory> inventories = new HashMap<>();
    private Map<Integer, ItemStack[]> items = new HashMap<>();

    private final String id;
    private final String storageIdConfig;
    private Date date = new Date();
    private Date lastOpen = new Date();
    private Date lastAccess = new Date();

    private final StorageMechanic core = StorageMechanic.getInstance();

    private StorageOriginContext storageOriginContext;
    private boolean isTempStorage = false;

    /**
     * Constructs a new Storage object with the given storageIdConfig and storageOriginContext.
     *
     * @param storageIdConfig      The storage id configuration.
     * @param storageOriginContext The storage origin context.
     */
    public Storage(String storageIdConfig, StorageOriginContext storageOriginContext) {
        this.id = UUID.randomUUID().toString();
        this.storageIdConfig = storageIdConfig;
        this.storageOriginContext = storageOriginContext;
    }

    /**
     * Constructs a new Storage object.
     *
     * @param storageIdConfig      The storage ID configuration.
     * @param id                   The UUID of the storage.
     * @param storageOriginContext The origin context of the storage.
     */
    public Storage(String storageIdConfig, UUID id, StorageOriginContext storageOriginContext) {
        this.id = id.toString();
        this.storageIdConfig = storageIdConfig;
        this.storageOriginContext = storageOriginContext;
    }

    /**
     * Creates a new Storage object.
     *
     * @param id                   The ID of the storage.
     * @param items                A map representing the items in the storage. The keys are page numbers and the values are arrays of ItemStacks.
     * @param storageIdConfig      The configuration ID for the storage.
     * @param date                 The creation date of the storage.
     * @param storageOriginContext The origin context of the storage, which specifies the type of storage.
     * @param lastOpen             The date when the storage was last opened. If null, the current date will be used.
     */
    public Storage(String id, Map<Integer, ItemStack[]> items, String storageIdConfig, Date date, StorageOriginContext storageOriginContext, Date lastOpen) {
        this.id = id;
        this.items = items;
        this.storageIdConfig = storageIdConfig;
        this.date = date;
        this.storageOriginContext = storageOriginContext;
        this.lastOpen = lastOpen != null ? lastOpen : new Date();
    }

    /**
     * Closes the storage on the specified page for the given player.
     *
     * @param page   The page of the storage to close.
     * @param player The player for whom the storage is being closed.
     */
    public void closeStorage(int page, Player player) {
        if (inventories.containsKey(page)) {
            StorageInventory storageInventory = inventories.get(page);
            if (storageInventory.getInventory().getViewers().size() <= 1) { // <= 1 because the current player is still in the viewer list
                ItemStack[] contents = storageInventory.getInventory().getContents();
                ItemInterfaceManager itemInterfaceManager = StorageMechanic.getInstance().getManagers().getItemInterfaceManager();

                for (int i = 0; i < contents.length; i++) {
                    ItemStack item = contents[i];
                    if (item != null && (itemInterfaceManager.isItemInterface(item) && !PlaceholderItemInterface.isPlaceholderItem(item))) {
                        contents[i] = null;
                    }
                }

                items.put(page, contents);
                storageInventory.stopAnimationStages();
                inventories.remove(page);

                if (getStorageConfig().getStorageProperties().isDropItemsPageOnClose())
                    dropItemsFromPage(player.getLocation(), page);

                //MYTHIC
                if (getInventories().isEmpty()) {
                    if (core.getManagers().getMythicManager() != null) {
                        core.getManagers().getMythicManager().executeCloseStorageSkill(storageOriginContext, id, storageInventory);
                    }
                }

                CloseStorageEvent closeStorageEvent = new CloseStorageEvent(player, storageInventory);
                Bukkit.getPluginManager().callEvent(closeStorageEvent);
            }
        }

    }

    /**
     * Closes all inventories associated with the storage object.
     */
    public void closeAllInventory() {
        while (!inventories.isEmpty()) {
            StorageInventory storageInventory = (StorageInventory) (inventories.values().toArray())[0];
            while (!storageInventory.getInventory().getViewers().isEmpty()) {
                storageInventory.getInventory().getViewers().get(0).closeInventory();
            }
        }
    }

    /**
     * Opens a storage for a player on the specified page.
     *
     * @param player The player to open the storage for.
     * @param page   The page of the storage to open.
     * @return True if the storage was successfully opened, false otherwise.
     */
    public boolean openStorage(Player player, int page) {

        StorageConfig storageConfig = getStorageConfig();

        if (player.getOpenInventory() != null) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof StorageInventory storageInventory) {
                if (storageInventory.getStorage().equals(this) && storageInventory.getPage() == page) {
                    return false;
                }
            }
        }

        if (storageConfig.getMaxViewers() != -1) {
            if (getAllViewers().size() >= storageConfig.getMaxViewers()) {
                AdventureUtils.sendMessage(player, core.getManagers().getConfigManager().getLangDocumentYaml().getString("messages.storage.max_views"));
                return false;
            }
        }

        lastOpen = new Date();

        if (!items.containsKey(page)) {
            int slots = storageConfig.getInventoryType().getSize();
            if (storageConfig.getInventoryType().equals(StorageInventoryTypeConfig.CHEST)) {
                slots = (storageConfig.getRows() * 9);
            }

            ItemStack[] itemsInv = new ItemStack[slots]; //SLOTS

            if (storageConfig.isStorageItemsDefaultEnabled()) {

                for (StorageItemConfig itemDefault : storageConfig.getStorageItemsDefaultConfig()) {
                    if (!itemDefault.getPagesToSlots().containsKey(page)) continue;
                    for (String item : itemDefault.getItemsList()) {
                        for (int s : itemDefault.getPagesToSlots().get(page)) {
                            if (!MathUtils.chance(itemDefault.getChance())) continue;
                            ItemStack itemStack = Adapter.getItemStack(item);
                            itemStack.setAmount(MathUtils.randomNumberString(itemDefault.getAmount()));
                            itemsInv[s] = itemStack;

                        }

                    }

                }

            }

            items.put(page, itemsInv);
        }

        if (!inventories.containsKey(page)) {
            StorageInventory inventory = new StorageInventory(storageConfig, this, page);
            inventory.getInventory().setContents(items.get(page));
            if (storageConfig.isStorageItemsInterfaceEnabled()) {
                if (storageConfig.getStorageItemsInterfaceConfig().containsKey(page)) {
                    for (Map.Entry<Integer, StorageItemInterfaceConfig> entry : storageConfig.getStorageItemsInterfaceConfig().get(page).entrySet()) {
                        if (PlaceholderItemInterface.isPlaceholderItem(inventory.getInventory().getItem(entry.getKey())))
                            continue;
                        inventory.getInventory().setItem(entry.getKey(), entry.getValue().getItemInterface().getItemStack());
                    }
                }
            }
            inventories.put(page, inventory);
        }
        StorageInventory storageInventoryPage = inventories.get(page);
        storageInventoryPage.open(player);
        if (storageConfig.getRefreshTimeStages() != 0L && storageConfig.getRefreshTimeStages() != -1L)
            storageInventoryPage.startAnimationStages();
        if (storageInventoryPage.getCurrentStage() != null && storageInventoryPage.getCurrentStage().getTitle() != null)
            storageInventoryPage.setTitleInventory(storageInventoryPage.getCurrentStage().getTitle(), player);

        OpenStorageEvent openStorageEvent = new OpenStorageEvent(player, storageInventoryPage);
        Bukkit.getPluginManager().callEvent(openStorageEvent);

        return true;
    }

    /**
     * Opens the storage for a given player and page.
     *
     * @param player the player for whom the storage will be opened
     * @param page   the page number of the storage to be opened
     * @return true if the storage was successfully opened, false otherwise
     */
    public boolean openStorageR(Player player, int page) {
        if (page < 0 || page >= getTotalPages()) return false;
        Debug.debugToPlayer("Opening storage page " + page, player);
        return openStorage(player, page);
    }


    public void removeItemsInterface(int page) {
        if (!inventories.containsKey(page)) return;
        StorageInventory storageInventory = inventories.get(page);
        for (int i = 0; i < storageInventory.getInventory().getSize(); i++) {
            ItemStack item = storageInventory.getInventory().getItem(i);
            if (item != null && !item.getType().equals(Material.AIR) && core.getManagers().getItemInterfaceManager().isItemInterface(item)) {
                storageInventory.getInventory().clear(i);
            }
        }
    }


    public void loadAllItemsDefault() {
        for (int p = 0; p < getTotalPages(); p++) {
            loadItemsDefault(p);
        }
    }

    /**
     * Loads the default items for a given page in the storage.
     *
     * @param page The page number to load the items for.
     */
    public void loadItemsDefault(int page) {

        StorageConfig storageConfig = getStorageConfig();

        if (storageConfig.isStorageItemsDefaultEnabled()) {

            if (!items.containsKey(page)) {

                int slots = storageConfig.getInventoryType().getSize();
                if (storageConfig.getInventoryType().equals(StorageInventoryTypeConfig.CHEST)) {
                    slots = (storageConfig.getRows() * 9);
                }

                ItemStack[] itemsInv = new ItemStack[slots]; //SLOTS

                for (StorageItemConfig itemDefault : storageConfig.getStorageItemsDefaultConfig()) {

                    if (!itemDefault.getPagesToSlots().containsKey(page)) continue;
                    for (ItemStack item : itemDefault.getItems()) {
                        for (int s : itemDefault.getPagesToSlots().get(page)) {
                            if (!MathUtils.chance(itemDefault.getChance())) continue;
                            ItemStack itemStack = item.clone();
                            itemStack.setAmount(MathUtils.randomNumberString(itemDefault.getAmount()));
                            itemsInv[s] = itemStack;
                        }
                    }
                }
                items.put(page, itemsInv);
            }

        }
    }

    /**
     * Adds an ItemStack to a specific page of the storage inventory.
     *
     * @param page      The page number of the storage inventory.
     * @param itemStack The ItemStack to be added.
     * @return A list of ItemStacks that were not successfully added to the inventory.
     */
    public List<ItemStack> addItemStack(int page, ItemStack itemStack) {
        List<ItemStack> notAddedItems = new ArrayList<>();
        StorageConfig storageConfig = getStorageConfig();

        if (inventories.containsKey(page)) {
            StorageInventory storageInventory = inventories.get(page);
            Inventory inventory = storageInventory.getInventory();
            HashMap<Integer, ItemStack> remainingItems = inventory.addItem(itemStack);
            if (!remainingItems.isEmpty()) {
                notAddedItems.addAll(remainingItems.values());
            }
        } else {
            ItemStack[] contents = items.get(page);
            if (contents == null) {
                int slots = getStorageConfig().getInventoryType().getSize();
                contents = new ItemStack[slots];
                items.put(page, contents);
            }

            int remainingAmount = itemStack.getAmount();
            for (int i = 0; i < contents.length && remainingAmount > 0; i++) {
                if (isItemInterfaceSlot(page, i, storageConfig)) continue;
                if (contents[i] == null || contents[i].getAmount() == 0) {
                    contents[i] = itemStack.clone();
                    contents[i].setAmount(remainingAmount);
                    remainingAmount = 0;
                } else if (contents[i].isSimilar(itemStack)) {
                    int currentAmount = contents[i].getAmount();
                    int maxAmount = contents[i].getMaxStackSize();
                    int totalAmount = currentAmount + remainingAmount;

                    if (totalAmount <= maxAmount) {
                        contents[i].setAmount(totalAmount);
                        remainingAmount = 0;
                    } else {
                        contents[i].setAmount(maxAmount);
                        remainingAmount = totalAmount - maxAmount;
                    }
                }
            }
            if (remainingAmount > 0) {
                ItemStack notAddedItem = itemStack.clone();
                notAddedItem.setAmount(remainingAmount);
                notAddedItems.add(notAddedItem);
            }
        }

        return notAddedItems;
    }

    /**
     * Adds an ItemStack to the specified page of the storage inventory, taking into account
     * restrictions defined in the storage configuration.
     *
     * @param page      the page index of the storage inventory
     * @param itemStack the ItemStack to be added
     * @return a list of ItemStacks that were not able to be added to the storage inventory
     */
    public List<ItemStack> addItemStackWithRestrictions(int page, ItemStack itemStack) {
        List<ItemStack> notAddedItems = new ArrayList<>();
        StorageConfig storageConfig = getStorageConfig();

        if (inventories.containsKey(page)) {
            StorageInventory storageInventory = inventories.get(page);
            Inventory inventory = storageInventory.getInventory();

            int remainingAmount = itemStack.getAmount();
            for (int i = 0; i < inventory.getSize() && remainingAmount > 0; i++) {
                if (!canBePlaced(itemStack, page, i, storageConfig)) continue;

                ItemStack currentSlot = inventory.getItem(i);
                if (currentSlot == null || currentSlot.getAmount() == 0) {
                    ItemStack cloneItem = itemStack.clone();
                    cloneItem.setAmount(remainingAmount);
                    inventory.setItem(i, cloneItem);
                    remainingAmount = 0;
                } else if (currentSlot.isSimilar(itemStack)) {
                    int currentAmount = currentSlot.getAmount();
                    int maxAmount = currentSlot.getMaxStackSize();
                    int totalAmount = currentAmount + remainingAmount;

                    if (totalAmount <= maxAmount) {
                        currentSlot.setAmount(totalAmount);
                        remainingAmount = 0;
                    } else {
                        currentSlot.setAmount(maxAmount);
                        remainingAmount = totalAmount - maxAmount;
                    }
                }
            }
            if (remainingAmount > 0) {
                ItemStack notAddedItem = itemStack.clone();
                notAddedItem.setAmount(remainingAmount);
                notAddedItems.add(notAddedItem);
            }
        } else {
            ItemStack[] contents = items.get(page);
            if (contents == null) {
                int slots = getStorageConfig().getInventoryType().getSize();
                contents = new ItemStack[slots];
                items.put(page, contents);
            }

            int remainingAmount = itemStack.getAmount();
            for (int i = 0; i < contents.length && remainingAmount > 0; i++) {
                if (!canBePlaced(itemStack, page, i, storageConfig)) continue;
                if (contents[i] == null || contents[i].getAmount() == 0) {
                    contents[i] = itemStack.clone();
                    contents[i].setAmount(remainingAmount);
                    remainingAmount = 0;
                } else if (contents[i].isSimilar(itemStack)) {
                    int currentAmount = contents[i].getAmount();
                    int maxAmount = contents[i].getMaxStackSize();
                    int totalAmount = currentAmount + remainingAmount;

                    if (totalAmount <= maxAmount) {
                        contents[i].setAmount(totalAmount);
                        remainingAmount = 0;
                    } else {
                        contents[i].setAmount(maxAmount);
                        remainingAmount = totalAmount - maxAmount;
                    }
                }
            }
            if (remainingAmount > 0) {
                ItemStack notAddedItem = itemStack.clone();
                notAddedItem.setAmount(remainingAmount);
                notAddedItems.add(notAddedItem);
            }
        }

        return notAddedItems;
    }

    /**
     * Removes the specified ItemStack from the given page in the inventory.
     *
     * @param page      The page number of the inventory.
     * @param itemStack The ItemStack to be removed.
     */
    public void removeItemStack(int page, ItemStack itemStack) {
        if (inventories.containsKey(page)) {
            StorageInventory storageInventory = inventories.get(page);
            storageInventory.getInventory().removeItem(itemStack);
        }

        ItemStack[] contents = items.get(page);
        if (contents != null) {
            int remainingAmount = itemStack.getAmount();

            for (int i = 0; i < contents.length && remainingAmount > 0; i++) {
                if (contents[i] != null && contents[i].isSimilar(itemStack)) {
                    int currentAmount = contents[i].getAmount();
                    if (currentAmount <= remainingAmount) {
                        remainingAmount -= currentAmount;
                        contents[i] = null;
                    } else {
                        contents[i].setAmount(currentAmount - remainingAmount);
                        remainingAmount = 0;
                    }
                }
            }
        }
    }

    /**
     *
     */
    public List<ItemStack> removeItemStackAtSlot(int page, int slot, ItemStack itemStack) {
        List<ItemStack> notRemovedItems = new ArrayList<>();

        if (inventories.containsKey(page)) {
            StorageInventory storageInventory = inventories.get(page);
            Inventory inventory = storageInventory.getInventory();
            ItemStack currentItem = inventory.getItem(slot);

            if (currentItem != null && currentItem.isSimilar(itemStack)) {
                int currentAmount = currentItem.getAmount();
                if (currentAmount > itemStack.getAmount()) {
                    currentItem.setAmount(currentAmount - itemStack.getAmount());
                    inventory.setItem(slot, currentItem);
                } else if (currentAmount == itemStack.getAmount()) {
                    inventory.setItem(slot, null);
                } else {
                    ItemStack notRemovedItem = itemStack.clone();
                    notRemovedItem.setAmount(itemStack.getAmount() - currentAmount);
                    notRemovedItems.add(notRemovedItem);
                }
            } else {
                notRemovedItems.add(itemStack);
            }
        } else {
            ItemStack[] contents = items.get(page);
            if (contents != null) {
                ItemStack currentItem = contents[slot];

                if (currentItem != null && currentItem.isSimilar(itemStack)) {
                    int currentAmount = currentItem.getAmount();
                    if (currentAmount > itemStack.getAmount()) {
                        currentItem.setAmount(currentAmount - itemStack.getAmount());
                        contents[slot] = currentItem;
                    } else if (currentAmount == itemStack.getAmount()) {
                        contents[slot] = null;
                    } else {
                        ItemStack notRemovedItem = itemStack.clone();
                        notRemovedItem.setAmount(itemStack.getAmount() - currentAmount);
                        notRemovedItems.add(notRemovedItem);
                    }
                } else {
                    notRemovedItems.add(itemStack);
                }
            }
        }

        return notRemovedItems;
    }


    /**
     * Clears a specific slot on a given page, with restrictions.
     *
     * @param page The page number where the slot is located.
     * @param slot The slot number to be cleared on the page.
     */
    public void clearSlotWithRestrictions(int page, int slot) {
        if (isItemInterfaceSlot(page, slot, getStorageConfig()) && !PlaceholderItemInterface.isPlaceholderItem(getItem(slot, page)))
            return;
        clearSlotPage(page, slot);
    }

    /**
     * Clears the slot on a specific page in the inventory.
     * If the page exists, the slot on that page is cleared.
     * If the slot contains an item interface, the item interface item is set back in the slot if enabled in the config.
     *
     * @param page The page index of the inventory.
     * @param slot The slot index to clear.
     */
    public void clearSlotPage(int page, int slot) {
        if (inventories.containsKey(page)) {
            StorageInventory storageInventory = inventories.get(page);
            Inventory inventory = storageInventory.getInventory();
            inventory.clear(slot);
            StorageConfig storageConfig = getStorageConfig();
            if (isItemInterfaceSlot(page, slot, storageConfig)) {
                if (storageConfig.isStorageItemsInterfaceEnabled()) {
                    getStorageItemInterfaceConfig(page, slot).ifPresent(storageItemInterfaceConfig -> {
                        inventory.setItem(slot, storageItemInterfaceConfig.getItemInterface().getItemStack());
                    });
                }
            }
        } else {
            ItemStack[] contents = items.getOrDefault(page, null);
            if (contents != null) {
                contents[slot] = null;
            }
        }
    }

    /**
     * Retrieves the StorageItemInterfaceConfig associated with the given page and slot.
     *
     * @param page The page number to search for.
     * @param slot The slot number to search for.
     * @return An Optional object containing the StorageItemInterfaceConfig, or an empty Optional if not found.
     */
    public Optional<StorageItemInterfaceConfig> getStorageItemInterfaceConfig(int page, int slot) {
        if (!getStorageConfig().getStorageItemsInterfaceConfig().containsKey(page)) return Optional.empty();
        if (!getStorageConfig().getStorageItemsInterfaceConfig().get(page).containsKey(slot)) return Optional.empty();
        return Optional.of(getStorageConfig().getStorageItemsInterfaceConfig().get(page).get(slot));
    }

    /**
     * Drops an item at the specified location.
     *
     * @param item   the item to be dropped
     * @param loc    the location at which the item should be dropped
     * @param sync   whether the drop should be synchronized or asynchronous
     * @param remove whether to remove the item with restrictions
     */
    public void dropItem(StorageItemDataInfo item, Location loc, boolean sync, boolean remove) {
        if (remove) item.removeWithRestrictions();
        Runnable runnable = () -> {
            loc.getWorld().dropItem(loc, item.getItemStack());
        };
        if (!sync) runnable.run();
        else Bukkit.getScheduler().runTask(StorageMechanic.getInstance(), runnable);
    }

    /**
     * Drops an item from storage at the specified location in the world.
     *
     * @param item The storage item data information.
     * @param loc  The location where the item will be dropped.
     */
    public void dropItem(StorageItemDataInfo item, Location loc) {
        loc.getWorld().dropItem(loc, item.getItemStack());
    }

    /**
     * Drops a list of items at the specified location.
     *
     * @param items The list of StorageItemDataInfo objects to drop.
     * @param loc   The location where the items will be dropped.
     */
    public void dropItems(List<StorageItemDataInfo> items, Location loc) {
        for (StorageItemDataInfo item : items) {
            dropItem(item, loc);
        }
    }

    public void dropItems(List<StorageItemDataInfo> items, Location loc, boolean sync, boolean remove) {
        if (remove) items.forEach(StorageItemDataInfo::removeWithRestrictions);
        Runnable runnable = () -> {
            for (StorageItemDataInfo item : items) {
                dropItem(item, loc);
            }
        };
        if (!sync) runnable.run();
        else Bukkit.getScheduler().runTask(StorageMechanic.getInstance(), runnable);
    }

    public void dropItemsFromPage(Location dropLocation, int page) {
        List<StorageItemDataInfo> itemsList = getItemsFromPage(page);
        World world = dropLocation.getWorld();
        for (StorageItemDataInfo item : itemsList) {
            if(!item.exists()) continue;
            item.removeWithRestrictions();
            if (PlaceholderItemInterface.isPlaceholderItem(item.getItemStack())) {
                new ItemBuilder(item.getItemStack()).meta(meta -> {
                    PersistentDataContainer itemPersistentDataContainer = meta.getPersistentDataContainer();
                    itemPersistentDataContainer.remove(PlaceholderItemInterface.NAMESPACED_KEY_PLACEHOLDER);
                    itemPersistentDataContainer.remove(ItemInterfaceManager.NAMESPACED_KEY);
                }).build();
            }
            world.dropItem(dropLocation, item.getItemStack());
        }
    }

    public StorageBlockItemConfig getStorageBlockItemConfig(int slot, int page){
        StorageConfig storageConfig = getStorageConfig();
        for (StorageBlockItemConfig storageBlockItemConfig : storageConfig.getStorageBlockedItemsConfig()) {
            if (!storageBlockItemConfig.getPagesToSlots().containsKey(page)) {
                continue;
            }

            Set<Integer> slots = storageBlockItemConfig.getPagesToSlots().get(page);

            if (slots != null) {
                if (slots.contains(slot)) {
                    return storageBlockItemConfig;
                }
            }
        }
        return null;
    }

    public boolean isBlocked(int slot, int page) { //1: true or false 2:messsage
        StorageConfig storageConfig = getStorageConfig();
        if (!storageConfig.isStorageBlockItemEnabled()) {
            return false;
        }
        return getStorageBlockItemConfig(slot, page) != null;
    }

    /**
     * Determines whether an item can be placed in the storage at the specified page and slot.
     *
     * @param item          The item to be placed.
     * @param page          The page of the storage.
     * @param slot          The slot on the page.
     * @param storageConfig The configuration of the storage.
     * @return True if the item can be placed, false otherwise.
     */
    public boolean canBePlaced(ItemStack item, int page, int slot, StorageConfig storageConfig) {
        return (!isItemInterfaceSlot(page, slot, storageConfig) && !((boolean) isBlocked(slot, page)) && !isItemInList(item, slot, page, ListType.BLACKLIST, storageConfig) && isItemInList(item, slot, page, ListType.WHITELIST, storageConfig));
    }

    public boolean isItemInterfaceSlot(int page, int slot, StorageConfig storageConfig) {
        if (!storageConfig.getStorageItemsInterfaceConfig().containsKey(page)) return false;
        if (getStorageInventory(page) != null && getStorageInventory(page).getCurrentStage() != null) {
            StageStorage stage = getStorageInventory(page).getCurrentStage();
            if (stage.getStorageItemsInterfaceConfig().containsKey(page)) {
                HashMap<Integer, StorageItemInterfaceConfig> hashMap = stage.getStorageItemsInterfaceConfig().get(page);
                return hashMap.containsKey(slot);
            }
        }
        HashMap<Integer, StorageItemInterfaceConfig> hashMap = storageConfig.getStorageItemsInterfaceConfig().get(page);
        return hashMap.containsKey(slot);
    }

    public boolean isItemInList(ItemStack itemStack, int slot, int page, ListType listType, StorageConfig storageConfig) {
        String itemId = Adapter.getAdapterId(itemStack);
        switch (listType) {

            case BLACKLIST -> {
                if (!storageConfig.isStorageItemsBlackListEnabled()) return false;
                for (StorageItemConfig storageItemConfig : storageConfig.getStorageItemsBlackListConfig()) {

                    if (!storageItemConfig.getPagesToSlots().containsKey(page)) {
                        continue;
                    }

                    Set<Integer> slots = storageItemConfig.getPagesToSlots().get(page);
                    if (slots != null) {
                        if (!slots.contains(slot)) {
                            continue;
                        }
                    }

                    Set<String> items = storageItemConfig.getItemsList();
                    if (items.contains(itemId)) return true;


                }
                return false;
            }
            case WHITELIST -> {
                if (!storageConfig.isStorageItemsWhiteListEnabled()) return true;
                for (StorageItemConfig storageItemConfig : storageConfig.getStorageItemsWhiteListConfig()) {
                    if (!storageItemConfig.getPagesToSlots().containsKey(page)) {
                        continue;
                    }

                    Set<Integer> slots = storageItemConfig.getPagesToSlots().get(page);
                    if (slots != null) {
                        if (!slots.contains(slot)) {
                            continue;
                        }
                    }

                    Set<String> items = storageItemConfig.getItemsList();
                    if (!items.contains(itemId)) return false;

                }
                return true;
            }
        }
        return false;
    }

    /**
     * Searches for storage items that match the given filter.
     *
     * @param filter The filter to apply when searching for items. The filter is a function that takes three parameters: the page number (Integer), the slot number (Integer), and
     *               the item stack (ItemStack). It should return true if the item matches the filter, and false otherwise.
     * @return A list of StorageItemDataInfo objects that match the filter.
     */
    public List<StorageItemDataInfo> searchItems(TriFunction<Integer, Integer, ItemStack, Boolean> filter) {
        List<StorageItemDataInfo> list = new ArrayList<>();
        for (int i = 0; i < getTotalPages(); i++) {
            HashMap<Integer, ItemStack> pageItemsMap = getMapItemsFromPage(i);
            for (Map.Entry<Integer, ItemStack> entry : pageItemsMap.entrySet()) {
                if (entry.getValue().getType().equals(Material.AIR)) continue;
                if (filter.apply(i, entry.getKey(), entry.getValue())) {
                    list.add(new StorageItemDataInfo(entry.getValue(), i, entry.getKey(), this));
                }
            }
        }
        return list;
    }


    public List<StorageItemDataInfo> searchItemsByName(String s, boolean exact) {
        s = s.toUpperCase(Locale.ENGLISH).trim();
        List<StorageItemDataInfo> list = new ArrayList<>();
        if (s.isEmpty()) return list;
        for (int i = 0; i < getTotalPages(); i++) {
            HashMap<Integer, ItemStack> pageItemsMap = getMapItemsFromPage(i);
            for (Map.Entry<Integer, ItemStack> entry : pageItemsMap.entrySet()) {
                if (entry.getValue().getType().equals(Material.AIR)) continue;
                String name = entry.getValue().getItemMeta().getDisplayName() != null ? entry.getValue().getItemMeta().getDisplayName().trim() : null;
                if (name == null || name.isEmpty()) name = entry.getValue().getType().toString();
                name = name.toUpperCase(Locale.ENGLISH);
                if (exact) {
                    if (name.equals(s)) {
                        list.add(new StorageItemDataInfo(entry.getValue(), i, entry.getKey(), this));
                    }
                } else {
                    if (name.contains(s)) {
                        list.add(new StorageItemDataInfo(entry.getValue(), i, entry.getKey(), this));
                    }
                }
            }
        }
        return list;
    }

    public List<StorageItemDataInfo> searchItemsByMaterial(String s, boolean exact) {
        s = s.toUpperCase(Locale.ENGLISH).trim();
        List<StorageItemDataInfo> list = new ArrayList<>();
        if (s.isEmpty()) return list;
        for (int i = 0; i < getTotalPages(); i++) {
            HashMap<Integer, ItemStack> pageItemsMap = getMapItemsFromPage(i);
            for (Map.Entry<Integer, ItemStack> entry : pageItemsMap.entrySet()) {
                if (entry.getValue().getType().equals(Material.AIR)) continue;
                if (exact) {
                    if (entry.getValue().getType().toString().toUpperCase(Locale.ENGLISH).equals(s)) {
                        list.add(new StorageItemDataInfo(entry.getValue(), i, entry.getKey(), this));
                    }
                } else {
                    if (entry.getValue().getType().toString().toUpperCase(Locale.ENGLISH).contains(s)) {
                        list.add(new StorageItemDataInfo(entry.getValue(), i, entry.getKey(), this));
                    }
                }
            }
        }
        return list;
    }

    public List<StorageItemDataInfo> searchItemsByAdapterId(String s, boolean exact) {
        s = s.toUpperCase(Locale.ENGLISH).trim();
        List<StorageItemDataInfo> list = new ArrayList<>();
        if (s.isEmpty()) return list;
        for (int i = 0; i < getTotalPages(); i++) {
            HashMap<Integer, ItemStack> pageItemsMap = getMapItemsFromPage(i);
            for (Map.Entry<Integer, ItemStack> entry : pageItemsMap.entrySet()) {
                if (entry.getValue().getType().equals(Material.AIR)) continue;
                String adapterId = Adapter.getAdvancedAdapterId(entry.getValue()).toUpperCase(Locale.ENGLISH);
                if (adapterId.contains(s)) {
                    list.add(new StorageItemDataInfo(entry.getValue(), i, entry.getKey(), this));
                }
            }
        }
        return list;
    }


    public void dropAllItems(Location dropLocation) {
        World world = dropLocation.getWorld();
        for (int p = 0; p < getTotalPages(); p++) {
            dropItemsFromPage(dropLocation, p);
        }
    }

    public List<ItemStack> addItemStackAtSlot(int page, int slot, ItemStack itemStack) {
        List<ItemStack> notAddedItems = new ArrayList<>();

        if (inventories.containsKey(page)) {
            StorageInventory storageInventory = inventories.get(page);
            Inventory inventory = storageInventory.getInventory();
            ItemStack currentItem = inventory.getItem(slot);

            if (currentItem == null || currentItem.getAmount() == 0) {
                inventory.setItem(slot, itemStack);
            } else if (currentItem.isSimilar(itemStack)) {
                int currentAmount = currentItem.getAmount();
                int maxAmount = currentItem.getMaxStackSize();
                int totalAmount = currentAmount + itemStack.getAmount();

                if (totalAmount <= maxAmount) {
                    currentItem.setAmount(totalAmount);
                    inventory.setItem(slot, currentItem);
                } else {
                    currentItem.setAmount(maxAmount);
                    inventory.setItem(slot, currentItem);
                    ItemStack notAddedItem = itemStack.clone();
                    notAddedItem.setAmount(totalAmount - maxAmount);
                    notAddedItems.add(notAddedItem);
                }
            } else {
                notAddedItems.add(itemStack);
            }
        } else {
            ItemStack[] contents = items.get(page);
            if (contents == null) {
                int slots = getStorageConfig().getInventoryType().getSize();
                contents = new ItemStack[slots];
                items.put(page, contents);
            }

            ItemStack currentItem = contents[slot];

            if (currentItem == null || currentItem.getAmount() == 0) {
                contents[slot] = itemStack.clone();
            } else if (currentItem.isSimilar(itemStack)) {
                int currentAmount = currentItem.getAmount();
                int maxAmount = currentItem.getMaxStackSize();
                int totalAmount = currentAmount + itemStack.getAmount();

                if (totalAmount <= maxAmount) {
                    currentItem.setAmount(totalAmount);
                    contents[slot] = currentItem;
                } else {
                    currentItem.setAmount(maxAmount);
                    contents[slot] = currentItem;
                    ItemStack notAddedItem = itemStack.clone();
                    notAddedItem.setAmount(totalAmount - maxAmount);
                    notAddedItems.add(notAddedItem);
                }
            } else {
                notAddedItems.add(itemStack);
            }
        }

        return notAddedItems;
    }

    public List<ItemStack> addItemStackToAllPages(ItemStack itemStack) {
        List<ItemStack> noAddedItems = new ArrayList<>();
        noAddedItems.add(itemStack);
        for (int i = 0; i < getTotalPages(); i++) {
            List<ItemStack> noAddedPage = new ArrayList<>();
            while (!noAddedItems.isEmpty()) {
                List<ItemStack> noAdded = addItemStack(i, noAddedItems.get(0));
                noAddedItems.remove(0);
                if (noAdded.size() > 0) noAddedPage.addAll(noAdded);
            }
            if (noAddedPage.size() == 0) break;
            noAddedItems.addAll(noAddedPage);
        }
        return noAddedItems;
    }

    public List<ItemStack> addItemStackToAllPagesWithRestrictions(ItemStack itemStack) {
        List<ItemStack> noAddedItems = new ArrayList<>();
        noAddedItems.add(itemStack);
        for (int i = 0; i < getTotalPages(); i++) {
            List<ItemStack> noAddedPage = new ArrayList<>();
            while (!noAddedItems.isEmpty()) {
                List<ItemStack> noAdded = addItemStackWithRestrictions(i, noAddedItems.get(0));
                noAddedItems.remove(0);
                if (noAdded.size() > 0) noAddedPage.addAll(noAdded);
            }
            if (noAddedPage.size() == 0) break;
            noAddedItems.addAll(noAddedPage);
        }
        return noAddedItems;
    }

    public void removeItemStackFromAllPages(ItemStack itemStack) {
        for (int i = 0; i < getTotalPages(); i++) {
            removeItemStack(i, itemStack);
        }
    }

    public int getTotalPages() {
        StorageConfig storageConfig = getStorageConfig();
        return storageConfig.getPages();
    }

    public void removeAllItems() {
        items.clear();
        for (Integer key : inventories.keySet()) {
            StorageInventory storageInventory = inventories.get(key);
            storageInventory.getInventory().clear();
        }
    }

    public boolean isEmpty() {
        for (Integer page : items.keySet()) {
            if (inventories.containsKey(page)) {
                StorageInventory storageInventory = inventories.get(page);
                if (!storageInventory.getInventory().isEmpty()) {
                    return false;
                }
            } else {
                ItemStack[] contents = items.get(page);
                for (ItemStack itemStack : contents) {
                    if (itemStack != null && itemStack.getAmount() > 0) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public int getItemCount(ItemStack itemStackToCount) {
        int totalCount = 0;
        for (ItemStack[] contents : items.values()) {
            for (ItemStack itemStack : contents) {
                if (itemStack != null && itemStack.isSimilar(itemStackToCount)) {
                    totalCount += itemStack.getAmount();
                }
            }
        }
        return totalCount;
    }

    public boolean containsItem(ItemStack itemStackToFind) {
        for (ItemStack[] contents : items.values()) {
            for (ItemStack itemStack : contents) {
                if (itemStack != null && itemStack.isSimilar(itemStackToFind)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void copyPage(int sourcePage, int destinationPage) {
        if (items.containsKey(sourcePage)) {
            ItemStack[] sourceContents = items.get(sourcePage);
            ItemStack[] destinationContents = new ItemStack[sourceContents.length];
            for (int i = 0; i < sourceContents.length; i++) {
                if (sourceContents[i] != null) {
                    destinationContents[i] = sourceContents[i].clone();
                }
            }
            items.put(destinationPage, destinationContents);
        }
    }

    public void movePage(int sourcePage, int destinationPage) {
        if (items.containsKey(sourcePage)) {
            items.put(destinationPage, items.get(sourcePage));
            items.remove(sourcePage);
        }
    }

    public boolean hasSpaceForItem(ItemStack itemStackToCheck) {
        int totalFreeSpace = 0;
        for (ItemStack[] contents : items.values()) {
            for (ItemStack itemStack : contents) {
                if (itemStack == null || itemStack.getAmount() == 0) {
                    totalFreeSpace += itemStackToCheck.getMaxStackSize();
                } else if (itemStack.isSimilar(itemStackToCheck)) {
                    totalFreeSpace += (itemStack.getMaxStackSize() - itemStack.getAmount());
                }
            }
        }
        return totalFreeSpace >= itemStackToCheck.getAmount();
    }

    public int getFreeSlotsCount() {
        int freeSlots = 0;
        for (ItemStack[] contents : items.values()) {
            for (ItemStack itemStack : contents) {
                if (itemStack == null || itemStack.getAmount() == 0) {
                    freeSlots++;
                }
            }
        }
        return freeSlots;
    }

    public int getTotalSlots() {
        StorageConfig storageConfig = getStorageConfig();
        int slotsPerPage = storageConfig.getInventoryType().getSize();
        if (storageConfig.getInventoryType().equals(StorageInventoryTypeConfig.CHEST)) {
            slotsPerPage = (storageConfig.getRows() * 9);
        }
        return slotsPerPage * getTotalPages();
    }

    public int getOccupiedSlotsCount() {
        int occupiedSlots = 0;
        for (ItemStack[] contents : items.values()) {
            for (ItemStack itemStack : contents) {
                if (itemStack != null && itemStack.getAmount() > 0) {
                    occupiedSlots++;
                }
            }
        }
        return occupiedSlots;
    }

    public List<HumanEntity> getViewersFromPage(int page) {
        if (inventories.containsKey(page)) {
            return new ArrayList<>(inventories.get(page).getInventory().getViewers());
        }
        return new ArrayList<>();
    }

    public List<HumanEntity> getAllViewers() {
        List<HumanEntity> allViewers = new ArrayList<>();
        for (StorageInventory inventory : inventories.values()) {
            allViewers.addAll(inventory.getInventory().getViewers());
        }
        return allViewers;
    }

    public void setItemInSlotPage(int page, int slot, ItemStack item) {
        if (inventories.containsKey(page)) {
            StorageInventory storageInventory = inventories.get(page);
            storageInventory.getInventory().setItem(slot, item);
        }

        if (items.containsKey(page)) {
            ItemStack[] contents = items.get(page);
            contents[slot] = item;
        }
    }

    public ItemStack getItem(int slot, int page) {
        if (inventories.containsKey(page)) {
            StorageInventory storageInventory = inventories.get(page);
            return storageInventory.getInventory().getItem(slot);
        }

        if (items.containsKey(page)) {
            ItemStack[] contents = items.get(page);
            return contents[slot];
        }
        return null;
    }

    public ItemStack[] getItemsFromPageInventory(int page) {
        if (inventories.containsKey(page)) {
            StorageInventory storageInventory = inventories.get(page);
            ItemStack[] contents = storageInventory.getInventory().getContents();
            return contents;
        }

        if (items.containsKey(page)) {
            ItemStack[] contents = items.get(page);
            return contents;
        }

        return null;
    }

    public ItemStack[] getItemsFromPageStorage(int page) {
        if (items.containsKey(page)) {
            ItemStack[] contents = items.get(page);
            return contents;
        }
        return null;
    }

    public ItemStack[] getItemsFromPageSlots(int page) {
        if (inventories.containsKey(page)) {
            StorageInventory storageInventory = inventories.get(page);
            ItemStack[] contents = storageInventory.getInventory().getContents();
            return contents;
        }

        if (items.containsKey(page)) {
            ItemStack[] contents = items.get(page);
            return contents;
        }

        return null;
    }

    public List<StorageItemDataInfo> getItemsFromPage(int page) {
        List<StorageItemDataInfo> itemsList = new ArrayList<>();

        if (inventories.containsKey(page)) {
            StorageInventory storageInventory = inventories.get(page);
            ItemStack[] contents = storageInventory.getInventory().getContents();
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] != null && !contents[i].getType().isAir()) {
                    if(core.getManagers().getItemInterfaceManager().isItemInterface(contents[i]) && PlaceholderItemInterface.isPlaceholderItem(contents[i])){
                        itemsList.add(new StorageItemDataInfo(PlaceholderItemInterface.getOriginalItemStack(contents[i]), page, i, this));
                        continue;
                    }
                    else if (!core.getManagers().getItemInterfaceManager().isItemInterface(contents[i])) {
                        itemsList.add(new StorageItemDataInfo(contents[i], page, i, this));
                    }
                }
            }
        }

        if (items.containsKey(page)) {
            ItemStack[] contents = items.get(page);
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] != null) {
                    if(PlaceholderItemInterface.isPlaceholderItem(contents[i])){
                        itemsList.add(new StorageItemDataInfo(PlaceholderItemInterface.getOriginalItemStack(contents[i]), page, i, this));
                        continue;
                    }
                    itemsList.add(new StorageItemDataInfo(contents[i], page, i, this));
                }
            }
        }

        return itemsList;
    }

    public HashMap<Integer, ItemStack> getMapItemsFromPage(int page) {
        HashMap<Integer, ItemStack> itemsList = new HashMap<>();

        if (inventories.containsKey(page)) {
            StorageInventory storageInventory = inventories.get(page);
            ItemStack[] contents = storageInventory.getInventory().getContents();
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] != null) {
                    if (PlaceholderItemInterface.isPlaceholderItem(contents[i])) {
                        ItemStack item = ItemBuilder.copyOf(contents[i]).meta(meta -> {
                            PersistentDataContainer itemPersistentDataContainer = meta.getPersistentDataContainer();
                            itemPersistentDataContainer.remove(PlaceholderItemInterface.NAMESPACED_KEY_PLACEHOLDER);
                            itemPersistentDataContainer.remove(ItemInterfaceManager.NAMESPACED_KEY);
                        }).build();
                        itemsList.put(i, item);
                        continue;
                    }
                    if (!core.getManagers().getItemInterfaceManager().isItemInterface(contents[i])) {
                        itemsList.put(i, contents[i]);
                    }
                }
            }
        }

        if (items.containsKey(page)) {
            ItemStack[] contents = items.get(page);
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] != null) {
                    if (PlaceholderItemInterface.isPlaceholderItem(contents[i])) {
                        ItemStack item = ItemBuilder.copyOf(contents[i]).meta(meta -> {
                            PersistentDataContainer itemPersistentDataContainer = meta.getPersistentDataContainer();
                            itemPersistentDataContainer.remove(PlaceholderItemInterface.NAMESPACED_KEY_PLACEHOLDER);
                            itemPersistentDataContainer.remove(ItemInterfaceManager.NAMESPACED_KEY);
                        }).build();
                        itemsList.put(i, item);
                        continue;
                    }
                    itemsList.put(i, contents[i]);
                }
            }
        }

        return itemsList;
    }

    public StorageItemDataInfo getFirstItemStack() {
        for (int i = 0; i < getTotalPages(); i++) {
            if (inventories.containsKey(i)) {
                StorageInventory storageInventory = inventories.get(i);
                ItemStack[] contents = storageInventory.getInventory().getContents();
                for (int k = 0; k < contents.length; k++) {
                    ItemStack item = contents[k];
                    if (item != null && !item.getType().isAir() && !core.getManagers().getItemInterfaceManager().isItemInterface(item)) {
                        return new StorageItemDataInfo(item, i, k, this);
                    }
                }
            } else if (items.containsKey(i)) {
                ItemStack[] contents = items.get(i);
                for (int k = 0; k < contents.length; k++) {
                    ItemStack item = contents[k];
                    if (item != null && !item.getType().isAir()) {
                        return new StorageItemDataInfo(item, i, k, this);
                    }
                }
            }
        }
        return null;
    }

    public StorageItemDataInfo getFirstItemStackSimilar(ItemStack similar) {
        for (int i = 0; i < getTotalPages(); i++) {
            if (inventories.containsKey(i)) {
                StorageInventory storageInventory = inventories.get(i);
                ItemStack[] contents = storageInventory.getInventory().getContents();
                for (int k = 0; k < contents.length; k++) {
                    ItemStack item = contents[k];
                    if (item != null && !item.getType().isAir() && item.isSimilar(similar))
                        return new StorageItemDataInfo(item, i, k, this);
                }
            } else if (items.containsKey(i)) {
                ItemStack[] contents = items.get(i);
                for (int k = 0; k < contents.length; k++) {
                    ItemStack item = contents[k];
                    if (item != null && !item.getType().isAir() && item.isSimilar(similar))
                        return new StorageItemDataInfo(item, i, k, this);
                }
            }
        }
        return null;
    }

    public int firstSlotEmptyPage(int page) {
        if (inventories.containsKey(page)) {
            StorageInventory storageInventory = inventories.get(page);
            return storageInventory.getInventory().firstEmpty();
        } else if (items.containsKey(page)) {
            ItemStack[] contents = items.get(page);
            for (int k = 0; k < contents.length; k++) {
                ItemStack item = contents[k];
                if (item == null || item.getType().equals(Material.AIR)) return k;
            }
            return -1;
        }
        return -1;
    }

    public int firstSlotEmpty() {
        for (int i = 0; i < getTotalPages(); i++) {
            int num = firstSlotEmptyPage(i);
            if (num == -1) continue;
            return num;
        }
        return -1;
    }

    public List<StorageItemDataInfo> getAllItems() {
        List<StorageItemDataInfo> allItems = new ArrayList<>();

        for (int page : items.keySet()) {
            allItems.addAll(getItemsFromPage(page));
        }

        return allItems;
    }

    public void removeAllItemsOfType(ItemStack itemStackToRemove) {
        for (int i = 0; i < getTotalPages(); i++) {
            removeItemStack(i, itemStackToRemove);
        }
    }

    public List<ItemStack> addItemStackListToAllPages(List<ItemStack> itemStackList) {
        List<ItemStack> noAddedItems = new ArrayList<>();
        for (ItemStack itemStack : itemStackList) {
            List<ItemStack> noAdded = addItemStackToAllPages(itemStack);
            if (noAdded.size() == 0) break;
            noAddedItems.addAll(noAdded);
        }
        return noAddedItems;
    }

    public void removeItemStackListFromAllPages(List<ItemStack> itemStackList) {
        for (ItemStack itemStack : itemStackList) {
            removeItemStackFromAllPages(itemStack);
        }
    }

    public boolean containsAtLeast(ItemStack itemStackToCheck, int amount) {
        int itemCount = getItemCount(itemStackToCheck);
        return itemCount >= amount;
    }

    public void addItemStackFromInventory(Inventory inventory) {
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack != null && itemStack.getAmount() > 0) {
                addItemStackToAllPages(itemStack);
            }
        }
    }

    public void removeItemStackFromInventory(Inventory inventory) {
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack != null && itemStack.getAmount() > 0) {
                removeItemStackFromAllPages(itemStack);
            }
        }
    }


    public void removeAmountFromList(List<StorageItemDataInfo> l, int amountToRemove) {
        if (amountToRemove <= 0) return;
        Iterator<StorageItemDataInfo> iterator = l.iterator();
        while (iterator.hasNext()) {
            StorageItemDataInfo storageIData = iterator.next();
            int itemAmount = storageIData.getItemStack().getAmount();
            int remaining = itemAmount - amountToRemove;
            if (remaining >= 0) {
                storageIData.getItemStack().setAmount(remaining);
                break;
            }
            iterator.remove();
            amountToRemove = Math.abs(remaining);
        }
    }


    public int getTotalAmountFromList(List<StorageItemDataInfo> l) {
        if (l.size() == 0) return -1;
        int totalAmount = 0;
        for (StorageItemDataInfo storageIData : l) {
            totalAmount += storageIData.getItemStack().getAmount();
        }
        return totalAmount;
    }

    public List<StorageItemDataInfo> getAllItemsSimilarFromAllPages(ItemStack similar) {
        List<StorageItemDataInfo> list = new ArrayList<>();
        for (int i = 0; i < getTotalPages(); i++) {
            list.addAll(getAllItemsSimilarFromPage(i, similar));
        }
        return list;
    }

    public List<StorageItemDataInfo> getAllItemsSimilarFromPage(int page, ItemStack similar) {
        List<StorageItemDataInfo> list = new ArrayList<>();
        if (inventories.containsKey(page)) {
            StorageInventory storageInventory = inventories.get(page);
            ItemStack[] contents = storageInventory.getInventory().getContents();
            for (int k = 0; k < contents.length; k++) {
                ItemStack item = contents[k];
                if (item != null && !item.getType().isAir() && item.isSimilar(similar))
                    list.add(new StorageItemDataInfo(item, page, k, this));
            }
        }
        if (items.containsKey(page)) {
            ItemStack[] contents = items.get(page);
            for (int k = 0; k < contents.length; k++) {
                ItemStack item = contents[k];
                if (item != null && !item.getType().isAir() && item.isSimilar(similar))
                    list.add(new StorageItemDataInfo(item, page, k, this));
            }
        }
        return list;
    }

    public StorageConfig getStorageConfig() {
        return StorageMechanic.getInstance().getManagers().getStorageConfigManager().getStorageConfigById(storageIdConfig);
    }


    public Map<Integer, StorageInventory> getInventories() {
        return inventories;
    }

    public Map<Integer, ItemStack[]> getItems() {
        return items;
    }

    public String getId() {
        return id;
    }

    public String getStorageIdConfig() {
        return storageIdConfig;
    }

    public Date getCreationDate() {
        return date;
    }

    public StorageMechanic getCore() {
        return core;
    }

    public StorageOriginContext getStorageOriginContext() {
        return storageOriginContext;
    }

    public enum ListType {
        WHITELIST,
        BLACKLIST
    }

    public Date getLastOpen() {
        return lastOpen;
    }

    public void setLastAccess(Date lastAccess) {
        this.lastAccess = lastAccess;
    }

    public Date getLastAccess() {
        return lastAccess;
    }

    public StorageInventory getStorageInventory(int page) {
        return inventories.get(page);
    }

    public void setStorageInventory(int page, StorageInventory storageInventory) {
        inventories.put(page, storageInventory);
    }

    public void removeStorageInventory(int page) {
        inventories.remove(page);
    }

    public boolean existsStorageInventory(int page) {
        return inventories.containsKey(page);
    }
}
