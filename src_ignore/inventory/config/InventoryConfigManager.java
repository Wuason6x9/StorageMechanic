package dev.wuason.storagemechanic.inventory.config;

import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.compatibilities.adapter.Adapter;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.utils.StorageUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryType;

import java.io.File;
import java.util.*;

public class InventoryConfigManager {
    private StorageMechanic core;

    public InventoryConfigManager(StorageMechanic core) {
        this.core = core;
    }

    private HashMap<String,InventoryConfig> inventories = new HashMap<>();


    public void loadInventoriesConfig(){

        inventories = new HashMap<>();

        File base = new File(core.getDataFolder() + "/inventories/");
        base.mkdirs();

        File[] files = Arrays.stream(base.listFiles()).filter(f -> {

            if(f.getName().contains(".yml")) return true;

            return false;

        }).toArray(File[]::new);

        for(File file : files){

            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);


            String id = config.getString("inventory.id","_");
            if(!StorageUtils.isValidConfigId(id)){
                AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Inventory Config! Inventory_id: " + id + " in file: " + file.getName());
                AdventureUtils.sendMessagePluginConsole(core, "<red>Error: The Inventory id cannot contain \"_\" in the id!");
                continue;
            }

            InventoryType inventoryType = null;
            try {
                inventoryType = InventoryType.valueOf(config.getString("inventory.type","CHEST").toUpperCase(Locale.ENGLISH));
            }
            catch (Exception e){
                AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Inventory Config! Inventory_id: " + id + " in file: " + file.getName());
                AdventureUtils.sendMessagePluginConsole(core, "<red>Error: The Inventory type is invalid!");
                continue;
            }
            int rows = config.getInt("inventory.rows", 3);
            String title = config.getString("inventory.title", "<red>--- UNNAMED ---");


            Set<Integer> blockedSlots = new HashSet<>(StorageUtils.configFill((List<String>) config.getList("inventory.slots.blocked_slots",new ArrayList<>())));
            Set<Integer> dataSlots = new HashSet<>(StorageUtils.configFill((List<String>) config.getList("inventory.slots.data_slots",new ArrayList<>())));

            HashMap<String,ItemInventoryConfig> items = new HashMap<>();
            ConfigurationSection itemsConfigurationSection = config.getConfigurationSection("items");
            if(itemsConfigurationSection != null){

                for(String key : itemsConfigurationSection.getKeys(false)){

                    ConfigurationSection itemConfigurationSection = itemsConfigurationSection.getConfigurationSection(key);

                    String itemId = key;

                    String item = itemConfigurationSection.getString("item","mc:stone");
                    if(!Adapter.getInstance().existAdapterID(item)){
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Item in Inventory Config! item_id: " + itemId + " Inventory_id: " + id + " in file: " + file.getName());
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error: The Item Adapter is invalid! itemAdapter: " + item);
                        continue;
                    }
                    int amount = itemConfigurationSection.getInt("amount",1);
                    String display = itemConfigurationSection.getString("display");
                    List<String> lore = itemConfigurationSection.getStringList("lore");
                    String type = itemConfigurationSection.getString("type").toUpperCase(Locale.ENGLISH);

                    ItemInventoryConfig itemInventoryConfig = new ItemInventoryConfig(itemId,item,amount,display,lore,type);
                    items.put(key,itemInventoryConfig);
                }

            }
            //AdventureUtils.sendMessagePluginConsole(core, "<aqua> Items loaded: <yellow>" + items.size() + "<aqua> in inventory: <yellow>" + id);
            HashMap<Integer,ItemInventoryConfig> itemsSlots = new HashMap<>();
            for(String itemSlot : (List<String>)config.getList("inventory.slots.items",new ArrayList<>())){

                String[] src = itemSlot.split(":");

                String itemId = src[0];
                Set<Integer> slots = new HashSet<>(StorageUtils.configFill(Collections.singletonList(src[1])));

                for(int s : slots){
                    ItemInventoryConfig itemInventoryConfig = items.get(itemId);
                    itemInventoryConfig.setSlot(s);
                    itemsSlots.put(s,itemInventoryConfig);
                }

            }

            InventoryConfig inventoryConfig = new InventoryConfig(inventoryType,rows,title,blockedSlots,dataSlots,itemsSlots,id);

            inventories.put(id,inventoryConfig);
        }


        AdventureUtils.sendMessagePluginConsole(core, "<aqua> Inventories loaded: <yellow>" + inventories.size());

    }

    public List<ItemInventoryConfig> getAllItemsByType(String configId, String type){
        List<ItemInventoryConfig> list = new ArrayList<>();
        for(ItemInventoryConfig item : inventories.get(configId).getItemsInventory().values()){
            if(item.getType().toUpperCase().equals(type.toUpperCase(Locale.ENGLISH))) list.add(item);
        }
        return list;
    }

    public HashMap<String, InventoryConfig> getInventories() {
        return inventories;
    }


}