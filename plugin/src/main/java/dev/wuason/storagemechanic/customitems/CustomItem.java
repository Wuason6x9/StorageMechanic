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
import java.util.Objects;

public class CustomItem {
    private final String id;
    private final String item;
    private final CustomItemProperties customItemProperties;
    private final String displayName;
    private final List<String> lore;

    private static final NamespacedKey CUSTOM_ITEM_KEY = new NamespacedKey(StorageMechanic.getInstance(), "storagemechanicb");
    private static final NamespacedKey UNIQUE_KEY = new NamespacedKey(StorageMechanic.getInstance(), "storagemechanicbrandom");

    public CustomItem(String id, String item, String displayName, List<String> lore, CustomItemProperties customItemProperties) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.item = Objects.requireNonNull(item, "item cannot be null");
        this.customItemProperties = Objects.requireNonNull(customItemProperties, "customItemProperties cannot be null");
        this.displayName = displayName;
        this.lore = lore;
    }

    public ItemStack getItemStack() {
        ItemBuilder itemBuilder = new ItemBuilder(item, 1);
        String skullTexture = customItemProperties.getSkullTexture();
        if (skullTexture != null && !skullTexture.isEmpty()) {
            itemBuilder.setSkullOwner(skullTexture);
        }
        if (displayName != null && !displayName.isEmpty()) {
            itemBuilder.setName(AdventureUtils.deserializeLegacy(displayName, null));
        }
        if (lore != null && !lore.isEmpty()) {
            itemBuilder.setLore(AdventureUtils.deserializeLegacyList(lore, null));
        }
        itemBuilder.addPersistentData(CUSTOM_ITEM_KEY, id);
        ItemStack result = itemBuilder.build();
        if (!customItemProperties.isStackable()) {
            ItemMeta meta = result.getItemMeta();
            if (meta != null) {
                int randomValue = MathUtils.randomNumber(0, 100000);
                meta.getPersistentDataContainer().set(UNIQUE_KEY, PersistentDataType.INTEGER, randomValue);
                result.setItemMeta(meta);
            }
        }
        return result;
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