package dev.wuason.storagemechanic.storages;

import dev.wuason.mechanics.utils.Adapter;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.items.ItemInterfaceManager;
import dev.wuason.storagemechanic.storages.config.StorageConfig;
import dev.wuason.storagemechanic.storages.config.StorageInventoryTypeConfig;
import dev.wuason.storagemechanic.storages.config.StorageItemConfig;
import dev.wuason.storagemechanic.storages.config.StorageItemInterfaceConfig;
import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.*;

public class Storage {
    private Map<Integer, StorageInventory> inventories = new HashMap<>();
    private Map<Integer,ItemStack[]> items = new HashMap<>();

    private String id;
    private String storageIdConfig;
    private Date date = null;

    public Storage(String storageIdConfig) {
        this.id = UUID.randomUUID().toString();
        this.storageIdConfig = storageIdConfig;
    }

    public Storage(String id, Map<Integer,ItemStack[]> items, String storageIdConfig, Date date) {
        this.id = id;
        this.items = items;
        this.storageIdConfig = storageIdConfig;
        this.date = date;
    }

    public void closeStorage(int page) {
        if (inventories.containsKey(page)) {
            StorageInventory storageInventory = inventories.get(page);
            if (storageInventory.getInventory().getViewers().size() <= 1) { // <= 1 because the current player is still in the viewer list
                ItemStack[] contents = storageInventory.getInventory().getContents();
                ItemInterfaceManager itemInterfaceManager = StorageMechanic.getInstance().getManagers().getItemInterfaceManager();

                for (int i = 0; i < contents.length; i++) {
                    ItemStack item = contents[i];
                    if (item != null && itemInterfaceManager.isItemInterface(item)) {
                        contents[i] = null;
                    }
                }

                items.put(page, contents);
                inventories.remove(page);
            }
        }
    }

    public void openStorage(Player player, int page) {

        StorageConfig storageConfig = getStorageConfig();

        if (!items.containsKey(page)) {
            int slots = storageConfig.getInventoryType().getSize();
            if(storageConfig.getInventoryType().equals(StorageInventoryTypeConfig.CHEST)){
                slots = (storageConfig.getRows() * 9);
            }
            items.put(page, new ItemStack[slots]); //SLOTS
        }

        if (!inventories.containsKey(page)) {
            StorageInventory inventory = StorageMechanic.getInstance().getManagers().getStorageInventoryManager().createStorageInventory(storageConfig,this,page);
            inventory.getInventory().setContents(items.get(page));
            if(date == null){
                if(storageConfig.isStorageItemsDefaultEnabled()){
                    for(StorageItemConfig itemDefault : storageConfig.getStorageItemsDefaultConfig()){
                        if(itemDefault.getPages().indexOf(page) != (-1)){
                            for(int s : itemDefault.getSlots()){
                                for(String item : itemDefault.getItems()){
                                    ItemStack itemStack = Adapter.getItemStack(item);
                                    itemStack.setAmount(itemDefault.getAmount());
                                    inventory.getInventory().setItem(s, itemStack);
                                }
                            }
                        }
                    }
                }
            }
            if(storageConfig.isStorageItemsInterfaceEnabled()){
                for(StorageItemInterfaceConfig itemInterface : storageConfig.getStorageItemsInterfaceConfig()){
                    if(itemInterface.getPages().contains(page)){
                        for(int s : itemInterface.getSlots()){
                            inventory.getInventory().setItem(s,itemInterface.getitemInterface().getItemStack());
                        }
                    }
                }
            }

            inventories.put(page, inventory);
        }
        if(date == null){
            date = new Date();
        }
        inventories.get(page).open(player);
    }

    public List<ItemStack> addItemStack(int page, ItemStack itemStack) {
        List<ItemStack> notAddedItems = new ArrayList<>();

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
    public void addItemStackToAllPages(ItemStack itemStack) {
        for (int i = 0; i < getTotalPages(); i++) {
            addItemStack(i, itemStack);
        }
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
        for (ItemStack[] contents : items.values()) {
            for (ItemStack itemStack : contents) {
                if (itemStack != null && itemStack.getAmount() > 0) {
                    return false;
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
    public List<ItemStack> getAllItems() {
        List<ItemStack> allItems = new ArrayList<>();
        for (ItemStack[] contents : items.values()) {
            for (ItemStack itemStack : contents) {
                if (itemStack != null && itemStack.getAmount() > 0) {
                    allItems.add(itemStack.clone());
                }
            }
        }
        return allItems;
    }
    public void removeAllItemsOfType(ItemStack itemStackToRemove) {
        for (int i = 0; i < getTotalPages(); i++) {
            removeItemStack(i, itemStackToRemove);
        }
    }
    public void addItemStackListToAllPages(List<ItemStack> itemStackList) {
        for (ItemStack itemStack : itemStackList) {
            addItemStackToAllPages(itemStack);
        }
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

    public StorageConfig getStorageConfig(){
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

    public Date getDate() {
        return date;
    }
}
