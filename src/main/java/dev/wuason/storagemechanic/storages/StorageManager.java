package dev.wuason.storagemechanic.storages;

import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.items.ItemInterface;
import dev.wuason.storagemechanic.items.ItemInterfaceManager;
import dev.wuason.storagemechanic.storages.config.StorageConfig;
import dev.wuason.storagemechanic.storages.config.StorageSoundConfig;
import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class StorageManager implements Listener{
    private StorageMechanic core;
    private Map<String, Storage> storageMap = new HashMap<>();
    private Map<UUID, WaitingInputData> waitingInput = new ConcurrentHashMap<>();

    public StorageManager(StorageMechanic core) {
        this.core = core;
    }

    public Storage createStorage(String storageIdConfig) {
        if(core.getManagers().getStorageConfigManager().existsStorageConfig(storageIdConfig)){
            Storage storage = new Storage(storageIdConfig);
            storageMap.put(storage.getId(), storage);
            return storage;
        }
        return null;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof StorageInventory) {
            StorageInventory storageInventory = (StorageInventory) holder;
            Storage storage = storageInventory.getStorage();
            StorageConfig storageConfig = core.getManagers().getStorageConfigManager().getStorageConfigById(storage.getStorageIdConfig());
            //ITEMS INTERFACES
            if (event.getClickedInventory() != null && !event.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
                // Cancel event if clicked item is an interface item
                ItemStack clickedItem = event.getCurrentItem();
                ItemInterfaceManager itemInterfaceManager = core.getManagers().getItemInterfaceManager();
                if (clickedItem != null && itemInterfaceManager.isItemInterface(clickedItem)) {
                    event.setCancelled(true);
                    ItemInterface itemInterface = core.getManagers().getItemInterfaceManager().getItemInterfaceByItemStack(clickedItem);
                    ClickItemInterface(storage,storageInventory,event,storageConfig,itemInterface);
                    return;
                }
            }
            //SOUNDS
            if(storageConfig.isStorageSoundEnabled()){
                for(StorageSoundConfig soundConfig : storageConfig.getStorageSounds()){
                    if(soundConfig.getType().equals(StorageSoundConfig.type.CLICK)){
                        if(soundConfig.getPages().contains(storageInventory.getPage())){
                            if(soundConfig.getSlots().size()>0){
                                if(event.getClickedInventory() != null && !event.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
                                    if (soundConfig.getSlots().contains(event.getSlot())) {
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
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof StorageInventory) {
            StorageInventory storageInventory = (StorageInventory) holder;
            Storage storage = storageInventory.getStorage();
            StorageConfig storageConfig = core.getManagers().getStorageConfigManager().getStorageConfigById(storage.getStorageIdConfig());

            //SOUNDS
            if(storageConfig.isStorageSoundEnabled()){
                for(StorageSoundConfig soundConfig : storageConfig.getStorageSounds()){
                    if(soundConfig.getType().equals(StorageSoundConfig.type.OPEN)){
                        if(soundConfig.getPages().contains(storageInventory.getPage())){
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
            storage.closeStorage(storageInventory.getPage());

            //SOUNDS
            if(storageConfig.isStorageSoundEnabled()){
                for(StorageSoundConfig soundConfig : storageConfig.getStorageSounds()){
                    if(soundConfig.getType().equals(StorageSoundConfig.type.CLOSE)){
                        if(soundConfig.getPages().contains(storageInventory.getPage())){
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
            event.getPlayer().sendMessage(AdventureUtils.deserializeLegacy(core.getManagers().getConfigManager().getConfig().getString("messages.storage.search_page_enter")));
            try {
                event.setCancelled(true);
                int pageNumber = Integer.parseInt(event.getMessage());
                WaitingInputData data = waitingInput.get(playerId);
                Storage storage = data.getStorage();
                if(pageNumber>0 && pageNumber <= storage.getStorageConfig().getPages()){
                    Bukkit.getScheduler().runTask(core,()-> storage.openStorage(event.getPlayer(), (pageNumber - 1)));
                }
                else {
                    event.getPlayer().sendMessage(AdventureUtils.deserializeLegacy(core.getManagers().getConfigManager().getConfig().getString("messages.storage.search_page_invalid")));
                }
            } catch (NumberFormatException e) {
                event.getPlayer().sendMessage(AdventureUtils.deserializeLegacy(core.getManagers().getConfigManager().getConfig().getString("messages.storage.search_page_invalid")));
            }
            waitingInput.remove(playerId);
            event.setCancelled(true);
        }
    }

    private void ClickItemInterface(Storage storage,StorageInventory storageInventory,InventoryClickEvent event,StorageConfig storageConfig,ItemInterface itemInterface){
        switch (itemInterface.getItemInterfaceType()){
            case SEARCH_PAGE -> {
                Player player = (Player) event.getWhoClicked();
                UUID playerId = player.getUniqueId();
                waitingInput.put(playerId, new WaitingInputData(storage, storageInventory.getPage()));

                AtomicLong timer = new AtomicLong(0);
                Bukkit.getScheduler().runTaskTimer(core, task -> {
                    if (timer.incrementAndGet() >= 20 * 10 || !waitingInput.containsKey(playerId)) {
                        waitingInput.remove(playerId);
                        task.cancel();
                    }
                }, 0L, 1L);
            }
            case NEXT_PAGE -> {
                if(storageInventory.getPage()<(storageConfig.getPages() - 1)){
                    storage.openStorage((Player) event.getWhoClicked(),storageInventory.getPage() + 1);
                }
            }
            case BACK_PAGE -> {
                if(storageInventory.getPage() > 0){
                    storage.openStorage((Player) event.getWhoClicked(),storageInventory.getPage() - 1);
                }
            }
            default ->{

            }
        }
    }

    public Storage getStorage(String id) {
        return storageMap.get(id);
    }

    public void removeStorage(String id) {
        storageMap.remove(id);
    }

    public Storage storageSerializableToStorage(StorageSerializable storageSerializable) {
        Storage storage = new Storage(storageSerializable.getId(), storageSerializable.getItems(), storageSerializable.getStorageIdConfig(), storageSerializable.getDate());return storage;
    }

    public StorageSerializable storageToStorageSerializable(Storage storage) {
        return new StorageSerializable(storage.getItems(), storage.getId(), storage.getStorageIdConfig(), storage.getDate());
    }

    public boolean storageExists(String id) {
        return storageMap.containsKey(id);
    }

    public Map<String, Storage> getStorageMap() {
        return storageMap;
    }
}