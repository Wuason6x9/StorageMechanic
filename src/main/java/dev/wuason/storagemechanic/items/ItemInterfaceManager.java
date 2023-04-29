package dev.wuason.storagemechanic.items;

import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.utils.Adapter;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.customblocks.CustomBlock;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemInterfaceManager {

    private StorageMechanic core;

    private ArrayList<ItemInterface> itemsInterface;

    public ItemInterfaceManager(StorageMechanic core) {
        this.core = core;
    }



    public void loadItemsInterface(){

        itemsInterface = new ArrayList<>();

        File base = new File(Mechanics.getInstance().getMechanicsManager().getMechanic(core).getDirConfig().getPath() + "/itemInterfaces/");
        base.mkdirs();

        File[] files = Arrays.stream(base.listFiles()).filter(f -> {

            if(f.getName().contains(".yml")) return true;

            return false;

        }).toArray(File[]::new);

        for(File file : files){

            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

            ConfigurationSection sectionItemsInterfaces = config.getConfigurationSection("blocks");

            for(Object key : sectionItemsInterfaces.getKeys(false).toArray()){

                ConfigurationSection sectionItemInterface = sectionItemsInterfaces.getConfigurationSection((String)key);
                ItemInterfaceType itemInterfaceType = null;
                try {
                    itemInterfaceType = itemInterfaceType.valueOf(sectionItemInterface.getString("itemType"));
                } catch (IllegalArgumentException e) {
                    AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Item interface! itemInterface_id: " + key + " in file: " + file.getName());
                    AdventureUtils.sendMessagePluginConsole(core, "<red>Error: ItemType is null");
                    continue;
                }

                if(itemInterfaceType == null){
                    AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Item interface! itemInterface_id: " + key + " in file: " + file.getName());
                    AdventureUtils.sendMessagePluginConsole(core, "<red>Error: ItemType is null");
                    continue;
                }

                String item = sectionItemInterface.getString("item");

                if(item == null){
                    AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Item interface! itemInterface_id: " + key + " in file: " + file.getName());
                    AdventureUtils.sendMessagePluginConsole(core, "<red>Error: item is null");
                    continue;
                }
                if(Adapter.getItemStack(item) == null){
                    AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Item interface! itemInterface_id: " + key + " in file: " + file.getName());
                    AdventureUtils.sendMessagePluginConsole(core, "<red>Error: item is null");
                    continue;
                }

                String displayName = sectionItemInterface.getString("displayName");

                List<String> lore = sectionItemInterface.getStringList("lore");

                ItemInterface itemInterface = new ItemInterface(item,displayName,lore,itemInterfaceType,(String)key);

                itemsInterface.add(itemInterface);

            }

        }

    }

    public ItemInterface getItemInterfaceById(String id) {
        for (ItemInterface itemInterface : itemsInterface) {
            if (itemInterface.getId().equals(id)) {
                return itemInterface;
            }
        }
        return null;
    }

    public ItemInterface getItemInterfaceByItemStack(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return null;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer itemDataContainer = itemMeta.getPersistentDataContainer();

        if (itemDataContainer.has(new NamespacedKey(StorageMechanic.getInstance(), "storagemechanicitem"), PersistentDataType.STRING)) {
            String id = itemDataContainer.get(new NamespacedKey(StorageMechanic.getInstance(), "storagemechanicitem"), PersistentDataType.STRING);
            return getItemInterfaceById(id);
        }
        return null;
    }

    public List<ItemInterface> getItemInterfacesByType(ItemInterfaceType type) {
        List<ItemInterface> filteredItems = new ArrayList<>();
        for (ItemInterface itemInterface : itemsInterface) {
            if (itemInterface.getItemInterfaceType() == type) {
                filteredItems.add(itemInterface);
            }
        }
        return filteredItems;
    }

    public boolean existsItemInterface(String id) {
        for (ItemInterface itemInterface : itemsInterface) {
            if (itemInterface.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public boolean isItemInterface(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return false;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer itemDataContainer = itemMeta.getPersistentDataContainer();

        return itemDataContainer.has(new NamespacedKey(StorageMechanic.getInstance(), "storagemechanicitem"), PersistentDataType.STRING);
    }

    public List<ItemInterface> getAllItemInterfaces() {
        return new ArrayList<>(itemsInterface);
    }

}
