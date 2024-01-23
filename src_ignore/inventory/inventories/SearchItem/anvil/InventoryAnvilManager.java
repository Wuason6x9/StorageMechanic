package dev.wuason.storagemechanic.inventory.inventories.SearchItem.anvil;

import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.inventory.inventories.SearchItem.SearchItem;
import dev.wuason.storagemechanic.inventory.inventories.SearchItem.SearchType;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageManager;
import dev.wuason.storagemechanic.storages.config.StorageConfig;
import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class InventoryAnvilManager implements Listener {

    private HashMap<Inventory,AnvilInventory> anvilInventoriesOpened = new HashMap<>();
    private HashMap<Player,AnvilInventory> anvils = new HashMap<>();
    private StorageMechanic core;
    private StorageManager storageManager;

    public InventoryAnvilManager(StorageMechanic core, StorageManager storageManager){
        this.core = core;
        this.storageManager = storageManager;
    }

    public AnvilInventory createAnvilInventory(Player player, String configId, Storage storage, String configIdResultInv, SearchType searchType){

        AnvilInventory anvilInventory = new AnvilInventory(player,configId,storage,configIdResultInv,this,searchType, storageManager,core);
        anvils.put(player,anvilInventory);

        return anvilInventory;
    }


    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        if(anvilInventoriesOpened.containsKey(event.getInventory())){
            AnvilInventory anvilInventory = anvilInventoriesOpened.get(event.getInventory());

        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if(anvilInventoriesOpened.containsKey(event.getInventory())){
            AnvilInventory anvilInventory = anvilInventoriesOpened.get(event.getInventory());
            if(anvilInventory.getAnvilResultInventory() == null){
                if(anvilInventory.getAnvilGui().isBlockClose()){
                    Bukkit.getScheduler().runTaskLater(core,() -> anvilInventory.reOpen(),1L);
                }
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if(anvilInventoriesOpened.containsKey(event.getInventory()) && event.getClickedInventory() != null){
            AnvilInventory anvilInventory = anvilInventoriesOpened.get(event.getInventory());
            if(anvilInventory.getAnvilResultInventory() != null) return;
            event.setCancelled(true);
            if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
                if(event.getCurrentItem() != null && !event.getCurrentItem().equals(Material.AIR) && event.getCurrentItem().getItemMeta() != null && event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(anvilInventory.getNamespacedKeyItemSearch(), PersistentDataType.STRING)){
                    String searchItemId = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(anvilInventory.getNamespacedKeyItemSearch(),PersistentDataType.STRING);
                    SearchItem searchItem = anvilInventory.getResultList().getOrDefault(searchItemId,null);
                    if(searchItem != null){
                        if(searchItem.exist()) {
                            searchItem.removeItemFromStorage();
                            anvilInventory.getItemsToAddInInventory().add(searchItem.getItemStack());
                            anvilInventory.getResultList().remove(searchItemId);
                            event.getWhoClicked().getInventory().setContents(anvilInventory.setResultItemsPage(anvilInventory.getCurrentPage(), event.getWhoClicked().getInventory().getContents()));
                        }
                    }
                }
                if(event.getCurrentItem() != null && !event.getCurrentItem().equals(Material.AIR) && event.getCurrentItem().getItemMeta() != null && event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(anvilInventory.getNamespacedKeyItem(), PersistentDataType.STRING)){
                    String type = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(anvilInventory.getNamespacedKeyItem(),PersistentDataType.STRING);
                    switch (type){
                        case "RESEARCH_ITEM" -> {
                            Bukkit.getScheduler().runTaskAsynchronously(core, () -> {
                                if(anvilInventory.getRenameText() != null && !anvilInventory.isInSearch()) {
                                    anvilInventory.setResultList(new HashMap<>());
                                    anvilInventory.setCurrentPage(1);
                                    anvilInventory.setItemsNull();
                                    anvilInventory.setItems();
                                    anvilInventory.searchItems(anvilInventory.getRenameText().toLowerCase(Locale.ENGLISH),anvilInventory.getSearchType());
                                    anvilInventory.setResultItems();
                                }
                            });

                        }
                        case "CLOSE_ITEM" ->{
                            anvilInventory.getAnvilGui().setBlockClose(false);
                            anvilInventory.close(AnvilInventory.CloseType.NORMAL);
                        }
                        case "NEXT_ITEM" ->{
                            Bukkit.getScheduler().runTaskAsynchronously(core, () -> event.getWhoClicked().getInventory().setContents(anvilInventory.setResultItemsPage(anvilInventory.getCurrentPage() + 1, event.getWhoClicked().getInventory().getContents())));
                        }
                        case "BACK_ITEM" ->{
                            Bukkit.getScheduler().runTaskAsynchronously(core, () -> event.getWhoClicked().getInventory().setContents(anvilInventory.setResultItemsPage(anvilInventory.getCurrentPage() - 1, event.getWhoClicked().getInventory().getContents())));
                        }
                        case "OPEN_RESULT_INV_ITEM" ->{
                            Bukkit.getScheduler().runTaskAsynchronously(core, () -> {
                                try {
                                    anvilInventory.openAnvilResultInventory();
                                }
                                catch (Exception e){
                                    e.printStackTrace();
                                }
                            });
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryDrag(InventoryDragEvent event){
        if(anvils.containsKey((Player)event.getWhoClicked())){
            event.setCancelled(false);
            Inventory inv = event.getInventory();
            for (Integer slot : event.getRawSlots()) {
                if (slot < inv.getSize()) {
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }

    public void stop(){
        Iterator<Map.Entry<Player, AnvilInventory>> it = anvils.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Player, AnvilInventory> entry = it.next();
            entry.getValue().close(AnvilInventory.CloseType.STOP);
        }

    }
    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event){
        if(anvils.containsKey(event.getPlayer())){
            anvils.get(event.getPlayer()).close(AnvilInventory.CloseType.STOP);
        }
    }
    @EventHandler
    public void onPickUp(EntityPickupItemEvent event){
        if(event.getEntityType().equals(EntityType.PLAYER)){
            Player player = (Player) event.getEntity();
            if(anvils.containsKey(player)){
                event.setCancelled(true);
            }
        }
    }

    public HashMap<Inventory, AnvilInventory> getAnvilInventoriesOpened() {
        return anvilInventoriesOpened;
    }

    public HashMap<Player, AnvilInventory> getAnvils() {
        return anvils;
    }
}
