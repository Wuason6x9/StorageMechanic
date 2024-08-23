package dev.wuason.storagemechanic.customitems;

import dev.wuason.mechanics.items.ItemBuilder;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.mechanics.utils.MathUtils;
import dev.wuason.storagemechanic.StorageMechanic;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class CustomItem {
    private String id;
    private String item;
    private ItemStack itemStack;
    private CustomItemProperties customItemProperties;

    public CustomItem(String id, String item, String displayName, List<String> lore, CustomItemProperties customItemProperties) {
        this.id = id;
        this.item = item;
        this.customItemProperties = customItemProperties;
        ItemBuilder ItemBuilder = new ItemBuilder(item,1);
        if(customItemProperties.getSkullTexture() != null) ItemBuilder.setSkullOwner(customItemProperties.getSkullTexture());

        if(lore != null) ItemBuilder.setLore(AdventureUtils.deserializeLegacyList(lore,null));
        if(displayName != null) ItemBuilder.setName(AdventureUtils.deserializeLegacy(displayName,null));

        ItemBuilder.addPersistentData(new NamespacedKey(StorageMechanic.getInstance(),"storagemechanicb"),id);

        itemStack = ItemBuilder.build();
    }

    public ItemStack getItemStack() {
        ItemStack clone = itemStack.clone();
        if(!getCustomBlockProperties().isStackable()){
            ItemMeta itemMeta = clone.getItemMeta();
            itemMeta.getPersistentDataContainer().set(new NamespacedKey(StorageMechanic.getInstance(),"storagemechanicbrandom"), PersistentDataType.INTEGER, MathUtils.randomNumber(0,100000));
            clone.setItemMeta(itemMeta);
        }
        return clone;
    }

    public String getId() {
        return id;
    }

    public String getItem() {
        return item;
    }

    public CustomItemProperties getCustomBlockProperties() {
        return customItemProperties;
    }
}
