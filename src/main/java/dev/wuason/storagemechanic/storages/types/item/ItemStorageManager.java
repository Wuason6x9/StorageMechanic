package dev.wuason.storagemechanic.storages.types.item;

import dev.wuason.mechanics.Mechanics;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.data.player.PlayerData;
import dev.wuason.storagemechanic.data.player.PlayerDataManager;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageManager;
import dev.wuason.storagemechanic.storages.types.item.config.ItemStorageConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class ItemStorageManager implements Listener {

    private final static NamespacedKey NAMESPACED_KEY = new NamespacedKey(StorageMechanic.getInstance(),"sm_item_storage");
    public final static String STORAGE_CONTEXT = "ITEM_STORAGE";

    private StorageMechanic core;

    public ItemStorageManager(StorageMechanic core) {
        this.core = core;
    }

    @EventHandler
    public void OnInteract(PlayerInteractEvent e){

        if(e.getHand() != null && e.getHand().equals(EquipmentSlot.HAND) && e.hasItem() && !e.getItem().getType().equals(Material.AIR)){
            BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(core,() -> {
                try{
                    if(isItemStorage(e.getItem())){
                        String[] src = getDataFromItemStack(e.getItem()).split(":");
                        StorageManager storageManager = core.getManagers().getStorageManager();
                        if(!storageManager.storageExists(src[1])){
                            removeStorageFromItemStack(e.getItem());
                            removeStoragePlayerData(UUID.fromString(src[2]), src[1]);
                            Bukkit.getScheduler().runTask(core,() -> e.getPlayer().getInventory().remove(e.getItem()));
                            return;
                        }
                        Storage storage = storageManager.getStorage(src[1]);
                        if(!core.getManagers().getItemStorageConfigManager().getItemStorageConfigs().containsKey(src[0])){
                            core.getManagers().getStorageManager().removeStorage(src[1]);
                            removeStorageFromItemStack(e.getItem());
                            removeStoragePlayerData(UUID.fromString(src[2]), src[1]);
                            Bukkit.getScheduler().runTask(core,() -> e.getPlayer().getInventory().remove(e.getItem()));
                            return;
                        }
                        ItemStorageConfig itemStorageConfig = core.getManagers().getItemStorageConfigManager().getItemStorageConfigs().get(src[0]);
                        if(!e.getAction().toString().contains(itemStorageConfig.getItemStorageClickType().toString())) return;
                        Bukkit.getScheduler().runTask(core,() -> storage.openStorage(e.getPlayer(),0));
                        return;
                    }
                    ItemStorageConfig itemStorageConfig = core.getManagers().getItemStorageConfigManager().findItemStorageConfigByItemID(Mechanics.getInstance().getManager().getAdapterManager().getAdapterID(e.getItem()));
                    if(itemStorageConfig == null) return;
                    if(!e.getAction().toString().contains(itemStorageConfig.getItemStorageClickType().toString())) return;
                    Storage storage = core.getManagers().getStorageManager().createStorage(itemStorageConfig.getStorageConfigID());
                    addStoragePlayerData(e.getPlayer().getUniqueId(),storage.getId(),itemStorageConfig.getId());

                    setStorageFromItemStack(e.getItem(),storage.getId(),itemStorageConfig.getId(),e.getPlayer().getUniqueId());

                    Bukkit.getScheduler().runTask(core,() -> storage.openStorage(e.getPlayer(), 0));


                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            });
        }

    }
    public void setStorageFromItemStack(ItemStack itemStack, String storageID, String itemStorageConfigID, UUID owner){
        if(itemStack == null) return;
        String src = itemStorageConfigID + ":" + storageID + ":" + owner;
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.getPersistentDataContainer().set(NAMESPACED_KEY,PersistentDataType.STRING,src);
        itemStack.setItemMeta(itemMeta);
    }
    public void removeStorageFromItemStack(ItemStack itemStack){
        if(itemStack == null) return;
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.getPersistentDataContainer().remove(NAMESPACED_KEY);
        itemStack.setItemMeta(itemMeta);
    }
    public String getDataFromItemStack(ItemStack itemStack){
        if(itemStack == null) return null;
        return itemStack.getItemMeta().getPersistentDataContainer().has(NAMESPACED_KEY, PersistentDataType.STRING) ? itemStack.getItemMeta().getPersistentDataContainer().get(NAMESPACED_KEY, PersistentDataType.STRING) : null;
    }
    public boolean isItemStorage(ItemStack itemStack){
        if(itemStack == null) return false;
        return itemStack.getItemMeta().getPersistentDataContainer().has(NAMESPACED_KEY, PersistentDataType.STRING);
    }
    public void addStoragePlayerData(UUID uuid, String storageId, String itemConfigId){
        PlayerDataManager playerDataManager = core.getManagers().getDataManager().getPlayerDataManager();
        if(playerDataManager.existPlayerData(uuid)){
            PlayerData playerData = playerDataManager.getPlayerData(uuid);
            playerData.getStorages().put(storageId,STORAGE_CONTEXT + "_" + itemConfigId + "_" + storageId);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            if(offlinePlayer != null && !offlinePlayer.isOnline()){
                playerDataManager.savePlayerData(uuid);
            }
        }
    }
    public void removeStoragePlayerData(UUID uuid, String storageID){
        PlayerDataManager playerDataManager = core.getManagers().getDataManager().getPlayerDataManager();
        if(playerDataManager.existPlayerData(uuid)){
            PlayerData playerData = playerDataManager.getPlayerData(uuid);
            playerData.getStorages().remove(storageID);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            if(offlinePlayer != null && !offlinePlayer.isOnline()){
                playerDataManager.savePlayerData(uuid);
            }
        }
    }
}
