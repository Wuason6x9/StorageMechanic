package dev.wuason.storagemechanic.items;
import dev.wuason.mechanics.compatibilities.adapter.Adapter;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.items.items.*;
import dev.wuason.storagemechanic.utils.StorageUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.*;

public class ItemInterfaceManager {
    private StorageMechanic core;
    private HashMap<String, ItemInterface> itemInterfaceHashMap = new HashMap<>();
    public final static NamespacedKey NAMESPACED_KEY = new NamespacedKey(StorageMechanic.getInstance(),"storagemechanicitem");
    public static final List<String> ITEMS_REGISTERED_LIST = new ArrayList<>(){{
        add("NEXT_PAGE");
        add("BACK_PAGE");
        add("SEARCH_PAGE");
        //add("SORT_ITEMS");
        add("BLOCKED_ITEM");
        add("SEARCH_ITEM");
        add("CLEAN_ITEM");
        add("DROP_ITEMS");
        add("PLACEHOLDER");
        add("ACTION");
    }};

    public ItemInterfaceManager(StorageMechanic core) {
        this.core = core;
    }



    public void loadItemsInterface(){

        itemInterfaceHashMap = new HashMap<>();

        File base = new File(core.getDataFolder() + "/itemInterfaces/");
        base.mkdirs();

        File[] files = Arrays.stream(base.listFiles()).filter(f -> {

            if(f.getName().contains(".yml")) return true;

            return false;

        }).toArray(File[]::new);

        for(File file : files){

            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

            ConfigurationSection sectionItemsInterfaces = config.getConfigurationSection("Items");

            if(sectionItemsInterfaces != null){
                for(String key : sectionItemsInterfaces.getKeys(false)){

                    ConfigurationSection sectionItemInterface = sectionItemsInterfaces.getConfigurationSection((String)key);

                    String itemType = sectionItemInterface.getString("itemType", ".").toUpperCase(Locale.ENGLISH);

                    if(!ITEMS_REGISTERED_LIST.contains(itemType)){
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Item interface! itemInterface_id: " + key + " in file: " + file.getName());
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error: ItemType is invalid or null");
                        continue;
                    }

                    String item = sectionItemInterface.getString("item");

                    if(item == null){
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Item interface! itemInterface_id: " + key + " in file: " + file.getName());
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error: item is null");
                        continue;
                    }
                    if(!Adapter.getInstance().existAdapterID(item)){
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Item interface! itemInterface_id: " + key + " in file: " + file.getName());
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error: item is null");
                        continue;
                    }

                    String displayName = sectionItemInterface.getString("displayName");
                    List<String> lore = sectionItemInterface.getStringList("lore");

                    switch (itemType){
                        case "NEXT_PAGE" -> {
                            itemInterfaceHashMap.put(key, new NextPageItemInterface(item, displayName, lore, key));
                            continue;
                        }
                        case "BACK_PAGE" -> {
                            itemInterfaceHashMap.put(key, new BackPageItemInterface(item, displayName, lore, key));
                            continue;
                        }
                        case "SEARCH_PAGE" -> {
                            double maxDistance = sectionItemInterface.getDouble("properties.maxDistance", 5.0);
                            itemInterfaceHashMap.put(key, new SearchPageItemInterface(item, displayName, lore, maxDistance, key));
                            continue;
                        }
                        case "ACTION" -> {
                            String actionId = sectionItemInterface.getString("properties.action_id");
                            if(actionId == null || !core.getManagers().getActionManager().isActionConfigRegistered(actionId)){
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Item interface! itemInterface_id: " + key + " in file: " + file.getName());
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error: Action id is null or invalid!");
                                continue;
                            }
                            itemInterfaceHashMap.put(key, new ActionItemInterface(item, displayName, lore, actionId, key));
                            continue;
                        }
                        case "BLOCKED_ITEM" -> {
                            itemInterfaceHashMap.put(key, new BlockedItemInterface(item, displayName, lore, key));
                            continue;
                        }
                        case "PLACEHOLDER" -> {
                            List<String> whitelistItems = sectionItemInterface.getStringList("properties.whitelist.list");
                            List<String> blacklistItems = sectionItemInterface.getStringList("properties.blacklist.list");
                            boolean whitelistEnabled = sectionItemInterface.getBoolean("properties.whitelist.enabled", false);
                            boolean blacklistEnabled = sectionItemInterface.getBoolean("properties.blacklist.enabled", false);
                            if(whitelistItems == null) whitelistEnabled = false;
                            if(blacklistItems == null) blacklistEnabled = false;
                            Adapter adapterManager = Adapter.getInstance();
                            //COMPUTE ITEMS
                            List<String> itemsBlackListComputed = new ArrayList<>();
                            if(blacklistEnabled){
                                for(String i : blacklistItems){
                                    itemsBlackListComputed.add(adapterManager.getAdapterID(adapterManager.getItemStack(i)));
                                }
                            }
                            List<String> itemsWhiteListComputed = new ArrayList<>();
                            if(whitelistEnabled){
                                for(String i : whitelistItems){
                                    itemsWhiteListComputed.add(adapterManager.getAdapterID(adapterManager.getItemStack(i)));
                                }
                            }
                            itemInterfaceHashMap.put(key, new PlaceholderItemInterface(item, displayName, lore, whitelistEnabled, blacklistEnabled, itemsWhiteListComputed, itemsBlackListComputed, key));
                            continue;
                        }

                        case "CLEAN_ITEM" -> {
                            List<String> pagesString = sectionItemInterface.getStringList("properties.pages");
                            List<String> slotsString = sectionItemInterface.getStringList("properties.slots");
                            if(slotsString == null || pagesString == null){
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Item interface! itemInterface_id: " + key + " in file: " + file.getName());
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error: CLEAN_ITEM slots or pages is invalid!");
                                continue;
                            }
                            itemInterfaceHashMap.put(key, new CleanItemsItemInterface(item, displayName, lore, StorageUtils.configFill(pagesString), StorageUtils.configFill(slotsString), key));
                            continue;
                        }

                        case "DROP_ITEMS" -> {

                            String dropItemsTypeString = sectionItemInterface.getString("properties.def-action");
                            String inventoryConfigId = sectionItemInterface.getString("properties.inv-id");

                            itemInterfaceHashMap.put(key, new DropItemsItemInterface(item, displayName, lore, key, dropItemsTypeString != null ? DropItemsItemInterface.DropItemsType.valueOf(dropItemsTypeString.toUpperCase(Locale.ENGLISH)) : null, inventoryConfigId));
                        }

                        case "SEARCH_ITEM" -> {
                            String invId = sectionItemInterface.getString("properties.inv-id", "search-item");
                            String invResultId = sectionItemInterface.getString("properties.inv-result-id");
                            String searchTypeString = sectionItemInterface.getString("properties.def-action");
                            if(invId == null || invResultId == null){
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Item interface! itemInterface_id: " + key + " in file: " + file.getName());
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error: SEARCH_ITEM invId, invResultId or searchType is invalid!");
                                continue;
                            }


                            SearchItemsItemInterface.SearchType searchType = null;
                            try {
                                if(searchTypeString != null) searchType = SearchItemsItemInterface.SearchType.valueOf(searchTypeString.toUpperCase(Locale.ENGLISH));
                            } catch (IllegalArgumentException e) {
                            }

                            String searchInputString = sectionItemInterface.getString("properties.search-input", "ANVIL");

                            SearchItemsItemInterface.SearchInput searchInput = null;
                            try {
                                searchInput = SearchItemsItemInterface.SearchInput.valueOf(searchInputString.toUpperCase(Locale.ENGLISH));
                            } catch (IllegalArgumentException e) {
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Item interface! itemInterface_id: " + key + " in file: " + file.getName());
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error: SEARCH_ITEM searchInput is invalid!");
                                continue;
                            }

                            String invAnvilId = sectionItemInterface.getString("properties.inv-anvil-id");

                            itemInterfaceHashMap.put(key, new SearchItemsItemInterface(item, displayName, lore, key, invId, invResultId, searchType, searchInput, invAnvilId));
                        }
                    }
                }
            }
        }

        AdventureUtils.sendMessagePluginConsole(core, "<aqua> Items Interface loaded: <yellow>" + itemInterfaceHashMap.size());

    }

    public ItemInterface getItemInterfaceById(String id) {
        return itemInterfaceHashMap.get(id);
    }

    public boolean existsItemInterface(String id) {
        return itemInterfaceHashMap.containsKey(id);
    }

    public boolean isItemInterface(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return false;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer itemDataContainer = itemMeta.getPersistentDataContainer();
        return itemDataContainer.has(NAMESPACED_KEY, PersistentDataType.STRING);
    }

    public ItemInterface getItemInterfaceByItemStack(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) return null;
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer itemDataContainer = itemMeta.getPersistentDataContainer();
        if (!itemDataContainer.has(NAMESPACED_KEY, PersistentDataType.STRING)) return null;
        String id = itemDataContainer.get(NAMESPACED_KEY, PersistentDataType.STRING);
        return getItemInterfaceById(id);
    }

}
