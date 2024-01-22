package dev.wuason.storagemechanic.storages;

import dev.wuason.mechanics.compatibilities.adapter.Adapter;
import dev.wuason.mechanics.items.ItemBuilderMechanic;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.mechanics.utils.MathUtils;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.api.events.storage.CloseStorageEvent;
import dev.wuason.storagemechanic.api.events.storage.OpenStorageEvent;
import dev.wuason.storagemechanic.items.ItemInterfaceManager;
import dev.wuason.storagemechanic.items.items.PlaceholderItemInterface;
import dev.wuason.storagemechanic.storages.config.*;
import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class Storage {
    private Map<Integer, StorageInventory> inventories = new HashMap<>();
    private Map<Integer,ItemStack[]> items = new HashMap<>();

    private String id;
    private String storageIdConfig;
    private Date date = new Date();
    private Date lastOpen = new Date();
    private Date lastAccess = new Date();
    private StorageMechanic core;
    private StorageOriginContext storageOriginContext;
    private HashMap<Integer, StageStorage> currentStages = new HashMap<>();
    private boolean isTempStorage = false;

    public Storage(String storageIdConfig, StorageOriginContext storageOriginContext) {
        this.id = UUID.randomUUID().toString();
        this.storageIdConfig = storageIdConfig;
        core = StorageMechanic.getInstance();
        this.storageOriginContext = storageOriginContext;
    }

    public Storage(String storageIdConfig, UUID id, StorageOriginContext storageOriginContext) {
        this.id = id.toString();
        this.storageIdConfig = storageIdConfig;
        core = StorageMechanic.getInstance();
        this.storageOriginContext = storageOriginContext;
    }

    public Storage(String id, Map<Integer,ItemStack[]> items, String storageIdConfig, Date date, StorageOriginContext storageOriginContext, Date lastOpen) {
        this.id = id;
        this.items = items;
        this.storageIdConfig = storageIdConfig;
        this.date = date;
        core = StorageMechanic.getInstance();
        this.storageOriginContext = storageOriginContext;
        this.lastOpen = lastOpen != null ? lastOpen : new Date();
    }

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
                stopAnimationStages(page);
                inventories.remove(page);
                if(currentStages.containsKey(page)) currentStages.remove(page);
                if(getStorageConfig().getStorageProperties().isDropItemsPageOnClose()) dropItemsFromPage(player.getLocation(), page);



                //MYTHIC
                if(getInventories().size()==0){
                    if(core.getManagers().getMythicManager() != null){
                        core.getManagers().getMythicManager().executeCloseStorageSkill(storageOriginContext,id,storageInventory);
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

    public boolean openStorage(Player player, int page) {

        StorageConfig storageConfig = getStorageConfig();

        if(storageConfig.getMaxViewers() != -1){
            if(getAllViewers().size() >= storageConfig.getMaxViewers()){
                AdventureUtils.sendMessage(player, core.getManagers().getConfigManager().getLangDocumentYaml().getString("messages.storage.max_views"));
                return false;
            }
        }

        lastOpen = new Date();

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
                            ItemStack itemStack = Adapter.getInstance().getItemStack(item);
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
                if(storageConfig.getStorageItemsInterfaceConfig().containsKey(page)){
                    for(Map.Entry<Integer,StorageItemInterfaceConfig> entry : storageConfig.getStorageItemsInterfaceConfig().get(page).entrySet()){
                        if(PlaceholderItemInterface.isPlaceholderItem(inventory.getInventory().getItem(entry.getKey()))) continue;
                        inventory.getInventory().setItem(entry.getKey(),entry.getValue().getItemInterface().getItemStack());
                    }
                }
            }
            inventories.put(page, inventory);
        }
        StorageInventory storageInventoryPage = inventories.get(page);
        storageInventoryPage.open(player);
        if(storageConfig.getRefreshTimeStages() != 0L && storageConfig.getRefreshTimeStages() != -1L) startAnimationStages(page);
        if(currentStages.containsKey(page)){
            StageStorage stage = currentStages.get(page);
            if(stage.getTitle() != null) storageInventoryPage.setTitleInventory(stage.getTitle(), player);
        }

        OpenStorageEvent openStorageEvent = new OpenStorageEvent(player,storageInventoryPage);
        Bukkit.getPluginManager().callEvent(openStorageEvent);

        return true;
    }
    public void startAnimationStages(int page){
        if(currentStages.containsKey(page)) return;
        StorageConfig storageConfig = getStorageConfig();
        if(storageConfig.getRefreshTimeStages() == 0L || storageConfig.getRefreshTimeStages() == -1L) return;
        ArrayList<StageStorage> stages = storageConfig.getStagesOrder();
        if(stages.size()<1) return;
        StorageInventory inventory = inventories.get(page);
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(core, () ->{
            StageStorage stage = null;
            if(currentStages.containsKey(page)) {
                stage = currentStages.get(page);
                if(stages.indexOf(stage) + 1 < stages.size()) stage = stages.get(stages.indexOf(stage)+1);
                else {
                    stage = stages.get(0);
                }
            }
            if(!currentStages.containsKey(page)) stage = storageConfig.getStagesOrder().get(0);
            setStage(stage,page);
        },0,storageConfig.getRefreshTimeStages());
        inventory.setAnimationStagesTask(bukkitTask);
    }
    public void stopAnimationStages(int page){
        if(!currentStages.containsKey(page)) return;
        StorageInventory inventory = inventories.get(page);
        inventory.getAnimationStagesTask().cancel();
        inventory.setAnimationStagesTask(null);
    }
    public void setStage(String stageId, int page){
        StorageConfig storageConfig = getStorageConfig();
        if(!storageConfig.getStagesHashMap().containsKey(stageId)) return;
        StageStorage stage = storageConfig.getStagesHashMap().get(stageId);
        setStage(stage,page);
    }
    public void setStage(StageStorage stage, int page){
        if(!inventories.containsKey(page)) return;
        StorageInventory inventory = inventories.get(page);
        if(stage.getTitle() != null) inventory.setTitleInventory(stage.getTitle(),null);
        currentStages.put(page,stage);
        if(stage.getStorageItemsInterfaceConfig().containsKey(page)){
            removeItemsInterface(page);
            for(Map.Entry<Integer,StorageItemInterfaceConfig> entry : stage.getStorageItemsInterfaceConfig().get(page).entrySet()){
                if(PlaceholderItemInterface.isPlaceholderItem(inventory.getInventory().getItem(entry.getKey()))) continue;
                inventory.getInventory().setItem(entry.getKey(),entry.getValue().getItemInterface().getItemStack());
            }
        }
    }
    public void removeItemsInterface(int page){
        if(!inventories.containsKey(page)) return;
        StorageInventory storageInventory = inventories.get(page);
        for(int i=0;i<storageInventory.getInventory().getSize();i++){
            ItemStack item = storageInventory.getInventory().getItem(i);
            if(item != null && !item.getType().equals(Material.AIR) && core.getManagers().getItemInterfaceManager().isItemInterface(item)){
                storageInventory.getInventory().clear(i);
            }
        }
    }


    public void loadAllItemsDefault(){
        for (int p = 0; p < getTotalPages(); p++) {
            loadItemsDefault(p);
        }
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
                if(isItemInterfaceSlot(page,i,storageConfig)) continue;
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
        }
        else {
            ItemStack[] contents = items.get(page);
            if (contents == null) {
                int slots = getStorageConfig().getInventoryType().getSize();
                contents = new ItemStack[slots];
                items.put(page, contents);
            }

            int remainingAmount = itemStack.getAmount();
            for (int i = 0; i < contents.length && remainingAmount > 0; i++) {
                if(!canBePlaced(itemStack,page,i,storageConfig)) continue;
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

    public void clearSlotWithRestrictions(int page, int slot) {
        if(isItemInterfaceSlot(page,slot,getStorageConfig()) && !PlaceholderItemInterface.isPlaceholderItem(getItem(slot, page))) return;
        clearSlotPage(page, slot);
    }
    public void clearSlotPage(int page, int slot) {
        if (inventories.containsKey(page)) {
            StorageInventory storageInventory = inventories.get(page);
            Inventory inventory = storageInventory.getInventory();
            inventory.clear(slot);
        }
        else {
            ItemStack[] contents = items.getOrDefault(page,null);
            if (contents != null) {
                contents[slot] = null;
            }
        }
    }

    public void dropItem(StorageItemDataInfo item, Location loc, boolean sync, boolean remove){
        if(remove) item.removeWithRestrictions();
        Runnable runnable = () -> {
            loc.getWorld().dropItem(loc, item.getItemStack());
        };
        if(!sync) runnable.run();
        else Bukkit.getScheduler().runTask(StorageMechanic.getInstance(), runnable);
    }
    public void dropItem(StorageItemDataInfo item, Location loc){
        loc.getWorld().dropItem(loc, item.getItemStack());
    }

    public void dropItems(List<StorageItemDataInfo> items, Location loc){
        for(StorageItemDataInfo item : items){
            dropItem(item,loc);
        }
    }

    public void dropItems(List<StorageItemDataInfo> items, Location loc, boolean sync, boolean remove){
        if(remove) items.forEach(StorageItemDataInfo::removeWithRestrictions);
        Runnable runnable = () -> {
            for(StorageItemDataInfo item : items){
                dropItem(item,loc);
            }
        };
        if(!sync) runnable.run();
        else Bukkit.getScheduler().runTask(StorageMechanic.getInstance(), runnable);
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
                if(PlaceholderItemInterface.isPlaceholderItem(item)){
                    new ItemBuilderMechanic(item).meta( meta -> {
                        PersistentDataContainer itemPersistentDataContainer = meta.getPersistentDataContainer();
                        itemPersistentDataContainer.remove(PlaceholderItemInterface.NAMESPACED_KEY_PLACEHOLDER);
                        itemPersistentDataContainer.remove(ItemInterfaceManager.NAMESPACED_KEY);
                    }).build();
                }
                world.dropItem(dropLocation, item);
            }
        });
    }
    public ArrayList<Object> isBlocked(int slot, int page, StorageConfig storageConfig){ //1: true or false 2:messsage
        ArrayList<Object> objects = new ArrayList<>();
        if(!storageConfig.isStorageBlockItemEnabled()){
            objects.add(false);
            return objects;
        }
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
    public boolean canBePlaced(ItemStack item, int page, int slot, StorageConfig storageConfig){
        return (!isItemInterfaceSlot(page,slot,storageConfig) && !((boolean)isBlocked(slot,page,storageConfig).get(0)) && !isItemInList(item,slot,page,ListType.BLACKLIST,storageConfig) && isItemInList(item,slot,page,ListType.WHITELIST,storageConfig));
    }
    public boolean isItemInterfaceSlot(int page, int slot, StorageConfig storageConfig){
        if(!storageConfig.getStorageItemsInterfaceConfig().containsKey(page)) return false;
        if(currentStages.containsKey(page)){
            StageStorage stage = currentStages.get(page);
            if(stage.getStorageItemsInterfaceConfig().containsKey(page)){
                HashMap<Integer,StorageItemInterfaceConfig> hashMap = stage.getStorageItemsInterfaceConfig().get(page);
                return hashMap.containsKey(slot);
            }
        }
        HashMap<Integer,StorageItemInterfaceConfig> hashMap = storageConfig.getStorageItemsInterfaceConfig().get(page);
        return hashMap.containsKey(slot);
    }
    public boolean isItemInList(ItemStack itemStack, int slot, int page, ListType listType, StorageConfig storageConfig){
        String itemId = Adapter.getInstance().getAdapterID(itemStack);
        switch (listType){

            case BLACKLIST -> {
                if(!storageConfig.isStorageBlockItemEnabled()) return false;
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
                if(!storageConfig.isStorageItemsWhiteListEnabled()) return true;
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


    public List<StorageItemDataInfo> searchItemsByName(String s, boolean exact){
        s = s.toUpperCase(Locale.ENGLISH).trim();
        List<StorageItemDataInfo> list = new ArrayList<>();
        if(s.isEmpty()) return list;
        for(int i=0;i<getTotalPages();i++){
            HashMap<Integer,ItemStack> pageItemsMap = getMapItemsFromPage(i);
            for(Map.Entry<Integer,ItemStack> entry : pageItemsMap.entrySet()){
                if(entry.getValue().getType().equals(Material.AIR)) continue;
                String name = entry.getValue().getItemMeta().getDisplayName() != null ? entry.getValue().getItemMeta().getDisplayName().trim() : null;
                if(name == null || name.isEmpty()) name = entry.getValue().getType().toString();
                name = name.toUpperCase(Locale.ENGLISH);
                if(exact){
                    if(name.equals(s)){
                        list.add(new StorageItemDataInfo(entry.getValue(),i,entry.getKey(), this));
                    }
                }
                else {
                    if(name.contains(s)){
                        list.add(new StorageItemDataInfo(entry.getValue(),i,entry.getKey(), this));
                    }
                }
            }
        }
        return list;
    }

    public List<StorageItemDataInfo> searchItemsByMaterial(String s, boolean exact){
        s = s.toUpperCase(Locale.ENGLISH).trim();
        List<StorageItemDataInfo> list = new ArrayList<>();
        if(s.isEmpty()) return list;
        for(int i=0;i<getTotalPages();i++){
            HashMap<Integer,ItemStack> pageItemsMap = getMapItemsFromPage(i);
            for(Map.Entry<Integer,ItemStack> entry : pageItemsMap.entrySet()){
                if(entry.getValue().getType().equals(Material.AIR)) continue;
                if(exact){
                    if(entry.getValue().getType().toString().toUpperCase(Locale.ENGLISH).equals(s)){
                        list.add(new StorageItemDataInfo(entry.getValue(),i,entry.getKey(), this));
                    }
                }
                else {
                    if(entry.getValue().getType().toString().toUpperCase(Locale.ENGLISH).contains(s)){
                        list.add(new StorageItemDataInfo(entry.getValue(),i,entry.getKey(), this));
                    }
                }
            }
        }
        return list;
    }

    public List<StorageItemDataInfo> searchItemsByAdapterId(String s, boolean exact){
        if(exact) s = Adapter.getInstance().computeAdapterId(s);
        s = s.toUpperCase(Locale.ENGLISH).trim();
        List<StorageItemDataInfo> list = new ArrayList<>();
        if(s.isEmpty()) return list;
        for(int i=0;i<getTotalPages();i++){
            HashMap<Integer,ItemStack> pageItemsMap = getMapItemsFromPage(i);
            for(Map.Entry<Integer,ItemStack> entry : pageItemsMap.entrySet()){
                if(entry.getValue().getType().equals(Material.AIR)) continue;
                String adapterId = Adapter.getInstance().getAdapterID(entry.getValue()).toUpperCase(Locale.ENGLISH);
                if(adapterId.contains(s)){
                    list.add(new StorageItemDataInfo(entry.getValue(),i,entry.getKey(), this));
                }
            }
        }
        return list;
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
    public List<ItemStack> addItemStackToAllPages(ItemStack itemStack) {
        List<ItemStack> noAddedItems = new ArrayList<>();
        noAddedItems.add(itemStack);
        for (int i = 0; i < getTotalPages(); i++) {
            List<ItemStack> noAddedPage = new ArrayList<>();
            while (!noAddedItems.isEmpty()){
                List<ItemStack> noAdded = addItemStack(i, noAddedItems.get(0));
                noAddedItems.remove(0);
                if(noAdded.size()>0) noAddedPage.addAll(noAdded);
            }
            if(noAddedPage.size()==0) break;
            noAddedItems.addAll(noAddedPage);
        }
        return noAddedItems;
    }

    public List<ItemStack> addItemStackToAllPagesWithRestrictions(ItemStack itemStack) {
        List<ItemStack> noAddedItems = new ArrayList<>();
        noAddedItems.add(itemStack);
        for (int i = 0; i < getTotalPages(); i++) {
            List<ItemStack> noAddedPage = new ArrayList<>();
            while (!noAddedItems.isEmpty()){
                List<ItemStack> noAdded = addItemStackWithRestrictions(i, noAddedItems.get(0));
                noAddedItems.remove(0);
                if(noAdded.size()>0) noAddedPage.addAll(noAdded);
            }
            if(noAddedPage.size()==0) break;
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

    public void setItemInSlotPage(int page, int slot, ItemStack item){
        if (inventories.containsKey(page)) {
            StorageInventory storageInventory = inventories.get(page);
            storageInventory.getInventory().setItem(slot, item);
        }

        if (items.containsKey(page)) {
            ItemStack[] contents = items.get(page);
            contents[slot] = item;
        }
    }

    public ItemStack getItem(int slot, int page){
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

    public ItemStack[] getItemsFromPageInventory(int page){
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

    public ItemStack[] getItemsFromPageStorage(int page){
        if (items.containsKey(page)) {
            ItemStack[] contents = items.get(page);
            return contents;
        }
        return null;
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
                    if(PlaceholderItemInterface.isPlaceholderItem(contents[i])){
                        ItemStack item = ItemBuilderMechanic.copyOf(contents[i]).meta( meta -> {
                            PersistentDataContainer itemPersistentDataContainer = meta.getPersistentDataContainer();
                            itemPersistentDataContainer.remove(PlaceholderItemInterface.NAMESPACED_KEY_PLACEHOLDER);
                            itemPersistentDataContainer.remove(ItemInterfaceManager.NAMESPACED_KEY);
                        }).build();
                        itemsList.put(i,item);
                        continue;
                    }
                    itemsList.put(i,contents[i]);
                }
            }
        }

        if (items.containsKey(page)) {
            ItemStack[] contents = items.get(page);
            for(int i=0;i<contents.length;i++){
                if (contents[i] != null) {
                    if(PlaceholderItemInterface.isPlaceholderItem(contents[i])){
                        ItemStack item = ItemBuilderMechanic.copyOf(contents[i]).meta( meta -> {
                            PersistentDataContainer itemPersistentDataContainer = meta.getPersistentDataContainer();
                            itemPersistentDataContainer.remove(PlaceholderItemInterface.NAMESPACED_KEY_PLACEHOLDER);
                            itemPersistentDataContainer.remove(ItemInterfaceManager.NAMESPACED_KEY);
                        }).build();
                        itemsList.put(i,item);
                        continue;
                    }
                    itemsList.put(i,contents[i]);
                }
            }
        }

        return itemsList;
    }

    public StorageItemDataInfo getFirstItemStack(){
        for(int i=0;i<getTotalPages();i++){
            if (inventories.containsKey(i)) {
                StorageInventory storageInventory = inventories.get(i);
                ItemStack[] contents = storageInventory.getInventory().getContents();
                for(int k=0;k<contents.length;k++){
                    ItemStack item = contents[k];
                    if (item != null && !item.getType().isAir() && !core.getManagers().getItemInterfaceManager().isItemInterface(item)) {
                        return new StorageItemDataInfo(item,i,k,this);
                    }
                }
            }
            else if (items.containsKey(i)) {
                ItemStack[] contents = items.get(i);
                for(int k=0;k<contents.length;k++){
                    ItemStack item = contents[k];
                    if (item != null && !item.getType().isAir()) {
                        return new StorageItemDataInfo(item,i,k,this);
                    }
                }
            }
        }
        return null;
    }
    public StorageItemDataInfo getFirstItemStackSimilar(ItemStack similar){
        for(int i=0;i<getTotalPages();i++){
            if (inventories.containsKey(i)) {
                StorageInventory storageInventory = inventories.get(i);
                ItemStack[] contents = storageInventory.getInventory().getContents();
                for(int k=0;k<contents.length;k++){
                    ItemStack item = contents[k];
                    if (item != null && !item.getType().isAir() && item.isSimilar(similar)) return new StorageItemDataInfo(item,i,k,this);
                }
            }
            else if (items.containsKey(i)) {
                ItemStack[] contents = items.get(i);
                for(int k=0;k<contents.length;k++){
                    ItemStack item = contents[k];
                    if (item != null && !item.getType().isAir() && item.isSimilar(similar)) return new StorageItemDataInfo(item,i,k,this);
                }
            }
        }
        return null;
    }
    public int firstSlotEmptyPage(int page){
        if (inventories.containsKey(page)) {
            StorageInventory storageInventory = inventories.get(page);
            return storageInventory.getInventory().firstEmpty();
        }
        else if (items.containsKey(page)) {
            ItemStack[] contents = items.get(page);
            for(int k=0;k<contents.length;k++){
                ItemStack item = contents[k];
                if(item == null || item.getType().equals(Material.AIR)) return k;
            }
            return -1;
        }
        return -1;
    }
    public int firstSlotEmpty(){
        for(int i=0;i<getTotalPages();i++){
            int num = firstSlotEmptyPage(i);
            if(num == -1) continue;
            return num;
        }
        return -1;
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
    public List<ItemStack> addItemStackListToAllPages(List<ItemStack> itemStackList) {
        List<ItemStack> noAddedItems = new ArrayList<>();
        for (ItemStack itemStack : itemStackList) {
            List<ItemStack> noAdded = addItemStackToAllPages(itemStack);
            if(noAdded.size()==0) break;
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


    public int getTotalAmountFromList(List<StorageItemDataInfo> l){
        if(l.size()==0) return -1;
        int totalAmount = 0;
        for(StorageItemDataInfo storageIData : l){
            totalAmount += storageIData.getItemStack().getAmount();
        }
        return totalAmount;
    }

    public List<StorageItemDataInfo> getAllItemsSimilarFromAllPages(ItemStack similar){
        List<StorageItemDataInfo> list = new ArrayList<>();
        for(int i=0;i<getTotalPages();i++){
            list.addAll(getAllItemsSimilarFromPage(i, similar));
        }
        return list;
    }
    public List<StorageItemDataInfo> getAllItemsSimilarFromPage(int page, ItemStack similar){
        List<StorageItemDataInfo> list = new ArrayList<>();
        if (inventories.containsKey(page)) {
            StorageInventory storageInventory = inventories.get(page);
            ItemStack[] contents = storageInventory.getInventory().getContents();
            for(int k=0;k<contents.length;k++){
                ItemStack item = contents[k];
                if (item != null && !item.getType().isAir() && item.isSimilar(similar)) list.add(new StorageItemDataInfo(item,page,k,this));
            }
        }
        if (items.containsKey(page)) {
            ItemStack[] contents = items.get(page);
            for(int k=0;k<contents.length;k++){
                ItemStack item = contents[k];
                if (item != null && !item.getType().isAir() && item.isSimilar(similar)) list.add(new StorageItemDataInfo(item,page,k,this));
            }
        }
        return list;
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

    public HashMap<Integer, StageStorage> getCurrentStages() {
        return currentStages;
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
}
