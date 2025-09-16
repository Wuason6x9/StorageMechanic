package dev.wuason.storagemechanic.storages;

import dev.wuason.libs.invmechaniclib.events.CloseEvent;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.storagemechanic.Managers;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.data.DataManager;
import dev.wuason.storagemechanic.data.SaveCause;
import dev.wuason.storagemechanic.items.items.SearchPageItemInterface;
import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StorageManager implements Listener {
    private final StorageMechanic core;
    private final HashMap<String, Storage> storageMap = new HashMap<>();
    private final Map<UUID, WaitingInputData> waitingInput = new ConcurrentHashMap<>();
    private final DataManager dataManager;
    private final Managers managers;
    private static final Long TIME_TO_SAVE = 1200000L;

    public StorageManager(StorageMechanic core, DataManager dataManager, Managers managers) {
        this.core = core;
        this.dataManager = dataManager;
        this.managers = managers;
    }

    public Storage createStorage(String storageIdConfig, StorageOriginContext storageOriginContext) {
        if (core.getManagers().getStorageConfigManager().existsStorageConfig(storageIdConfig)) {
            Storage storage = new Storage(storageIdConfig, storageOriginContext);
            storageMap.put(storage.getId(), storage);
            return storage;
        }
        return null;
    }

    public Storage createStorage(String storageIdConfig, UUID id, StorageOriginContext storageOriginContext) {
        if (core.getManagers().getStorageConfigManager().existsStorageConfig(storageIdConfig)) {
            Storage storage = new Storage(storageIdConfig, id, storageOriginContext);
            storageMap.put(storage.getId(), storage);
            return storage;
        }
        return null;
    }

    public void antiTrashTask() {
        if (storageMap.isEmpty()) return;
        for (Map.Entry<String, Storage> entry : storageMap.entrySet()) {
            if (entry.getValue().getLastOpen().getTime() + TIME_TO_SAVE < System.currentTimeMillis() && entry.getValue().getLastAccess().getTime() + TIME_TO_SAVE < System.currentTimeMillis()) {
                saveStorage(entry.getValue(), SaveCause.NORMAL_SAVE);
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof StorageInventory storageInventory) {
            storageInventory.onDrag(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof StorageInventory storageInventory) storageInventory.onClick(event);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof StorageInventory storageInventory) {
            storageInventory.onOpen(event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof StorageInventory storageInventory) {
            CloseEvent closeEvent = new CloseEvent(event);
            storageInventory.onClose(closeEvent);
            if (closeEvent.isCancelled()) {
                Bukkit.getScheduler().runTask(core, () -> {
                    event.getPlayer().openInventory(storageInventory.getInventory());
                });
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        if (waitingInput.containsKey(playerId)) {
            event.getPlayer().sendMessage(AdventureUtils.deserializeLegacy(core.getManagers().getConfigManager().getLangDocumentYaml().getString("messages.storage.search_page_enter"), null));
            event.setCancelled(true);
            try {
                int pageNumber = Integer.parseInt(event.getMessage());
                WaitingInputData data = waitingInput.get(playerId);
                Storage storage = data.getStorage();
                if (pageNumber > 0 && pageNumber <= storage.getStorageConfig().getPages()) {
                    if (event.getPlayer().getLocation().distance(data.getLocation()) < ((SearchPageItemInterface) data.getItemInterface()).getMaxDistance() || ((SearchPageItemInterface) data.getItemInterface()).getMaxDistance() <= 0) {
                        Bukkit.getScheduler().runTask(core, () -> storage.openStorageR(event.getPlayer(), (pageNumber - 1)));
                    }
                } else {
                    event.getPlayer().sendMessage(AdventureUtils.deserializeLegacy(core.getManagers().getConfigManager().getLangDocumentYaml().getString("messages.storage.search_page_invalid"), null));
                }
            } catch (NumberFormatException e) {
                event.getPlayer().sendMessage(AdventureUtils.deserializeLegacy(core.getManagers().getConfigManager().getLangDocumentYaml().getString("messages.storage.search_page_invalid"), null));
            }
            waitingInput.remove(playerId);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDisable(PluginDisableEvent event) {
        if (event.getPlugin().equals(core)) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (player.getOpenInventory().getTopInventory() != null && player.getOpenInventory().getTopInventory().getHolder() instanceof StorageInventory) {
                    player.closeInventory();
                }
            });
        }
    }


    /**
     * Returns the storage with the given ID.
     *
     * @param id the ID of the storage to retrieve
     * @return the storage object if found, or null if not found
     */
    public Storage getStorage(String id) {
        if (storageMap.containsKey(id)) {
            Storage s = storageMap.get(id);
            s.setLastAccess(new Date());
            return s;
        }
        if (dataManager.getStorageManagerData().existStorageData(id)) {
            Storage s = loadStorage(id);
            s.setLastAccess(new Date());
            return s;
        }
        return null;
    }

    /**
     * Removes the storage with the given ID from the waiting input data.
     *
     * @param id the ID of the storage to remove
     */
    public void removeStorageFromInputWait(String id) {
        for (Map.Entry<UUID, WaitingInputData> entry : waitingInput.entrySet()) {
            if (id.equals(entry.getValue().getStorage().getId())) waitingInput.remove(entry.getKey());
        }
    }

    /**
     * Removes the storage with the given ID.
     *
     * @param id the ID of the storage to remove
     */
    public void removeStorage(String id) {
        removeStorageFromInputWait(id);
        if (storageMap.containsKey(id)) storageMap.remove(id);
        if (dataManager.getStorageManagerData().existStorageData(id))
            dataManager.getStorageManagerData().removeStorageData(id);
    }

    /**
     * Checks if a storage with the given ID exists.
     *
     * @param id the ID of the storage
     * @return true if the storage exists, false otherwise
     */
    public boolean storageExists(String id) {
        if (storageMap.containsKey(id)) return true;
        if (dataManager.getStorageManagerData().existStorageData(id)) return true;
        return false;
    }

    /**
     * Saves the given storage object to the data manager without removing any existing data.
     * The storage data is obtained from the data manager and then the provided storage object is saved.
     *
     * @param storage the storage object to be saved
     */
    //DATA
    public void saveStorageNoRemove(Storage storage) {
        dataManager.getStorageManagerData().saveStorageData(storage);
    }

    /**
     *
     */
    public void saveStorage(Storage storage, SaveCause saveCause) {
        String id = storage.getId();
        if (storage.getStorageConfig().getStorageProperties().isTempStorage()) {
            storageMap.remove(id);
            return;
        }
        if (storageMap.containsKey(id)) storageMap.remove(id);
        if (saveCause.equals(SaveCause.NORMAL_SAVE)) {
            Bukkit.getScheduler().runTask(core, storage::closeAllInventory);
        }
        dataManager.getStorageManagerData().saveStorageData(storage);
    }

    /**
     * Loads a storage with the specified ID from the storage map.
     *
     * @param id the ID of the storage to load
     * @return the loaded storage, or null if the storage is not found or failed to load
     */
    public Storage loadStorage(String id) {
        if (!storageMap.containsKey(id)) {
            Storage storage = dataManager.getStorageManagerData().loadStorageData(id);
            if (storage == null) return null;
            storageMap.put(storage.getId(), storage);
            return storage;
        }
        return null;
    }

    /**
     * Saves all storages.
     * <p>
     * This method iterates through all the storages in the storageMap and saves each storage using the saveStorageNoRemove method.
     */
    public void saveAllStorages() {
        for (Storage storage : storageMap.values()) {
            saveStorageNoRemove(storage);
        }
    }

    /**
     * Stops the process of saving all the storages.
     * This method will iterate through the storage map and save each storage with the save cause 'STOPPING_SAVE'.
     */
    public void stop() {
        while (!storageMap.isEmpty()) {
            Storage storage = storageMap.values().stream().toList().get(0);
            saveStorage(storage, SaveCause.STOPPING_SAVE);
        }
    }

    /**
     * Retrieves the map of storage objects.
     *
     * @return A map object representing the storage map. The keys are strings representing the storage IDs,
     * and the values are Storage objects representing the actual storage data.
     */
    public Map<String, Storage> getStorageMap() {
        return storageMap;
    }

    public Map<UUID, WaitingInputData> getWaitingInput() {
        return waitingInput;
    }
}