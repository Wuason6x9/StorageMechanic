package dev.wuason.storagemechanic.storages;

import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.storagemechanic.Managers;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.actions.events.def.ClickItemInterfaceActionEvent;
import dev.wuason.storagemechanic.actions.events.def.ClickStoragePageActionEvent;
import dev.wuason.storagemechanic.actions.events.def.CloseStoragePageActionEvent;
import dev.wuason.storagemechanic.actions.events.def.OpenStoragePageActionEvent;
import dev.wuason.storagemechanic.api.events.storage.ClickPageStorageEvent;
import dev.wuason.storagemechanic.data.DataManager;
import dev.wuason.storagemechanic.data.SaveCause;
import dev.wuason.storagemechanic.items.ItemInterface;
import dev.wuason.storagemechanic.items.ItemInterfaceManager;
import dev.wuason.storagemechanic.items.items.SearchPageItemInterface;
import dev.wuason.storagemechanic.storages.config.StorageConfig;
import dev.wuason.storagemechanic.storages.config.StorageSoundConfig;
import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import dev.wuason.storagemechanic.storages.types.block.mechanics.BlockMechanicManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StorageManager implements Listener {
    private StorageMechanic core;
    private Map<String, Storage> storageMap = new HashMap<>();
    private Map<UUID, WaitingInputData> waitingInput = new ConcurrentHashMap<>();
    private DataManager dataManager;
    private Managers managers;
    private static Long TIME_TO_SAVE = 1200000L;

    public StorageManager(StorageMechanic core, DataManager dataManager, Managers managers) {
        this.core = core;
        this.dataManager = dataManager;
        this.managers = managers;
    }

    public Storage createStorage(String storageIdConfig, StorageOriginContext storageOriginContext) {
        if(core.getManagers().getStorageConfigManager().existsStorageConfig(storageIdConfig)){
            Storage storage = new Storage(storageIdConfig,storageOriginContext);
            storageMap.put(storage.getId(), storage);
            return storage;
        }
        return null;
    }
    public Storage createStorage(String storageIdConfig, UUID id, StorageOriginContext storageOriginContext) {
        if(core.getManagers().getStorageConfigManager().existsStorageConfig(storageIdConfig)){
            Storage storage = new Storage(storageIdConfig,id,storageOriginContext);
            storageMap.put(storage.getId(), storage);
            return storage;
        }
        return null;
    }

    public void antiTrashTask(){
        if(storageMap.isEmpty()) return;
        for (Map.Entry<String, Storage> entry : storageMap.entrySet()) {
            if(entry.getValue().getLastOpen().getTime() + TIME_TO_SAVE < System.currentTimeMillis() && entry.getValue().getLastAccess().getTime() + TIME_TO_SAVE < System.currentTimeMillis()){
                saveStorage(entry.getValue(), SaveCause.NORMAL_SAVE);
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event){
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof StorageInventory) {
            StorageInventory storageInventory = (StorageInventory) holder;
            Storage storage = storageInventory.getStorage();
            StorageConfig storageConfig = core.getManagers().getStorageConfigManager().getStorageConfigById(storage.getStorageIdConfig());
            DragItemCheck(storage, storageInventory, event, storageConfig);
            if(storageConfig.isStorageBlockItemEnabled()){
                DragItemBlocked(storage, storageInventory, event, storageConfig);
            }
            if(storageConfig.isStorageItemsWhiteListEnabled() || storageConfig.isStorageItemsBlackListEnabled()) {
                DragItemCheckList(storage, storageInventory, event, storageConfig);
            }
            storageInventory.onDrag(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if(event.getClickedInventory() == null) return;
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof StorageInventory) {
            StorageInventory storageInventory = (StorageInventory) holder;
            Storage storage = storageInventory.getStorage();
            StorageConfig storageConfig = core.getManagers().getStorageConfigManager().getStorageConfigById(storage.getStorageIdConfig());
            //SOUNDS
            if(storageConfig.isStorageSoundEnabled()){
                for(StorageSoundConfig soundConfig : storageConfig.getStorageSounds()){
                    if(soundConfig.getType().equals(StorageSoundConfig.Type.CLICK) && soundConfig.getPagesToSlots().containsKey(storageInventory.getPage())){
                        if(soundConfig.getPagesToSlots().get(storageInventory.getPage()).size()>0){
                            if(event.getClickedInventory() != null && !event.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
                                if (soundConfig.getPagesToSlots().get(storageInventory.getPage()).contains(storageInventory.getPage())) {
                                    ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), soundConfig.getSound(), soundConfig.getVolume(), soundConfig.getPitch());
                                }
                            }
                        }
                        else {
                            ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(),soundConfig.getSound(), soundConfig.getVolume(),soundConfig.getPitch());
                        }
                    }
                }
            }
            if(event.getCurrentItem() != null && event.getClickedInventory().getType().equals(InventoryType.PLAYER) && event.getClick().isShiftClick()){
                ShiftClickItemCheck(storage, storageInventory, event, storageConfig);
                if(storageConfig.isStorageBlockItemEnabled()) {
                    if(storageInventory.getInventory().firstEmpty() != -1 && event.getCurrentItem() != null){

                        ShiftClickItemBlocked(storage,storageInventory,event,storageConfig);

                    }
                }
                if(storageConfig.isStorageItemsWhiteListEnabled() || storageConfig.isStorageItemsBlackListEnabled()) {
                    if(storageInventory.getInventory().firstEmpty() != -1 && event.getCurrentItem() != null){

                        ShiftClickItemCheckList(storage,storageInventory,event,storageConfig);

                    }
                }
            }
            //ITEMS INTERFACES & Item CheckList
            if (event.getClickedInventory() != null && !event.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
                // Cancel event if clicked item is an interface item
                ItemStack clickedItem = event.getCurrentItem();
                ItemInterfaceManager itemInterfaceManager = core.getManagers().getItemInterfaceManager();
                if (clickedItem != null && itemInterfaceManager.isItemInterface(clickedItem) ) {
                    event.setCancelled(true);
                    ItemInterface itemInterface = core.getManagers().getItemInterfaceManager().getItemInterfaceByItemStack(clickedItem);
                    itemInterface.onClick(storage,storageInventory,event,storageConfig,this);

                    //events
                    ClickItemInterfaceActionEvent clickItemInterfaceActionEvent = new ClickItemInterfaceActionEvent(storageInventory, event, itemInterface);
                    core.getManagers().getActionManager().callEvent(clickItemInterfaceActionEvent, storage.getId(), storage);
                }
                ClickItemCheck(storage, storageInventory, event, storageConfig);
                if(storageConfig.isStorageBlockItemEnabled()){
                    ClickItemBlocked(storage, storageInventory, event, storageConfig);
                }
                if(storageConfig.isStorageItemsWhiteListEnabled() || storageConfig.isStorageItemsBlackListEnabled()) {
                    ClickItemCheckList(storage, storageInventory, event, storageConfig);
                }
            }
            //Hopper event
            if(storage.getStorageOriginContext().getContext().equals(StorageOriginContext.Context.BLOCK_STORAGE)){
                List<String> list = storage.getStorageOriginContext().getData();
                BlockMechanicManager.HOPPER_BLOCK_MECHANIC.checkBlockStorageAndTransfer(new String[]{list.get(1),list.get(0),list.get(2)});
            }
            //Events

            ClickStoragePageActionEvent clickStoragePageActionEvent = new ClickStoragePageActionEvent(storageInventory, event);
            core.getManagers().getActionManager().callEvent(clickStoragePageActionEvent, storage.getId(), storage);

            ClickPageStorageEvent clickPageStorageEvent = new ClickPageStorageEvent(event,storageInventory);
            Bukkit.getPluginManager().callEvent(clickPageStorageEvent);

            storageInventory.onClick(event);

        }
    }
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof StorageInventory) {
            StorageInventory storageInventory = (StorageInventory) holder;
            Storage storage = storageInventory.getStorage();
            StorageConfig storageConfig = core.getManagers().getStorageConfigManager().getStorageConfigById(storage.getStorageIdConfig());

            //events

            OpenStoragePageActionEvent openStoragePageActionEvent = new OpenStoragePageActionEvent(storageInventory, event);
            core.getManagers().getActionManager().callEvent(openStoragePageActionEvent, storage.getId(), storage);

            storageInventory.onOpen(event);

            //Hopper event
            if(storage.getStorageOriginContext().getContext().equals(StorageOriginContext.Context.BLOCK_STORAGE)){
                List<String> list = storage.getStorageOriginContext().getData();
                BlockMechanicManager.HOPPER_BLOCK_MECHANIC.checkBlockStorageAndTransfer(new String[]{list.get(1),list.get(0),list.get(2)});
            }

            //SOUNDS
            if(storageConfig.isStorageSoundEnabled()){
                for(StorageSoundConfig soundConfig : storageConfig.getStorageSounds()){
                    if(soundConfig.getType().equals(StorageSoundConfig.Type.OPEN)){
                        if(soundConfig.getPagesToSlots().containsKey(storageInventory.getPage())){
                            ((Player)event.getPlayer()).playSound(event.getPlayer().getLocation(),soundConfig.getSound(), soundConfig.getVolume(),soundConfig.getPitch());
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof StorageInventory) {
            StorageInventory storageInventory = (StorageInventory) holder;
            Storage storage = storageInventory.getStorage();
            StorageConfig storageConfig = core.getManagers().getStorageConfigManager().getStorageConfigById(storage.getStorageIdConfig());

            //SAVE STORAGE
            storage.closeStorage(storageInventory.getPage(), (Player) event.getPlayer());

            //events
            CloseStoragePageActionEvent closeStoragePageActionEvent = new CloseStoragePageActionEvent(storageInventory, event);
            core.getManagers().getActionManager().callEvent(closeStoragePageActionEvent, storage.getId(), storage);

            storageInventory.onClose(event);

            //Hopper event
            if(storage.getStorageOriginContext().getContext().equals(StorageOriginContext.Context.BLOCK_STORAGE)){
                List<String> list = storage.getStorageOriginContext().getData();
                BlockMechanicManager.HOPPER_BLOCK_MECHANIC.checkBlockStorageAndTransfer(new String[]{list.get(1),list.get(0),list.get(2)});
            }

            //SOUNDS
            if(storageConfig.isStorageSoundEnabled()){
                for(StorageSoundConfig soundConfig : storageConfig.getStorageSounds()){
                    if(soundConfig.getType().equals(StorageSoundConfig.Type.CLOSE)){
                        if(soundConfig.getPagesToSlots().containsKey(storageInventory.getPage())){
                            ((Player)event.getPlayer()).playSound(event.getPlayer().getLocation(),soundConfig.getSound(), soundConfig.getVolume(),soundConfig.getPitch());
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        if (waitingInput.containsKey(playerId)) {
            event.getPlayer().sendMessage(AdventureUtils.deserializeLegacy(core.getManagers().getConfigManager().getLangDocumentYaml().getString("messages.storage.search_page_enter"),null));
            event.setCancelled(true);
            try {
                int pageNumber = Integer.parseInt(event.getMessage());
                WaitingInputData data = waitingInput.get(playerId);
                Storage storage = data.getStorage();
                if(pageNumber>0 && pageNumber <= storage.getStorageConfig().getPages()){
                    if(event.getPlayer().getLocation().distance(data.getLocation()) < ((SearchPageItemInterface)data.getItemInterface()).getMaxDistance() || ((SearchPageItemInterface)data.getItemInterface()).getMaxDistance() <= 0 ){
                        Bukkit.getScheduler().runTask(core,()-> storage.openStorageR(event.getPlayer(), (pageNumber - 1)));
                    }
                }
                else {
                    event.getPlayer().sendMessage(AdventureUtils.deserializeLegacy(core.getManagers().getConfigManager().getLangDocumentYaml().getString("messages.storage.search_page_invalid"),null));
                }
            } catch (NumberFormatException e) {
                event.getPlayer().sendMessage(AdventureUtils.deserializeLegacy(core.getManagers().getConfigManager().getLangDocumentYaml().getString("messages.storage.search_page_invalid"),null));
            }
            waitingInput.remove(playerId);
            event.setCancelled(true);
        }
    }


    //CHECK ITEM
    public void ClickItemCheck(Storage storage,StorageInventory storageInventory,InventoryClickEvent event,StorageConfig storageConfig){
        HumanEntity humanEntity = event.getWhoClicked();
        ItemStack cursor = event.getCursor();

        if(cursor != null && !cursor.getType().equals(Material.AIR)){

            //ITEM STORAGE
            if(managers.getItemStorageManager().isItemStorage(cursor)){
                String[] src = managers.getItemStorageManager().getDataFromItemStack(cursor).split(":");
                if(src[1].equals(storage.getId())){
                    event.setCancelled(true);
                    return;
                }
                if(!managers.getItemStorageConfigManager().getItemStorageConfig(src[0]).getItemStoragePropertiesConfig().isStorageable()){
                    event.setCancelled(true);
                    return;
                }
            }
            //FURNITURE STORAGE
            if(managers.getFurnitureStorageManager().isShulker(cursor)){
                String[] src = managers.getFurnitureStorageManager().getShulkerData(cursor).split(":");
                managers.getFurnitureStorageConfigManager().findFurnitureStorageConfigById(src[1]).ifPresent(furnitureStorageC -> {
                    if(!furnitureStorageC.getFurnitureStorageProperties().isStorageable()){
                        event.setCancelled(true);
                        return;
                    }
                });
            }
            //BLOCK STORAGE
            if(managers.getBlockStorageManager().isShulker(cursor)){
                String[] src = managers.getBlockStorageManager().getShulkerData(cursor).split(":");
                managers.getBlockStorageConfigManager().findBlockStorageConfigById(src[1]).ifPresent(blockStorageC -> {
                    if(!blockStorageC.getBlockStorageProperties().isStorageable()){
                        event.setCancelled(true);
                        return;
                    }
                });
            }

        }
    }
    public void ShiftClickItemCheck(Storage storage,StorageInventory storageInventory,InventoryClickEvent event,StorageConfig storageConfig){

        HumanEntity humanEntity = event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        int slot = storageInventory.getInventory().firstEmpty();

        if(!clickedItem.getType().equals(Material.AIR)){

            //ITEM STORAGE
            if(managers.getItemStorageManager().isItemStorage(clickedItem)){
                String[] src = managers.getItemStorageManager().getDataFromItemStack(clickedItem).split(":");
                if(src[1].equals(storage.getId())){
                    event.setCancelled(true);
                    return;
                }
                if(!managers.getItemStorageConfigManager().getItemStorageConfig(src[0]).getItemStoragePropertiesConfig().isStorageable()){
                    event.setCancelled(true);
                    return;
                }
            }

            //FURNITURE STORAGE
            if(managers.getFurnitureStorageManager().isShulker(clickedItem)){
                String[] src = managers.getFurnitureStorageManager().getShulkerData(clickedItem).split(":");
                managers.getFurnitureStorageConfigManager().findFurnitureStorageConfigById(src[1]).ifPresent(furnitureStorageC -> {
                    if(!furnitureStorageC.getFurnitureStorageProperties().isStorageable()){
                        event.setCancelled(true);
                        return;
                    }
                });
            }
            //BLOCK STORAGE
            if(managers.getBlockStorageManager().isShulker(clickedItem)){
                String[] src = managers.getBlockStorageManager().getShulkerData(clickedItem).split(":");
                managers.getBlockStorageConfigManager().findBlockStorageConfigById(src[1]).ifPresent(blockStorageC -> {
                    if(!blockStorageC.getBlockStorageProperties().isStorageable()){
                        event.setCancelled(true);
                        return;
                    }
                });
            }
        }

    }
    public void DragItemCheck(Storage storage,StorageInventory storageInventory,InventoryDragEvent event,StorageConfig storageConfig){

        HumanEntity humanEntity = event.getWhoClicked();
        ItemStack cursor = event.getCursor();
        if(cursor == null){
            cursor = (ItemStack) (event.getNewItems().values().toArray())[0];
        }
        if(cursor != null && !cursor.getType().equals(Material.AIR)){

            //ITEM STORAGE
            if(managers.getItemStorageManager().isItemStorage(cursor)){
                String[] src = managers.getItemStorageManager().getDataFromItemStack(cursor).split(":");
                if(src[1].equals(storage.getId())){
                    event.setCancelled(true);
                    return;
                }
                if(!managers.getItemStorageConfigManager().getItemStorageConfig(src[0]).getItemStoragePropertiesConfig().isStorageable()){
                    event.setCancelled(true);
                    return;
                }
            }

            //FURNITURE STORAGE
            if(managers.getFurnitureStorageManager().isShulker(cursor)){
                String[] src = managers.getFurnitureStorageManager().getShulkerData(cursor).split(":");
                managers.getFurnitureStorageConfigManager().findFurnitureStorageConfigById(src[1]).ifPresent(furnitureStorageC -> {
                    if(!furnitureStorageC.getFurnitureStorageProperties().isStorageable()){
                        event.setCancelled(true);
                        return;
                    }
                });
            }
            //BLOCK STORAGE
            if(managers.getBlockStorageManager().isShulker(cursor)){
                String[] src = managers.getBlockStorageManager().getShulkerData(cursor).split(":");
                managers.getBlockStorageConfigManager().findBlockStorageConfigById(src[1]).ifPresent(blockStorageC -> {
                    if(!blockStorageC.getBlockStorageProperties().isStorageable()){
                        event.setCancelled(true);
                        return;
                    }
                });
            }

        }

    }


    public void ClickItemCheckList(Storage storage,StorageInventory storageInventory,InventoryClickEvent event,StorageConfig storageConfig){

        HumanEntity humanEntity = event.getWhoClicked();
        ItemStack cursor = event.getCursor();

        if(cursor != null && !cursor.getType().equals(Material.AIR)){

            if(storageConfig.isStorageItemsWhiteListEnabled()){
                if(!storage.isItemInList(cursor,event.getSlot(),storageInventory.getPage(), Storage.ListType.WHITELIST,storageConfig)){
                    AdventureUtils.playerMessage(storageConfig.getWhiteListMessage(), (Player) humanEntity);
                    event.setCancelled(true);
                }
            }
            if(storageConfig.isStorageItemsBlackListEnabled()){
                if(storage.isItemInList(cursor,event.getSlot(),storageInventory.getPage(), Storage.ListType.BLACKLIST,storageConfig)){
                    AdventureUtils.playerMessage(storageConfig.getBlackListMessage(), (Player) humanEntity);
                    event.setCancelled(true);
                }
            }

        }

    }
    public void ShiftClickItemCheckList(Storage storage,StorageInventory storageInventory,InventoryClickEvent event,StorageConfig storageConfig){

        HumanEntity humanEntity = event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        int slot = storageInventory.getInventory().firstEmpty();

        if(!clickedItem.getType().equals(Material.AIR)){

            if(storageConfig.isStorageItemsWhiteListEnabled()){
                if(!storage.isItemInList(clickedItem,slot,storageInventory.getPage(), Storage.ListType.WHITELIST,storageConfig)){
                    AdventureUtils.playerMessage(storageConfig.getWhiteListMessage(), (Player) humanEntity);
                    event.setCancelled(true);
                }
            }
            if(storageConfig.isStorageItemsBlackListEnabled()){
                if(storage.isItemInList(clickedItem,slot,storageInventory.getPage(), Storage.ListType.BLACKLIST,storageConfig)){
                    AdventureUtils.playerMessage(storageConfig.getBlackListMessage(), (Player) humanEntity);
                    event.setCancelled(true);
                }
            }

        }

    }

    public void DragItemCheckList(Storage storage,StorageInventory storageInventory,InventoryDragEvent event,StorageConfig storageConfig){

        HumanEntity humanEntity = event.getWhoClicked();
        ItemStack cursor = event.getCursor();
        if(cursor == null){
            cursor = (ItemStack) (event.getNewItems().values().toArray())[0];
        }
        if(cursor != null && !cursor.getType().equals(Material.AIR)){

            if(storageConfig.isStorageItemsWhiteListEnabled()){

                for(Integer s : event.getRawSlots()){
                    if(!storage.isItemInList(cursor,s,storageInventory.getPage(), Storage.ListType.WHITELIST,storageConfig)){
                        AdventureUtils.playerMessage(storageConfig.getWhiteListMessage(), (Player) humanEntity);
                        event.setCancelled(true);
                    }
                }
            }
            if(storageConfig.isStorageItemsBlackListEnabled()){

                for(Integer s : event.getRawSlots()){
                    if(storage.isItemInList(cursor,s,storageInventory.getPage(), Storage.ListType.BLACKLIST,storageConfig)){
                        AdventureUtils.playerMessage(storageConfig.getBlackListMessage(), (Player) humanEntity);
                        event.setCancelled(true);
                    }
                }

            }

        }

    }

    public void ClickItemBlocked(Storage storage,StorageInventory storageInventory,InventoryClickEvent event,StorageConfig storageConfig){

        HumanEntity humanEntity = event.getWhoClicked();
        ItemStack cursor = event.getCursor();

        if(cursor != null && !cursor.getType().equals(Material.AIR)){

            if(storageConfig.isStorageBlockItemEnabled()){
                ArrayList<Object> objects = storage.isBlocked(event.getSlot(), storageInventory.getPage(),storageConfig);
                if((boolean) objects.get(0)){
                    if(objects.get(1) != null){
                        AdventureUtils.playerMessage((String) objects.get(1), (Player) humanEntity);
                    }
                    event.setCancelled(true);
                }
            }
        }

    }

    public void ShiftClickItemBlocked(Storage storage,StorageInventory storageInventory,InventoryClickEvent event,StorageConfig storageConfig){

        HumanEntity humanEntity = event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        int slot = storageInventory.getInventory().firstEmpty();

        if(!clickedItem.getType().equals(Material.AIR)){

            if(storageConfig.isStorageBlockItemEnabled()){
                ArrayList<Object> objects = storage.isBlocked(slot, storageInventory.getPage(),storageConfig);
                if((boolean) objects.get(0)){
                    if(objects.get(1) != null){
                        AdventureUtils.playerMessage((String) objects.get(1), (Player) humanEntity);
                    }
                    event.setCancelled(true);
                }
            }

        }

    }
    public void DragItemBlocked(Storage storage,StorageInventory storageInventory,InventoryDragEvent event,StorageConfig storageConfig){

        HumanEntity humanEntity = event.getWhoClicked();
        ItemStack cursor = event.getCursor();
        if(cursor == null){
            cursor = (ItemStack) (event.getNewItems().values().toArray())[0];
        }
        if(cursor != null && !cursor.getType().equals(Material.AIR)){

            if(storageConfig.isStorageBlockItemEnabled()){

                for(Integer s : event.getRawSlots()){
                    ArrayList<Object> objects = storage.isBlocked(s, storageInventory.getPage(),storageConfig);
                    if((boolean) objects.get(0)){
                        if(objects.get(1) != null){
                            AdventureUtils.playerMessage((String) objects.get(1), (Player) humanEntity);
                        }
                        event.setCancelled(true);
                    }
                }
            }

        }

    }

    public Storage getStorage(String id) {
        if(storageMap.containsKey(id)) {
            Storage s = storageMap.get(id);
            s.setLastAccess(new Date());
            return s;
        }
        if(dataManager.getStorageManagerData().existStorageData(id)) {
            Storage s = loadStorage(id);
            s.setLastAccess(new Date());
            return s;
        }
        return null;
    }

    public void removeStorageFromInputWait(String id){
        for(Map.Entry<UUID,WaitingInputData> entry : waitingInput.entrySet()){
            if(id.equals(entry.getValue().getStorage().getId())) waitingInput.remove(entry.getKey());
        }
    }

    public void removeStorage(String id) {
        removeStorageFromInputWait(id);
        if(storageMap.containsKey(id)) storageMap.remove(id);
        if(dataManager.getStorageManagerData().existStorageData(id)) dataManager.getStorageManagerData().removeStorageData(id);
    }

    public boolean storageExists(String id) {
        if(storageMap.containsKey(id)) return true;
        if(dataManager.getStorageManagerData().existStorageData(id)) return true;
        return false;
    }
    //DATA
    public void saveStorageNoRemove(Storage storage){
        dataManager.getStorageManagerData().saveStorageData(storage);
    }
    public void saveStorage(Storage storage, SaveCause saveCause){
        String id = storage.getId();
        if(storage.getStorageConfig().getStorageProperties().isTempStorage()) {
            storageMap.remove(id);
            return;
        }
        if(storageMap.containsKey(id)) storageMap.remove(id);
        if(saveCause.equals(SaveCause.NORMAL_SAVE)){
            Bukkit.getScheduler().runTask(core, storage::closeAllInventory);
        }
        dataManager.getStorageManagerData().saveStorageData(storage);
    }
    public Storage loadStorage(String id){
        if(!storageMap.containsKey(id)){
            Storage storage = dataManager.getStorageManagerData().loadStorageData(id);
            if(storage == null) return null;
            storageMap.put(storage.getId(),storage);
            return storage;
        }
        return null;
    }
    public void saveAllStorages(){
        for(Storage storage : storageMap.values()){
            saveStorageNoRemove(storage);
        }
    }
    public void stop(){
        while(!storageMap.isEmpty()){
            Storage storage = storageMap.values().stream().toList().get(0);
            saveStorage(storage,SaveCause.STOPPING_SAVE);
        }
    }

    public Map<String, Storage> getStorageMap() {
        return storageMap;
    }

    @EventHandler
    public void onDisable(PluginDisableEvent event){
        if(event.getPlugin().equals(core)){
            Bukkit.getOnlinePlayers().forEach(player -> {
                if(player.getOpenInventory().getTopInventory() != null && player.getOpenInventory().getTopInventory().getHolder() instanceof StorageInventory){
                    player.closeInventory();
                }
            });
        }
    }
}