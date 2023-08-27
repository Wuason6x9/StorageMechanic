package dev.wuason.storagemechanic.storages;

import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.utils.MathUtils;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.api.events.storage.CloseStorageEvent;
import dev.wuason.storagemechanic.api.events.storage.OpenStorageEvent;
import dev.wuason.storagemechanic.compatibilities.MythicMobs;
import dev.wuason.storagemechanic.items.ItemInterfaceManager;
import dev.wuason.storagemechanic.storages.config.*;
import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import dev.wuason.storagemechanic.storages.types.entity.StorageTriggers;
import io.lumine.mythic.api.mobs.MobManager;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.mobs.MobExecutor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;

public class Storage {
    private Map<Integer, StorageInventory> inventories = new HashMap<>();
    private Map<Integer,ItemStack[]> items = new HashMap<>();

    private String id;
    private String storageIdConfig;
    private Date date = new Date();
    private StorageMechanic core;

    public Storage(String storageIdConfig) {
        this.id = UUID.randomUUID().toString();
        this.storageIdConfig = storageIdConfig;
        core = StorageMechanic.getInstance();
    }

    public Storage(String storageIdConfig, UUID id) {
        this.id = id.toString();
        this.storageIdConfig = storageIdConfig;
        core = StorageMechanic.getInstance();
    }

    public Storage(String id, Map<Integer,ItemStack[]> items, String storageIdConfig, Date date) {
        this.id = id;
        this.items = items;
        this.storageIdConfig = storageIdConfig;
        this.date = date;
        core = StorageMechanic.getInstance();
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


                if(getInventories().size()==0){
                    if(MythicMobs.isExistMythic()){
                        UUID uuid = null;
                        try {
                            uuid = UUID.fromString(id);
                            MobExecutor mobManager = MythicBukkit.inst().getMobManager();
                            if(mobManager.isActiveMob(uuid)){
                                ActiveMob activeMob = mobManager.getActiveMob(uuid).get();
                                MythicMob mythicMob = activeMob.getType();
                                core.getManagers().getMythicManager().runSkills(mythicMob.getInternalName(),activeMob, StorageTriggers.CLOSE_STORAGE, BukkitAdapter.adapt(storageInventory.getInventory().getViewers().get(0).getLocation()),BukkitAdapter.adapt((Player)storageInventory.getInventory().getViewers().get(0)),null);
                            }
                        }
                        catch (Exception e){
                        }
                    }
                }

                CloseStorageEvent closeStorageEvent = new CloseStorageEvent((Player)storageInventory.getInventory().getViewers().get(0),storageInventory);
                Bukkit.getPluginManager().callEvent(closeStorageEvent);
            }
        }

    }
    public void closeAllInventory(){
        while(!inventories.isEmpty()){
            StorageInventory storageInventory = (StorageInventory) (inventories.values().toArray())[0];
            while (!storageInventory.getInventory().getViewers().isEmpty()){
                storageInventory.getInventory().getViewers().get(0).closeInventory();
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

            ItemStack[] itemsInv = new ItemStack[slots]; //SLOTS

            if(storageConfig.isStorageItemsDefaultEnabled()){

                for(StorageItemConfig itemDefault : storageConfig.getStorageItemsDefaultConfig()){
                    if(!itemDefault.getPagesToSlots().containsKey(page)) continue;
                    for(String item : itemDefault.getItemsList()){
                        for(int s : itemDefault.getPagesToSlots().get(page)){
                            if(!MathUtils.chance(itemDefault.getChance())) continue;
                            ItemStack itemStack = Mechanics.getInstance().getManager().getAdapterManager().getItemStack(item);
                            itemStack.setAmount(MathUtils.randomNumberString(itemDefault.getAmount()));
                            itemsInv[s] = itemStack;

                        }

                    }

                }

            }

            items.put(page, itemsInv);
        }

        if (!inventories.containsKey(page)) {
            StorageInventory inventory = StorageMechanic.getInstance().getManagers().getStorageInventoryManager().createStorageInventory(storageConfig,this,page);
            inventory.getInventory().setContents(items.get(page));

            if(storageConfig.isStorageItemsInterfaceEnabled()){
                for(StorageItemInterfaceConfig itemInterface : storageConfig.getStorageItemsInterfaceConfig()){
                    if(itemInterface.getPagesToSlots().containsKey(page)){
                        for(int s : itemInterface.getPagesToSlots().get(page)){
                            inventory.getInventory().setItem(s,itemInterface.getItemInterface().getItemStack());
                        }
                    }
                }
            }

            inventories.put(page, inventory);
        }
        StorageInventory storageInventoryPage = inventories.get(page);
        storageInventoryPage.open(player);

        OpenStorageEvent openStorageEvent = new OpenStorageEvent(player,storageInventoryPage);
        Bukkit.getPluginManager().callEvent(openStorageEvent);
    }
    public void loadAllItemsDefault(){
        for (int p = 0; p < getTotalPages(); p++) {
            loadItemsDefault(p);
        }
        System.out.println("Loaded default Items! ItemsLoaded: " + getAllItems().size());
    }
    public void loadItemsDefault(int page){

        StorageConfig storageConfig = getStorageConfig();

        if(storageConfig.isStorageItemsDefaultEnabled()){

            if (!items.containsKey(page)) {

                int slots = storageConfig.getInventoryType().getSize();
                if(storageConfig.getInventoryType().equals(StorageInventoryTypeConfig.CHEST)){
                    slots = (storageConfig.getRows() * 9);
                }

                ItemStack[] itemsInv = new ItemStack[slots]; //SLOTS

                for(StorageItemConfig itemDefault : storageConfig.getStorageItemsDefaultConfig()){

                    if(!itemDefault.getPagesToSlots().containsKey(page)) continue;
                    for(ItemStack item : itemDefault.getItems()){
                        for(int s : itemDefault.getPagesToSlots().get(page)){
                            if(!MathUtils.chance(itemDefault.getChance())) continue;
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
    public void dropItemsFromPage(Location dropLocation, int page) {
        List<ItemStack> itemsList = getItemsFromPage(page);
        World world = dropLocation.getWorld();

        ArrayList<ItemStack> itemStacks = new ArrayList<>();
        for (ItemStack item : itemsList) {
            if (item != null) {
                itemStacks.add(item);
                removeItemStack(page, item);
            }
        }
        Bukkit.getScheduler().runTask(StorageMechanic.getInstance(),() -> {
            for(ItemStack item : itemStacks){
                world.dropItemNaturally(dropLocation, item);
            }
        });
    }
    public ArrayList<Object> isBlocked(int slot, int page){
        ArrayList<Object> objects = new ArrayList<>();
        StorageConfig storageConfig = getStorageConfig();
        for(StorageBlockItemConfig storageBlockItemConfig : storageConfig.getStorageBlockedItemsConfig()){
            if(!storageBlockItemConfig.getPagesToSlots().containsKey(page)){
                continue;
            }
            Set<Integer> slots = storageBlockItemConfig.getPagesToSlots().get(page);
            if(slots != null){
                if(slots.contains(slot)){
                    objects.add(true);
                    objects.add(storageBlockItemConfig.getMessage());
                    return objects;
                }
            }
        }
        objects.add(false);
        return objects;
    }
    public boolean isItemInList(ItemStack itemStack, int slot, int page, ListType listType){
        StorageConfig storageConfig = getStorageConfig();
        String itemId = Mechanics.getInstance().getManager().getAdapterManager().getAdapterID(itemStack);
        switch (listType){

            case BLACKLIST -> {
                for(StorageItemConfig storageItemConfig : storageConfig.getStorageItemsBlackListConfig()){

                    if(!storageItemConfig.getPagesToSlots().containsKey(page)){
                        continue;
                    }

                    Set<Integer> slots = storageItemConfig.getPagesToSlots().get(page);
                    if(slots != null){
                        if(!slots.contains(slot)){
                            continue;
                        }
                    }

                    Set<String> items = storageItemConfig.getItemsList();
                    if (items.contains(itemId)) return true;


                }
                return false;
            }
            case WHITELIST -> {

                for(StorageItemConfig storageItemConfig : storageConfig.getStorageItemsWhiteListConfig()){
                    if(!storageItemConfig.getPagesToSlots().containsKey(page)){
                        continue;
                    }

                    Set<Integer> slots = storageItemConfig.getPagesToSlots().get(page);
                    if(slots != null){
                        if(!slots.contains(slot)){
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


    public HashMap<Integer,HashMap<Integer,ItemStack>> searchItemsByName(String s){
        HashMap<Integer,HashMap<Integer,ItemStack>> mapPages = new HashMap<>();
        for(int i=0;i<items.size();i++){
            HashMap<Integer,ItemStack> mapSlots = new HashMap<>();
            for(Map.Entry<Integer,ItemStack> entry : getMapItemsFromPage(i).entrySet()){
                if(entry.getValue().equals(Material.AIR)) continue;
                String display = entry.getValue().getItemMeta().getDisplayName().toLowerCase(Locale.ENGLISH);
                if(display.equals("") || display == null) display = entry.getValue().getType().toString().toLowerCase();
                if(display.contains(s)){
                    mapSlots.put(entry.getKey(),entry.getValue());
                }
            }
            mapPages.put(i,mapSlots);
        }
        return mapPages;
    }

    public HashMap<Integer,HashMap<Integer,ItemStack>> searchItemsByMaterial(String s){
        HashMap<Integer,HashMap<Integer,ItemStack>> mapPages = new HashMap<>();
        for(int i=0;i<items.size();i++){
            HashMap<Integer,ItemStack> mapSlots = new HashMap<>();
            HashMap<Integer,ItemStack> pageItemsMap = getMapItemsFromPage(i);
            for(Map.Entry<Integer,ItemStack> entry : pageItemsMap.entrySet()){
                if(entry.getValue().getType().toString().toLowerCase(Locale.ENGLISH).contains(s)){
                    mapSlots.put(entry.getKey(),entry.getValue());
                }
            }
            mapPages.put(i,mapSlots);
        }
        return mapPages;
    }


    public void dropAllItems(Location dropLocation) {
        World world = dropLocation.getWorld();
        for (int p = 0; p < getTotalPages(); p++) {
            dropItemsFromPage(dropLocation,p);
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

    public ItemStack[] getItemsFromPageSlots(int page){
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

    public List<ItemStack> getItemsFromPage(int page) {
        List<ItemStack> itemsList = new ArrayList<>();

        if (inventories.containsKey(page)) {
            StorageInventory storageInventory = inventories.get(page);
            ItemStack[] contents = storageInventory.getInventory().getContents();
            for (ItemStack item : contents) {
                if (item != null) {
                    itemsList.add(item);
                }
            }
        }

        if (items.containsKey(page)) {
            ItemStack[] contents = items.get(page);
            for (ItemStack item : contents) {
                if (item != null) {
                    itemsList.add(item);
                }
            }
        }

        return itemsList;
    }

    public HashMap<Integer,ItemStack> getMapItemsFromPage(int page) {
        HashMap<Integer,ItemStack> itemsList = new HashMap<>();

        if (inventories.containsKey(page)) {
            StorageInventory storageInventory = inventories.get(page);
            ItemStack[] contents = storageInventory.getInventory().getContents();
            for(int i=0;i<contents.length;i++){
                if (contents[i] != null) {
                    itemsList.put(i,contents[i]);
                }
            }
        }

        if (items.containsKey(page)) {
            ItemStack[] contents = items.get(page);
            for(int i=0;i<contents.length;i++){
                if (contents[i] != null) {
                    itemsList.put(i,contents[i]);
                }
            }
        }

        return itemsList;
    }

    public List<ItemStack> getAllItems() {
        List<ItemStack> allItems = new ArrayList<>();

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

    public enum ListType{
        WHITELIST,
        BLACKLIST
    }

}
