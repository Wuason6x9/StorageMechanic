package dev.wuason.storagemechanic.customblocks;

import dev.wuason.mechanics.items.ItemBuilderMechanic;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.mechanics.utils.Utils;
import dev.wuason.storagemechanic.StorageMechanic;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class CustomBlock {
    private String id;
    private String item;
    private ItemStack itemStack;
    private CustomBlockProperties customBlockProperties;

    public CustomBlock(String id, String item, String displayName, List<String> lore, CustomBlockProperties customBlockProperties) {
        this.id = id;
        this.item = item;
        this.customBlockProperties = customBlockProperties;
        ItemBuilderMechanic itemBuilderMechanic = new ItemBuilderMechanic(item,1);

        if(lore != null) itemBuilderMechanic.setLore(AdventureUtils.deserializeLegacyList(lore,null));
        if(displayName != null) itemBuilderMechanic.setName(AdventureUtils.deserializeLegacy(displayName,null));

        itemBuilderMechanic.addPersistentData(new NamespacedKey(StorageMechanic.getInstance(),"storagemechanicb"),id);

        itemStack = itemBuilderMechanic.build();
    }

    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    public String getId() {
        return id;
    }

    public String getItem() {
        return item;
    }

    public CustomBlockProperties getCustomBlockProperties() {
        return customBlockProperties;
    }
}
