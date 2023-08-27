package dev.wuason.storagemechanic.inventory.inventories.SearchItem.anvil;

import dev.wuason.fastinv.FastInv;
import dev.wuason.mechanics.items.ItemBuilderMechanic;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.mechanics.utils.Utils;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.inventory.config.InventoryConfig;
import dev.wuason.storagemechanic.inventory.config.InventoryConfigManager;
import dev.wuason.storagemechanic.inventory.config.ItemInventoryConfig;
import dev.wuason.storagemechanic.inventory.inventories.SearchItem.SearchItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class AnvilResultInventory extends FastInv {
    private InventoryAnvilManager inventoryAnvilManager;
    private AnvilInventory anvilInventory;
    private StorageMechanic core;
    private int currentPage = 1;
    private String inventoryConfigId;
    private boolean openned = false;

    private static NamespacedKey NAMESPACEDKEY_ITEM_SEARCH = new NamespacedKey(StorageMechanic.getInstance(),"ItemSearch");

    public AnvilResultInventory(int size, String title, AnvilInventory anvilInventory, InventoryAnvilManager inventoryAnvilManager, String inventoryConfigId, StorageMechanic core) {
        super(size, title);
        this.anvilInventory = anvilInventory;
        this.inventoryConfigId = inventoryConfigId;
        this.core = core;
    }

    public void open(int page){
        currentPage = page;
        InventoryConfig inventoryConfig = getInventoryConfig();
        setResultItemsNull();
        ItemInventoryConfig backItem = getItemInventory(Item.BACK_PAGE);
        ItemInventoryConfig nextItem = getItemInventory(Item.NEXT_PAGE);
        getInventory().clear(nextItem.getSlot());
        getInventory().clear(backItem.getSlot());
        HashMap<String, SearchItem> resultList = getAnvilInventory().getResultList();
        SearchItem[] results = resultList.values().toArray(new SearchItem[0]);
        int init = (page * inventoryConfig.getDataSlots().size()) - inventoryConfig.getDataSlots().size();
        if((page * inventoryConfig.getDataSlots().size()) < resultList.size()){
            getInventory().setItem(nextItem.getSlot(), anvilInventory.buildItemStack(nextItem));
        }
        if(page>1){
            getInventory().setItem(backItem.getSlot(), anvilInventory.buildItemStack(backItem));
        }
        for(int s : inventoryConfig.getDataSlots()){
            if(results.length>=(init + 1)){
                SearchItem searchItem = results[init];
                ItemStack item = searchItem.getItemStack().clone();
                setItemSearchData(item,searchItem);
                getInventory().setItem(s,item);
                init++;
            }
        }

    }

    public void openDefault(){
        setItems();
        open(1);
        Bukkit.getScheduler().runTask(core,() -> open(anvilInventory.getAnvilGui().getPlayer()));
    }
    @Override
    public void onOpen(InventoryOpenEvent event) {


    }
    @Override
    public void onClick(InventoryClickEvent event) {
        if(event.getInventory() == getInventory()){
            if(event.getClickedInventory() == null) return;
            Player player = (Player) event.getWhoClicked();
            event.setCancelled(false);
            if(!event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
                event.setCancelled(true);
                if(event.getCurrentItem() != null && !event.getCurrentItem().equals(Material.AIR) && event.getCurrentItem().getItemMeta() != null && event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(NAMESPACEDKEY_ITEM_SEARCH, PersistentDataType.STRING)){
                    SearchItem searchItem = anvilInventory.getResultList().getOrDefault(event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(NAMESPACEDKEY_ITEM_SEARCH, PersistentDataType.STRING),null);
                    if(searchItem != null){
                        if(event.getClick().isShiftClick()){
                            int slot = player.getInventory().firstEmpty();
                            if(slot == -1) return;
                            if(searchItem.exist()) {
                                searchItem.removeItemFromStorage();
                                player.getInventory().setItem(slot,searchItem.getItemStack());
                            }
                            anvilInventory.getResultList().remove(searchItem.getId());
                            open(currentPage);
                        }
                        else if (event.getCursor() == null || event.getCursor().getType().equals(Material.AIR)){
                            if(searchItem.exist()) {
                                searchItem.removeItemFromStorage();
                                player.setItemOnCursor(searchItem.getItemStack());
                            }
                            anvilInventory.getResultList().remove(searchItem.getId());
                            open(currentPage);
                        }
                    }
                }
                if(event.getCurrentItem() != null && !event.getCurrentItem().equals(Material.AIR) && event.getCurrentItem().getItemMeta() != null && event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(anvilInventory.getNamespacedKeyItem(), PersistentDataType.STRING)){
                    String type = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(anvilInventory.getNamespacedKeyItem(),PersistentDataType.STRING);
                    switch (type){
                        case "NEXT_ITEM" ->{
                            Bukkit.getScheduler().runTaskAsynchronously(core, () -> open(currentPage + 1));
                        }
                        case "BACK_ITEM" ->{
                            Bukkit.getScheduler().runTaskAsynchronously(core, () -> open(currentPage - 1));
                        }
                        case "OPEN_ANVIL" ->{
                            anvilInventory.setAnvilResultInventory(null);
                            anvilInventory.savePlayerInventory();
                            anvilInventory.setEmptyItemRepairItem();
                            Bukkit.getScheduler().runTask(core, () -> anvilInventory.reOpen());
                        }
                    }
                }
            }
            if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
                if(event.getClick().isShiftClick()) event.setCancelled(true);
            }

        }
    }
    @Override
    public void onClose(InventoryCloseEvent event) {
        if(anvilInventory.getInventoryAnvilManager().getAnvils().containsKey(event.getPlayer())){
            if(anvilInventory.getAnvilResultInventory() != null){
                Bukkit.getScheduler().runTaskLater(StorageMechanic.getInstance(), () -> {
                    event.getPlayer().openInventory(getInventory());
                }, 1L);
            }
        }
    }

    public ItemInventoryConfig getItemInventory(Item item){
        switch (item){
            case BACK_PAGE -> {
                return core.getManagers().getInventoryConfigManager().getAllItemsByType(inventoryConfigId, "BACK_ITEM").get(0);
            }
            case NEXT_PAGE -> {
                return core.getManagers().getInventoryConfigManager().getAllItemsByType(inventoryConfigId, "NEXT_ITEM").get(0);
            }
            case OPEN_ANVIL -> {
                return core.getManagers().getInventoryConfigManager().getAllItemsByType(inventoryConfigId, "OPEN_ANVIL").get(0);
            }
        }
        return null;
    }
    public void setResultItemsNull(){
        InventoryConfig inventoryConfig = getInventoryConfig();
        for(int s : inventoryConfig.getDataSlots()){
            getInventory().clear(s);
        }
    }
    public void setItems(){
        InventoryConfig inventoryConfig = getInventoryConfig();
        for(int i=0;i<getInventory().getContents().length;i++){
            ItemInventoryConfig itemInventoryConfig = inventoryConfig.getItemsInventory().getOrDefault(i,null);
            if(itemInventoryConfig == null) continue;
            switch (itemInventoryConfig.getType().toUpperCase()){
                case "OPEN_ANVIL" -> {
                    getInventory().setItem(i, anvilInventory.buildItemStack(getItemInventory(Item.OPEN_ANVIL)));
                }
                case "BLOCKED_ITEM" -> {
                    getInventory().setItem(i, anvilInventory.buildItemStack(itemInventoryConfig));
                }
            }
        }
    }

    public void setItemSearchData(ItemStack itemStack, SearchItem searchItem){
        ItemBuilderMechanic itemBuilderMechanic = new ItemBuilderMechanic(itemStack);
        itemBuilderMechanic.addPersistentData(NAMESPACEDKEY_ITEM_SEARCH, searchItem.getId());

        //LORE
        InventoryConfigManager inventoryConfigManager = core.getManagers().getInventoryConfigManager();
        ItemInventoryConfig itemInventoryConfig = inventoryConfigManager.getAllItemsByType(inventoryConfigId,"RESULT_LORE").get(0);

        if(itemInventoryConfig.getLore() != null && itemInventoryConfig.getLore().size() > 0){

            Map<String, String> replacements = new HashMap<>(){{
                put("%SLOT%", "" + searchItem.getSlot());
                put("%PAGE%", "" + searchItem.getPage());
            }};

            for(String lore : itemInventoryConfig.getLore()){
                if(lore != null){
                    itemBuilderMechanic.addLoreLine(AdventureUtils.deserializeLegacy(Utils.replaceVariables(lore,replacements),anvilInventory.getAnvilGui().getPlayer()));
                }
            }

        }

        itemBuilderMechanic.build();
    }

    public InventoryConfig getInventoryConfig(){
        return core.getManagers().getInventoryConfigManager().getInventories().getOrDefault(inventoryConfigId,null);
    }

    public enum Item{
        NEXT_PAGE,
        BACK_PAGE,
        OPEN_ANVIL
    }

    public InventoryAnvilManager getInventoryAnvilManager() {
        return inventoryAnvilManager;
    }

    public AnvilInventory getAnvilInventory() {
        return anvilInventory;
    }

    public StorageMechanic getCore() {
        return core;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public String getInventoryConfigId() {
        return inventoryConfigId;
    }

    public boolean isOpenned() {
        return openned;
    }
}
