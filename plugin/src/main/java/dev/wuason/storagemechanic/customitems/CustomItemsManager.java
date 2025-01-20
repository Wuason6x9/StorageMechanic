package dev.wuason.storagemechanic.customitems;

import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.api.events.block.CustomBlockDestroyEvent;
import dev.wuason.storagemechanic.api.events.block.CustomBlockInteractEvent;
import dev.wuason.storagemechanic.api.events.block.CustomBlockPlaceEvent;
import dev.wuason.storagemechanic.utils.StorageUtils;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.NamespacedKey;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;


import java.io.File;
import java.util.*;

public class CustomItemsManager implements Listener {

    private StorageMechanic core;
    private HashMap<String, CustomItem> customItems = new HashMap<>();

    public CustomItemsManager(StorageMechanic core) {
        this.core = core;
    }

    public void loadCustomBlocks(){

        customItems = new HashMap<>();

        File base = new File(core.getDataFolder().getPath() + "/CustomItems/");
        base.mkdirs();

        File[] files = Arrays.stream(base.listFiles()).filter(f -> {

            if(f.getName().contains(".yml")) return true;

            return false;

        }).toArray(File[]::new);

        for(File file : files){

            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

            ConfigurationSection sectionCustomBlocks = config.getConfigurationSection("items");

            if(sectionCustomBlocks != null){
                for(String key : sectionCustomBlocks.getKeys(false)){

                    ConfigurationSection customBlockSection = sectionCustomBlocks.getConfigurationSection((String)key);
                    String material = customBlockSection.getString("item", "mc:stone");

                    String displayName = customBlockSection.getString("displayName");

                    List<String> lore = customBlockSection.getStringList("lore");
                    boolean dropBlock = customBlockSection.getBoolean("properties.drop_block",true);
                    boolean stackable = customBlockSection.getBoolean("properties.stackable",true);
                    String skullTexture = customBlockSection.getString("properties.skull_texture");
                    CustomItemProperties customItemProperties = new CustomItemProperties(dropBlock, stackable, skullTexture);
                    CustomItem customItem = new CustomItem((String)key,material,displayName,lore, customItemProperties);

                    customItems.put(customItem.getId(), customItem);

                }
            }

        }

        AdventureUtils.sendMessagePluginConsole(core, "<aqua> CustomItems loaded: <yellow>" + customItems.size());

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack itemInHand = event.getItemInHand();
        ItemMeta itemMeta = itemInHand.getItemMeta();
        if(itemInHand == null || itemMeta == null) return;
        PersistentDataContainer itemDataContainer = itemMeta.getPersistentDataContainer();

        if (itemDataContainer.has(new NamespacedKey(StorageMechanic.getInstance(), "storagemechanicb"), PersistentDataType.STRING)) {
            String customBlockId = itemDataContainer.get(new NamespacedKey(StorageMechanic.getInstance(), "storagemechanicb"), PersistentDataType.STRING);

            Block placedBlock = event.getBlockPlaced();
            PersistentDataContainer blockDataContainer = placedBlock.getChunk().getPersistentDataContainer();

            Location blockLocation = placedBlock.getLocation();
            int x = blockLocation.getBlockX();
            int y = blockLocation.getBlockY();
            int z = blockLocation.getBlockZ();

            blockDataContainer.set(new NamespacedKey(StorageMechanic.getInstance(), "storagemechanicb" + "x" + x + "x" + y + "x" + z), PersistentDataType.STRING, customBlockId);

            CustomBlockPlaceEvent CustomBlockPlaceEvent = new CustomBlockPlaceEvent(event, getCustomItemById(customBlockId));
            Bukkit.getPluginManager().callEvent(CustomBlockPlaceEvent);

            if(CustomBlockPlaceEvent.isCancelled()){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Block brokenBlock = event.getBlock();
        Location blockLocation = brokenBlock.getLocation();
        int x = blockLocation.getBlockX();
        int y = blockLocation.getBlockY();
        int z = blockLocation.getBlockZ();

        PersistentDataContainer chunkDataContainer = brokenBlock.getChunk().getPersistentDataContainer();

        NamespacedKey blockKey = new NamespacedKey(StorageMechanic.getInstance(), "storagemechanicb" + "x" + x + "x" + y + "x" + z);
        if (chunkDataContainer.has(blockKey, PersistentDataType.STRING)) {
            ItemStack itemInMainHand = event.getPlayer().getInventory().getItemInMainHand();
            String customBlockId = chunkDataContainer.get(blockKey, PersistentDataType.STRING);
            CustomItem customItem = getCustomItemById(customBlockId);
            if(customItem == null){
                core.getManagers().getTrashSystemManager().checkTrashOnChunk(chunkDataContainer);
                return;
            }
            CustomBlockDestroyEvent customBlockDestroyEvent = new CustomBlockDestroyEvent(event, getCustomItemById(customBlockId));
            Bukkit.getPluginManager().callEvent(customBlockDestroyEvent);
            if(customBlockDestroyEvent.isCancelled()){
                event.setCancelled(true);
                return;
            }

            chunkDataContainer.remove(blockKey);
            event.setDropItems(false);

            if(itemInMainHand == null) return;
            if(customItem.getCustomBlockProperties().isDropBlock()){
                blockLocation.getWorld().dropItem(blockLocation, customItem.getItemStack());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            Location blockLocation = clickedBlock.getLocation();
            int x = blockLocation.getBlockX();
            int y = blockLocation.getBlockY();
            int z = blockLocation.getBlockZ();

            PersistentDataContainer chunkDataContainer = clickedBlock.getChunk().getPersistentDataContainer();

            NamespacedKey blockKey = new NamespacedKey(StorageMechanic.getInstance(), "storagemechanicb" + "x" + x + "x" + y + "x" + z);
            if (chunkDataContainer.has(blockKey, PersistentDataType.STRING)) {
                String customBlockId = chunkDataContainer.get(blockKey, PersistentDataType.STRING);
                event.setCancelled(true);
                CustomBlockInteractEvent customBlockInteractEvent = new CustomBlockInteractEvent(event, getCustomItemById(customBlockId));
                Bukkit.getPluginManager().callEvent(customBlockInteractEvent);
                event.setCancelled(customBlockInteractEvent.isCancelled());
            }
        }
    }

    @EventHandler
    public void onPlayerInteractCreative(InventoryCreativeEvent event){
        Block block = event.getWhoClicked().getTargetBlockExact(6, FluidCollisionMode.NEVER);
        if(block != null && isCustomItem(block)){
            PlayerInventory inventory = event.getWhoClicked().getInventory();
            CustomItem customItem = getCustomItemById(getCustomItemIdFromBlock(block));
            if( !customItem.getItemStack().getType().equals( event.getCursor().getType() ) ) return;
            for( int i = 0 ; i < 9 ; i ++ ){
                if(inventory.getItem(i) != null && inventory.getItem(i).isSimilar(customItem.getItemStack())){
                    event.setCancelled(true);
                    event.getWhoClicked().getInventory().setHeldItemSlot(i);
                    return;
                }
            }
            event.setCursor(customItem.getItemStack());
        }
    }


    public CustomItem getCustomItemById(String id) {
        return customItems.getOrDefault(id,null);
    }

    public boolean customItemExists(String id) {
        return customItems.containsKey(id);
    }

    public List<CustomItem> getAllCustomItems() {
        return new ArrayList<>(customItems.values());
    }

    public boolean giveCustomItemToPlayer(Player player, String customBlockId, int amount) {
        CustomItem customItem = getCustomItemById(customBlockId);
        if (customItem != null) {
            ItemStack itemStack = customItem.getItemStack().clone();
            itemStack.setAmount(amount);
            StorageUtils.addItemToInventoryOrDrop(player, itemStack);
            return true;
        } else {
            return false;
        }
    }

    public String getCustomItemIdFromBlock(Block block) {
        Location blockLocation = block.getLocation();
        int x = blockLocation.getBlockX();
        int y = blockLocation.getBlockY();
        int z = blockLocation.getBlockZ();

        PersistentDataContainer chunkDataContainer = block.getChunk().getPersistentDataContainer();
        NamespacedKey blockKey = new NamespacedKey(core, "storagemechanicb" + "x" + x + "x" + y + "x" + z);

        if (chunkDataContainer.has(blockKey, PersistentDataType.STRING)) {
            return chunkDataContainer.get(blockKey, PersistentDataType.STRING);
        }
        return null;
    }

    public boolean isCustomItem(Block block) {
        return block.getChunk().getPersistentDataContainer().has(new NamespacedKey(core,"storagemechanicb" + "x" + block.getX() + "x" + block.getY() + "x" + block.getZ()),PersistentDataType.STRING);
    }

    public String getCustomItemIdFromItemStack(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer itemDataContainer = itemMeta.getPersistentDataContainer();

        if (itemDataContainer.has(new NamespacedKey(StorageMechanic.getInstance(), "storagemechanicb"), PersistentDataType.STRING)) {
            return itemDataContainer.get(new NamespacedKey(StorageMechanic.getInstance(), "storagemechanicb"), PersistentDataType.STRING);
        }
        return null;
    }

    public boolean isCustomItemItemStack(ItemStack itemStack) {
        return getCustomItemIdFromItemStack(itemStack) != null;
    }

    //EVENTS BLOCK

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        if (isCustomItem(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if (isCustomItem(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        if (isCustomItem(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        if (isCustomItem(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (isCustomItem(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        /*for(int i=0;i<event.blockList().size();i++){
            if(isCustomBlock(event.blockList().get(i))) event.setCancelled(true););
        }*/
        event.blockList().removeIf(block -> isCustomItem(block));
    }

    @EventHandler
    public void onLeafDecay(LeavesDecayEvent event) {
        if (isCustomItem(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        for(int i=0;i<event.getBlocks().size();i++){
            if(isCustomItem(event.getBlocks().get(i))) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        for(int i=0;i<event.getBlocks().size();i++){
            if(isCustomItem(event.getBlocks().get(i))) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (isCustomItem(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (isCustomItem(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event) {
        if (isCustomItem(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFluidLevelChange(FluidLevelChangeEvent event) {
        if (isCustomItem(event.getBlock())) {
            event.setCancelled(true);
        }
    }

}
