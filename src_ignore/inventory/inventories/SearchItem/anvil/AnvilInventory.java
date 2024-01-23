package dev.wuason.storagemechanic.inventory.inventories.SearchItem.anvil;

import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.items.ItemBuilderMechanic;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.mechanics.utils.StorageUtils;
import dev.wuason.mechanics.utils.Utils;
import dev.wuason.nms.wrappers.VersionWrapper;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.inventory.config.InventoryConfig;
import dev.wuason.storagemechanic.inventory.config.InventoryConfigManager;
import dev.wuason.storagemechanic.inventory.config.ItemInventoryConfig;
import dev.wuason.storagemechanic.inventory.inventories.SearchItem.SearchItem;
import dev.wuason.storagemechanic.inventory.inventories.SearchItem.SearchType;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class AnvilInventory {

    private String configId;
    private ItemStack[] playerInventory = null;
    private Storage storage;
    private NamespacedKey namespacedKeyItem = new NamespacedKey(StorageMechanic.getInstance(),"InventoryItemType");
    private HashMap<String, SearchItem> resultList = new HashMap<>();
    private NamespacedKey namespacedKeyItemSearch = new NamespacedKey(StorageMechanic.getInstance(),"ItemSearch");

    private int currentPage = 1;
    private String configIdResult;
    private AnvilResultInventory anvilResultInventory;
    private ArrayList<ItemStack> itemsToAddInInventory = new ArrayList<>();
    private InventoryAnvilManager inventoryAnvilManager;
    private Long timeRefresh = 40L; //5Sec
    private String renameText = "";
    private BukkitTask mainTask = null;
    private boolean inSearch = false;
    private SearchType searchType;
    private StorageManager storageManager;
    private StorageMechanic core;

    public AnvilInventory(Player player, String configId, Storage storage, String configIdResult, InventoryAnvilManager inventoryAnvilManager, SearchType searchType, StorageManager storageManager, StorageMechanic core) {
        this.configIdResult = configIdResult;
        this.searchType = searchType;
        this.storage = storage;
        this.configId = configId;
        this.core = core;
        this.inventoryAnvilManager = inventoryAnvilManager;
        this.storageManager = storageManager;
        InventoryConfigManager inventoryConfigManager = StorageMechanic.getInstance().getManagers().getInventoryConfigManager();
        InventoryConfig inventoryConfig = inventoryConfigManager.getInventories().getOrDefault(configId,null);
        ItemInventoryConfig repairItemConfig =  inventoryConfigManager.getAllItemsByType(configId,"REPAIR_ITEM").get(0);
        ItemBuilderMechanic itemBuilderMechanicRepairItem = new ItemBuilderMechanic(repairItemConfig.getItem(),repairItemConfig.getAmount());
        ItemStack repairItem = null;
        if(repairItemConfig.getLore() != null) itemBuilderMechanicRepairItem.setLore(repairItemConfig.getLore());
        if(repairItemConfig.getDisplayName() == null){
            repairItem = itemBuilderMechanicRepairItem.buildWithVoidName();
        }
        else {
            itemBuilderMechanicRepairItem.setName(repairItemConfig.getDisplayName());
            repairItem = itemBuilderMechanicRepairItem.build();
        }
        anvilGui = Mechanics.getInstance().getServerNmsVersion().getVersionWrapper().createAnvilGui(player, AdventureUtils.deserializeLegacy(inventoryConfig.getTitle(),player),repairItem);
        anvilGui.setBlockClose(true);
    }

    public VersionWrapper.AnvilGui getAnvilGui() {
        return anvilGui;
    }

    public String getConfigId() {
        return configId;
    }

    public void open(){
        resultList = new HashMap<>();
        currentPage = 1;
        savePlayerInventory();
        setItems();
        anvilGui.open();
        startMainTask();
        inventoryAnvilManager.getAnvilInventoriesOpened().put(anvilGui.getInventory(),this);
    }
    public void close(CloseType closeType){
        if(closeType.equals(CloseType.NORMAL)){
            if(inSearch) return;
        }
        stopMainTask();
        inventoryAnvilManager.getAnvils().remove(anvilGui.getPlayer());
        inventoryAnvilManager.getAnvilInventoriesOpened().remove(anvilGui.getInventory());
        anvilGui.close();
        if(anvilResultInventory != null){
            while (!anvilResultInventory.getInventory().getViewers().isEmpty()){
                anvilResultInventory.getInventory().getViewers().get(0).closeInventory();
            }
        }
        loadPlayerInventory();
        for(ItemStack itemSearch : itemsToAddInInventory){
            StorageUtils.addItemToInventoryOrDrop(anvilGui.getPlayer(), itemSearch);
        }
        itemsToAddInInventory = new ArrayList<>();
    }
    public void reOpen(){
        resultList = new HashMap<>();
        currentPage = 1;
        inventoryAnvilManager.getAnvilInventoriesOpened().remove(anvilGui.getInventory());
        anvilGui.close();
        anvilGui.getPlayer().getInventory().clear();
        for(int i=0;i<9;i++){
            ItemStack itemStack = anvilGui.getPlayer().getInventory().getItem(i);
            if(itemStack == null || itemStack.equals(Material.AIR)){
                anvilGui.getPlayer().getInventory().setHeldItemSlot(i);
            }
        }
        setItems();
        anvilGui.open();
        inventoryAnvilManager.getAnvilInventoriesOpened().put(anvilGui.getInventory(),this);
    }
    public void setEmptyItemRepairItem(){
        anvilGui.getAnvilInventory().setItem(0,anvilGui.getRepairItem());
        anvilGui.getAnvilInventory().setItem(1,anvilGui.getRepairItem());
    }

    public void savePlayerInventory() {
        this.playerInventory = anvilGui.getPlayer().getInventory().getContents();
        anvilGui.getPlayer().getInventory().clear();
    }
    public void loadPlayerInventory() {
        if(playerInventory == null) return;
        anvilGui.getPlayer().getInventory().setContents(this.playerInventory);
        this.playerInventory = null;
    }

    public void setPlayerInventory() {
        this.playerInventory = anvilGui.getPlayer().getInventory().getContents();
    }
    public ItemStack[] getPlayerInventory() {
        return playerInventory;
    }

    public ItemInventoryConfig getBackItem(){
        InventoryConfigManager inventoryConfigManager = core.getManagers().getInventoryConfigManager();
        return inventoryConfigManager.getAllItemsByType(configId,"BACK_ITEM").get(0);
    }
    public ItemInventoryConfig getNextItem(){
        InventoryConfigManager inventoryConfigManager = core.getManagers().getInventoryConfigManager();
        return inventoryConfigManager.getAllItemsByType(configId,"NEXT_ITEM").get(0);
    }
    public ItemStack getCloseItem(){
        InventoryConfigManager inventoryConfigManager = core.getManagers().getInventoryConfigManager();
        return buildItemStack(inventoryConfigManager.getAllItemsByType(configId,"CLOSE_ITEM").get(0));
    }
    public ItemInventoryConfig getResearchItem(){
        InventoryConfigManager inventoryConfigManager = core.getManagers().getInventoryConfigManager();
        return inventoryConfigManager.getAllItemsByType(configId,"RESEARCH_ITEM").get(0);
    }
    public ItemStack getOpenInvResult(){
        InventoryConfigManager inventoryConfigManager = core.getManagers().getInventoryConfigManager();
        return buildItemStack(inventoryConfigManager.getAllItemsByType(configId,"OPEN_RESULT_INV_ITEM").get(0));
    }
    public ItemStack setLoreItem(ItemStack i, int page, int slot){
        InventoryConfigManager inventoryConfigManager = core.getManagers().getInventoryConfigManager();
        ItemInventoryConfig itemInventoryConfig = inventoryConfigManager.getAllItemsByType(configId,"RESULT_LORE").get(0);
        if(itemInventoryConfig.getLore() == null) return i;
        if(itemInventoryConfig.getLore().size() < 1) return i;
        ItemBuilderMechanic itemBuilderMechanic = new ItemBuilderMechanic(i);
        Map<String, String> replacements = new HashMap<>(){{
            put("%SLOT%", "" + slot);
            put("%PAGE%", "" + page);
        }};

        for(String lore : itemInventoryConfig.getLore()){
            if(lore != null){
                itemBuilderMechanic.addLoreLine(AdventureUtils.deserializeLegacy(Utils.replaceVariables(lore,replacements),anvilGui.getPlayer()));
            }
        }
        return itemBuilderMechanic.build();
    }
    public ItemStack setItemSearchData(ItemStack item, String itemSearchId){
        return new ItemBuilderMechanic(item).addPersistentData(namespacedKeyItemSearch,itemSearchId).build();
    }

    public void startMainTask(){
        mainTask = Bukkit.getScheduler().runTaskTimerAsynchronously(core,() ->{
            String renameText = anvilGui.getAnvilInventory().getRenameText();
            if(anvilGui.isOpen()){
                if(!storageManager.getStorageMap().containsKey(storage.getId())){
                     close(CloseType.NORMAL);
                }
            }
            if(this.renameText == null || !this.renameText.equals(renameText)){
                this.renameText = renameText;
                if(renameText != null && !inSearch) {
                    resultList = new HashMap<>();
                    currentPage = 1;
                    setItemsNull();
                    setItems();
                    searchItems(renameText.toLowerCase(Locale.ENGLISH),searchType);
                    setResultItems();
                }
            }




        },1L,timeRefresh);
    }

    public void stopMainTask(){
        mainTask.cancel();
        mainTask = null;
    }
    public void setItemsNull(){
        ItemStack[] contents = anvilGui.getPlayer().getInventory().getContents();
        for(int i=0;i<contents.length;i++){
            contents[i] = null;
        }
        anvilGui.getPlayer().getInventory().setContents(contents);
    }

    public InventoryConfig getInventoryConfig(){
        InventoryConfigManager inventoryConfigManager = core.getManagers().getInventoryConfigManager();
        return inventoryConfigManager.getInventories().getOrDefault(configId,null);
    }

    public void setResultItems(){
        if(resultList.size()<1) return;
        InventoryConfigManager inventoryConfigManager = StorageMechanic.getInstance().getManagers().getInventoryConfigManager();
        if(inventoryConfigManager == null) return;
        ItemStack[] content = anvilGui.getPlayer().getInventory().getContents();

        content = setResultItemsPage(1,content);

        anvilGui.getPlayer().getInventory().setContents(content);
    }

    public ItemStack[] setResultItemsPage(int page, ItemStack[] contents){
        currentPage = page;
        InventoryConfig inventoryConfig = getInventoryConfig();

        for(int s : inventoryConfig.getDataSlots()){
            contents[s] = null;
        }
        contents[getBackItem().getSlot()] = null;
        contents[getNextItem().getSlot()] = null;

        int init = (page * inventoryConfig.getDataSlots().size()) - inventoryConfig.getDataSlots().size();
        SearchItem[] searchItems = resultList.values().toArray(SearchItem[]::new);

        if( (page * inventoryConfig.getDataSlots().size()) < resultList.size()){
            ItemInventoryConfig nextItem = getNextItem();
            contents[nextItem.getSlot()] = buildItemStack(nextItem);
        }
        if(page>1){
            ItemInventoryConfig backItem = getBackItem();
            contents[backItem.getSlot()] = buildItemStack(backItem);
        }
        for(int s : inventoryConfig.getDataSlots()){
            if(searchItems.length>=(init + 1)){
                SearchItem searchItem = searchItems[init];
                contents[s] = setLoreItem(setItemSearchData(searchItem.getItemStack().clone(),searchItem.getId()),searchItem.getPage(),searchItem.getSlot());
                init++;
            }
        }
        return contents;
    }
    public void updateStatusItem(){
        ItemStack[] contents = anvilGui.getPlayer().getInventory().getContents();
        ItemInventoryConfig researchItemConfig = getResearchItem();
        if(contents[researchItemConfig.getSlot()] != null){
            ItemStack researchItem = contents[researchItemConfig.getSlot()];
            List<String> loreToUpdate = researchItemConfig.getLore();
            if(loreToUpdate != null){
                List<String> lore = new ArrayList<>();
                Map<String, String> replacements = new HashMap<>(){{
                    put("%SEARCH_STATUS%", "" + inSearch);
                }};
                for(String l : loreToUpdate){
                    lore.add(AdventureUtils.deserializeLegacy(Utils.replaceVariables(l,replacements),anvilGui.getPlayer()));
                }
                ItemMeta itemMeta = researchItem.getItemMeta();
                itemMeta.setLore(lore);
                researchItem.setItemMeta(itemMeta);
                contents[researchItemConfig.getSlot()] = researchItem;
            }
        }
        anvilGui.getPlayer().getInventory().setContents(contents);

    }

    public Set<SearchItem> searchItems(String s, SearchType searchType){
        Set<SearchItem> searchItems = new HashSet<>();
        switch (searchType){
            case SearchType.NAME -> {
                inSearch = true;
                updateStatusItem();
                List<SearchItem> list = storage.searchItemsByName(s, false).stream().map(item -> new SearchItem(item.getSlot(),item.getPage(), item.getItemStack(), item.getStorage())).toList();
                for(SearchItem searchItem : list){
                    resultList.put(searchItem.getId(), searchItem);
                }
                inSearch = false;
                updateStatusItem();
            }

            case SearchType.MATERIAL -> {
                inSearch = true;
                updateStatusItem();
                List<SearchItem> list = storage.searchItemsByMaterial(s, false).stream().map(item -> new SearchItem(item.getSlot(),item.getPage(), item.getItemStack(), item.getStorage())).toList();
                for(SearchItem searchItem : list){
                    resultList.put(searchItem.getId(), searchItem);
                }
                inSearch = false;
                updateStatusItem();
            }
        }
        return searchItems;
    }

    public ItemStack buildItemStack(ItemInventoryConfig item){
        ItemBuilderMechanic itemBuilderMechanic = new ItemBuilderMechanic(item.getItem(),item.getAmount());
        itemBuilderMechanic.addPersistentData(namespacedKeyItem,item.getType());
        ItemStack itemStack = null;
        if(item.getLore() != null) {
            Map<String, String> replacements = new HashMap<>(){{
                put("%SEARCH_STATUS%", "" + inSearch);
            }};
            List<String> lore = new ArrayList<>();
            for(String l : item.getLore()){
                lore.add(AdventureUtils.deserializeLegacy(Utils.replaceVariables(l,replacements),anvilGui.getPlayer()));
            }
            itemBuilderMechanic.setLore(lore);
        }
        if(item.getDisplayName() == null) itemStack = itemBuilderMechanic.buildWithVoidName();
        else {
            itemBuilderMechanic.setName(AdventureUtils.deserializeLegacy(item.getDisplayName(),anvilGui.getPlayer()));
            itemStack = itemBuilderMechanic.build();
        }
        return itemStack;
    }

    public void setItems(){
        InventoryConfigManager inventoryConfigManager = StorageMechanic.getInstance().getManagers().getInventoryConfigManager();
        InventoryConfig inventoryConfig = inventoryConfigManager.getInventories().getOrDefault(configId,null);
        ItemStack[] content = new ItemStack[41];

        for(int i=0;i<content.length;i++){

            ItemInventoryConfig item = inventoryConfig.getItemsInventory().getOrDefault(i,null);
            if(item == null) continue;
            switch (item.getType().toUpperCase(Locale.ENGLISH)){
                case "OPEN_RESULT_INV_ITEM" -> {
                    content[i] = getOpenInvResult();
                }

                case "CLOSE_ITEM" -> {
                    content[i] = getCloseItem();
                }

                case "BLOCKED_ITEM" -> {
                    content[i] = buildItemStack(item);
                }
                case "RESEARCH_ITEM" -> {
                    content[i] = buildItemStack(getResearchItem());
                }
            }

        }
        anvilGui.getPlayer().getInventory().setContents(content);
    }

    public Storage getStorage() {
        return storage;
    }

    public NamespacedKey getNamespacedKeyItem() {
        return namespacedKeyItem;
    }


    public String getConfigIdResult() {
        return configIdResult;
    }

    public AnvilResultInventory getAnvilResultInventory() {
        return anvilResultInventory;
    }


    public InventoryAnvilManager getInventoryAnvilManager() {
        return inventoryAnvilManager;
    }

    public String getRenameText() {
        return renameText;
    }

    public BukkitTask getMainTask() {
        return mainTask;
    }

    public SearchType getSearchType() {
        return searchType;
    }

    public NamespacedKey getNamespacedKeyItemSearch() {
        return namespacedKeyItemSearch;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public HashMap<String, SearchItem> getResultList() {
        return resultList;
    }

    public ArrayList<ItemStack> getItemsToAddInInventory() {
        return itemsToAddInInventory;
    }

    public void setResultList(HashMap<String, SearchItem> resultList) {
        this.resultList = resultList;
    }

    public Long getTimeRefresh() {
        return timeRefresh;
    }

    public boolean isInSearch() {
        return inSearch;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }


    public enum CloseType{
        NORMAL,
        STOP
    }

    public void setAnvilResultInventory(AnvilResultInventory anvilResultInventory) {
        this.anvilResultInventory = anvilResultInventory;
    }

    public AnvilResultInventory openAnvilResultInventory(){
        InventoryConfig inventoryConfig = core.getManagers().getInventoryConfigManager().getInventories().getOrDefault(configIdResult, null);
        anvilResultInventory = new AnvilResultInventory(inventoryConfig.getRows() * 9, AdventureUtils.deserializeLegacy(inventoryConfig.getTitle(),anvilGui.getPlayer()),this,inventoryAnvilManager,configIdResult,core);
        Bukkit.getScheduler().runTask(core,() -> anvilGui.getAnvilInventory().clear());
        anvilGui.getPlayer().getInventory().setContents(playerInventory);
        playerInventory = null;
        anvilResultInventory.openDefault();
        return anvilResultInventory;
    }
}
