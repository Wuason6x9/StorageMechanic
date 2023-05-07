package dev.wuason.storagemechanic.customblocks;

import dev.wuason.mechanics.utils.Utils;
import dev.wuason.storagemechanic.StorageMechanic;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class CustomBlock {
    String id;
    Material material;
    ItemStack itemStack;

    public CustomBlock(String id, Material material, String displayName, List<String> lore) {
        this.id = id;
        this.material = material;

        itemStack = Utils.createItemStackByAdapter("mc:" + material.toString(), displayName, lore, 1);

        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.getPersistentDataContainer().set(new NamespacedKey(StorageMechanic.getInstance(),"storagemechanicb"), PersistentDataType.STRING, id);

        itemStack.setItemMeta(itemMeta);
    }

    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    public String getId() {
        return id;
    }


}
