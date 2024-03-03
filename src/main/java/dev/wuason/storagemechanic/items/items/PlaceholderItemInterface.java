package dev.wuason.storagemechanic.items.items;

import dev.wuason.mechanics.compatibilities.adapter.Adapter;
import dev.wuason.mechanics.items.ItemBuilderMechanic;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.items.ItemInterface;
import dev.wuason.storagemechanic.items.ItemInterfaceManager;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageManager;
import dev.wuason.storagemechanic.storages.config.StorageConfig;
import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class PlaceholderItemInterface extends ItemInterface {

    private boolean whitelistEnabled;
    private boolean blacklistEnabled;
    private List<String> whitelistItems;
    private List<String> blacklistItems;
    public final static NamespacedKey NAMESPACED_KEY_PLACEHOLDER = new NamespacedKey(StorageMechanic.getInstance(),"smplaceholderitem");

    public PlaceholderItemInterface(String item, String displayName, List<String> lore, boolean whitelistEnabled, boolean blacklistEnabled, List<String> whitelistItems, List<String> blacklistItems, String id) {
        super(item, displayName, lore, id, "PLACEHOLDER");
        this.whitelistEnabled = whitelistEnabled;
        this.blacklistEnabled = blacklistEnabled;
        this.whitelistItems = whitelistItems;
        this.blacklistItems = blacklistItems;
    }

    public static boolean isPlaceholderItem(ItemStack itemStack){
        if(itemStack == null || itemStack.getType().equals(Material.AIR)) return false;
        ItemMeta itemMeta = itemStack.getItemMeta();
        if(itemMeta == null) return false;
        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
        return persistentDataContainer.has(NAMESPACED_KEY_PLACEHOLDER, PersistentDataType.STRING);
    }

    public static ItemStack getOriginalItemStack(ItemStack itemStack){
        if(itemStack == null || itemStack.getType().equals(Material.AIR)) return null;
        return ItemBuilderMechanic.copyOf(itemStack).meta(meta -> {
            PersistentDataContainer itemPersistentDataContainer = meta.getPersistentDataContainer();
            itemPersistentDataContainer.remove(PlaceholderItemInterface.NAMESPACED_KEY_PLACEHOLDER);
            itemPersistentDataContainer.remove(ItemInterfaceManager.NAMESPACED_KEY);
        }).build();
    }

    @Override
    public void onClick(Storage storage, StorageInventory storageInventory, InventoryClickEvent event, StorageConfig storageConfig) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        ItemMeta clickedItemMeta = clickedItem.getItemMeta();
        PersistentDataContainer persistentDataContainer = clickedItemMeta.getPersistentDataContainer();
        if(persistentDataContainer.has(NAMESPACED_KEY_PLACEHOLDER, PersistentDataType.STRING)){
            if(event.getCursor() != null && !event.getCursor().getType().equals(Material.AIR)) {
                ItemStack itemCursor = event.getCursor();
                if(!clickedItem.getType().equals(itemCursor.getType())) return;
                ItemStack itemClickedClone = clickedItem.clone();
                ItemMeta itemMetaClone = itemClickedClone.getItemMeta();
                itemMetaClone.getPersistentDataContainer().remove(ItemInterfaceManager.NAMESPACED_KEY);
                itemMetaClone.getPersistentDataContainer().remove(NAMESPACED_KEY_PLACEHOLDER);
                itemClickedClone.setItemMeta(itemMetaClone);
                if(!itemClickedClone.isSimilar(itemCursor)) return;
                if(itemClickedClone.getMaxStackSize()==itemClickedClone.getAmount() || itemCursor.getAmount()>itemClickedClone.getMaxStackSize()) return;
                if((itemClickedClone.getAmount() + itemCursor.getAmount())>=itemClickedClone.getMaxStackSize()){
                    clickedItem.setAmount(itemClickedClone.getMaxStackSize());
                    if(((itemClickedClone.getAmount() + itemCursor.getAmount()) - itemClickedClone.getMaxStackSize())==0) player.setItemOnCursor(null);
                    else itemCursor.setAmount(((itemClickedClone.getAmount() + itemCursor.getAmount()) - itemClickedClone.getMaxStackSize()));
                    return;
                }
                player.setItemOnCursor(null);
                clickedItem.setAmount((itemClickedClone.getAmount() + itemCursor.getAmount()));
                return;
            };
            if(event.getClick().isRightClick() && clickedItem.getAmount()>1){
                int amount = clickedItem.getAmount() / 2;
                clickedItem.setAmount(clickedItem.getAmount() - amount);
                ItemStack item = clickedItem.clone();
                ItemMeta itemMeta = item.getItemMeta();
                PersistentDataContainer itemPersistentDataContainer = itemMeta.getPersistentDataContainer();
                itemPersistentDataContainer.remove(ItemInterfaceManager.NAMESPACED_KEY);
                itemPersistentDataContainer.remove(NAMESPACED_KEY_PLACEHOLDER);
                item.setItemMeta(itemMeta);
                item.setAmount(amount);
                player.setItemOnCursor(item);
                return;
            }
            ItemStack item = clickedItem.clone();
            ItemStack itemInterfaceCloned = getItemStack();
            clickedItem.setType(itemInterfaceCloned.getType());
            clickedItem.setItemMeta(itemInterfaceCloned.getItemMeta());
            clickedItem.setAmount(itemInterfaceCloned.getAmount());
            ItemMeta itemMeta = item.getItemMeta();
            PersistentDataContainer itemPersistentDataContainer = itemMeta.getPersistentDataContainer();
            itemPersistentDataContainer.remove(ItemInterfaceManager.NAMESPACED_KEY);
            itemPersistentDataContainer.remove(NAMESPACED_KEY_PLACEHOLDER);
            item.setItemMeta(itemMeta);
            player.setItemOnCursor(item);
            return;
        }
        if(event.getCursor() == null || event.getCursor().getType().equals(Material.AIR)) return;
        ItemStack itemCursor = event.getCursor();
        String itemId = Adapter.getInstance().getAdapterID(itemCursor);
        if(whitelistEnabled && !whitelistItems.contains(itemId)) return;
        if(blacklistEnabled && blacklistItems.contains(itemId)) return;
        clickedItem.setType(itemCursor.getType());
        clickedItem.setAmount(itemCursor.getAmount());
        ItemMeta itemMeta = itemCursor.getItemMeta();
        itemMeta.getPersistentDataContainer().set(NAMESPACED_KEY_PLACEHOLDER,PersistentDataType.STRING, itemId);
        itemMeta.getPersistentDataContainer().set(ItemInterfaceManager.NAMESPACED_KEY,PersistentDataType.STRING, getId());
        clickedItem.setItemMeta(itemMeta);
        player.setItemOnCursor(null);
    }

    public boolean isWhitelistEnabled() {
        return whitelistEnabled;
    }

    public boolean isBlacklistEnabled() {
        return blacklistEnabled;
    }

    public List<String> getWhitelistItems() {
        return whitelistItems;
    }

    public List<String> getBlacklistItems() {
        return blacklistItems;
    }
}
