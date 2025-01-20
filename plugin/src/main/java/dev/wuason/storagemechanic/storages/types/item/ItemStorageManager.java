package dev.wuason.storagemechanic.storages.types.item;

import dev.wuason.libs.adapter.Adapter;
import dev.wuason.mechanics.utils.StorageUtils;
import dev.wuason.storagemechanic.Debug;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.data.player.PlayerData;
import dev.wuason.storagemechanic.data.player.PlayerDataManager;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageManager;
import dev.wuason.storagemechanic.storages.StorageOriginContext;
import dev.wuason.storagemechanic.storages.types.item.config.ItemStorageConfig;
import dev.wuason.storagemechanic.storages.types.item.config.ItemStorageConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
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
            Debug.debugToPlayer("ItemStorageManager:OnInteract:1",e.getPlayer());
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
                if(e.getPlayer().isSneaking()) return;
                e.setCancelled(true);
                Bukkit.getScheduler().runTask(core,() -> storage.openStorageR(e.getPlayer(),0));
                return;
            }
            ItemStorageConfig itemStorageConfig = core.getManagers().getItemStorageConfigManager().findItemStorageConfigByItemID(Adapter.getAdapterId(e.getItem()));
            if(itemStorageConfig == null) return;
            if(!e.getAction().toString().contains(itemStorageConfig.getItemStorageClickType().toString())) return;
            if(e.getPlayer().isSneaking()) return;
            Storage storage = core.getManagers().getStorageManager().createStorage(itemStorageConfig.getStorageConfigID(),new StorageOriginContext(StorageOriginContext.Context.ITEM_STORAGE,new ArrayList<>(){{
                add(itemStorageConfig.getId());
                add(e.getPlayer().getUniqueId().toString());
            }}));
            addStoragePlayerData(e.getPlayer().getUniqueId(),storage.getId(),itemStorageConfig.getId());
            ItemStack itemStack = e.getItem().clone();
            e.getItem().setAmount(e.getItem().getAmount() - 1);
            itemStack.setAmount(1);
            setStorageFromItemStack(itemStack,storage.getId(),itemStorageConfig.getId(),e.getPlayer().getUniqueId());
            e.setCancelled(true);
            Bukkit.getScheduler().runTask(core,() -> {
                StorageUtils.addItemToInventoryOrDrop(e.getPlayer(), itemStack);
                storage.openStorageR(e.getPlayer(), 0);
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

    @EventHandler
    public void onItemRemove(ItemDespawnEvent event){
        if(isItemStorage(event.getEntity().getItemStack())){
            String[] src = getDataFromItemStack(event.getEntity().getItemStack()).split(":");
            event.setCancelled(false);
            StorageManager storageManager = core.getManagers().getStorageManager();

            if(storageManager.storageExists(src[1])){
                removeStoragePlayerData(UUID.fromString(src[2]), src[1]);
                storageManager.removeStorage(src[1]);
            }
        }
    }

    @EventHandler
    public void onItemRemove(EntityDamageEvent event){
        if(event.getEntity() instanceof Item && isItemStorage(((Item) event.getEntity()).getItemStack())){
            Item item = (Item) event.getEntity();
            if(item.getItemStack() == null) return;
            String[] src = getDataFromItemStack(item.getItemStack()).split(":");
            String itemStorageConfigID = src[0];
            String storageId = src[1];
            ItemStorageConfigManager itemStorageConfigManager = core.getManagers().getItemStorageConfigManager();
            StorageManager storageManager = core.getManagers().getStorageManager();
            if(!itemStorageConfigManager.existItemStorageConfig(itemStorageConfigID) || !storageManager.storageExists(storageId)) return;
            UUID owner = UUID.fromString(src[2]);
            removeStoragePlayerData(owner, storageId);
            if(itemStorageConfigManager.getItemStorageConfig(itemStorageConfigID).getItemStoragePropertiesConfig().isDropAllItemsOnDeath()){
                Storage storage = storageManager.getStorage(storageId);
                storage.dropAllItems(item.getLocation());
            }
            storageManager.removeStorage(storageId);
            item.remove();
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event){
        if(isItemStorage(event.getItemDrop().getItemStack())){
            ItemStorageConfigManager itemStorageConfigManager = core.getManagers().getItemStorageConfigManager();
            ItemStorageConfig itemStorageConfig = itemStorageConfigManager.getItemStorageConfig(getDataFromItemStack(event.getItemDrop().getItemStack()).split(":")[0]);
            if(itemStorageConfig == null) return;
            if(!itemStorageConfig.getItemStoragePropertiesConfig().isDamageable()){
                event.getItemDrop().setInvulnerable(true);
            }
        }
    }

}
