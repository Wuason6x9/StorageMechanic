package dev.wuason.storagemechanic.items;

import dev.wuason.mechanics.utils.Utils;
import dev.wuason.storagemechanic.StorageMechanic;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ItemInterface {
    private ItemStack itemStack;
    private ItemInterfaceType itemInterfaceType;
    private String id;

    public ItemInterface(String item, String displayName, List<String> lore, ItemInterfaceType itemInterfaceType, String id) {


        itemStack = Utils.createItemStackByAdapter(item, displayName, lore, 1);

        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.getPersistentDataContainer().set(new NamespacedKey(StorageMechanic.getInstance(),"storagemechanicitem"), PersistentDataType.STRING, id);

        itemStack.setItemMeta(itemMeta);
        this.itemInterfaceType = itemInterfaceType;
        this.id = id;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ItemInterfaceType getItemInterfaceType() {
        return itemInterfaceType;
    }

    public String getId() {
        return id;
    }
}
