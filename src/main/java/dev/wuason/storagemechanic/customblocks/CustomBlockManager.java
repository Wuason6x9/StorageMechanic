package dev.wuason.storagemechanic.customblocks;

import dev.wuason.mechanics.Mechanics;
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
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.NamespacedKey;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.RayTraceResult;


import java.io.File;
import java.util.*;

public class CustomBlockManager implements Listener {

    private StorageMechanic core;
    private HashMap<String,CustomBlock> customBlocks = new HashMap<>();

    public CustomBlockManager(StorageMechanic core) {
        this.core = core;
    }

    public void loadCustomBlocks(){

        customBlocks = new HashMap<>();

        File base = new File(Mechanics.getInstance().getManager().getMechanicsManager().getMechanic(core).getDirConfig().getPath() + "/CustomBlocks/");
        base.mkdirs();

        File[] files = Arrays.stream(base.listFiles()).filter(f -> {

            if(f.getName().contains(".yml")) return true;

            return false;

        }).toArray(File[]::new);

        for(File file : files){

            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

            ConfigurationSection sectionCustomBlocks = config.getConfigurationSection("blocks");

            if(sectionCustomBlocks != null){
                for(Object key : sectionCustomBlocks.getKeys(false).toArray()){

                    ConfigurationSection customBlockSection = sectionCustomBlocks.getConfigurationSection((String)key);
                    String material = customBlockSection.getString("material", "mc:stone");

                    String displayName = customBlockSection.getString("displayName");

                    List<String> lore = customBlockSection.getStringList("lore");
                    boolean dropBlock = customBlockSection.getBoolean("properties.drop_block",true);
                    CustomBlockProperties customBlockProperties = new CustomBlockProperties(dropBlock);
                    CustomBlock customBlock = new CustomBlock((String)key,material,displayName,lore,customBlockProperties);

                    customBlocks.put(customBlock.getId(), customBlock);

                }
            }

        }

        AdventureUtils.sendMessagePluginConsole(core, "<aqua> CustomBlocks loaded: <yellow>" + customBlocks.size());

    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack itemInHand = event.getItemInHand();
        ItemMeta itemMeta = itemInHand.getItemMeta();
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

            CustomBlockPlaceEvent CustomBlockPlaceEvent = new CustomBlockPlaceEvent(event,getCustomBlockById(customBlockId));
            Bukkit.getPluginManager().callEvent(CustomBlockPlaceEvent);

            if(CustomBlockPlaceEvent.isCancelled()){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
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
            CustomBlock customBlock = getCustomBlockById(customBlockId);
            if(customBlock == null){
                core.getManagers().getTrashSystemManager().checkTrashOnChunk(chunkDataContainer);
                return;
            }
            CustomBlockDestroyEvent customBlockDestroyEvent = new CustomBlockDestroyEvent(event,getCustomBlockById(customBlockId));
            Bukkit.getPluginManager().callEvent(customBlockDestroyEvent);
            if(customBlockDestroyEvent.isCancelled()){
                event.setCancelled(true);
                return;
            }

            chunkDataContainer.remove(blockKey);
            event.setDropItems(false);

            if(itemInMainHand == null) return;
            if(customBlock.getCustomBlockProperties().isDropBlock()){
                blockLocation.getWorld().dropItem(blockLocation,customBlock.getItemStack());
            }
        }
    }

    @EventHandler
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
                CustomBlockInteractEvent customBlockInteractEvent = new CustomBlockInteractEvent(event,getCustomBlockById(customBlockId));
                Bukkit.getPluginManager().callEvent(customBlockInteractEvent);
                event.setCancelled(customBlockInteractEvent.isCancelled());
            }
        }
    }

    @EventHandler
    public void onPlayerInteractCreative(InventoryCreativeEvent event){
        Block block = event.getWhoClicked().getTargetBlockExact(6, FluidCollisionMode.NEVER);
        if(block != null && isCustomBlock(block)){
            PlayerInventory inventory = event.getWhoClicked().getInventory();
            CustomBlock customBlock = getCustomBlockById(getCustomBlockIdFromBlock(block));
            if( !customBlock.getItemStack().getType().equals( event.getCursor().getType() ) ) return;
            for( int i = 0 ; i < 9 ; i ++ ){
                if(inventory.getItem(i) != null && inventory.getItem(i).isSimilar(customBlock.getItemStack())){
                    event.setCancelled(true);
                    event.getWhoClicked().getInventory().setHeldItemSlot(i);
                    return;
                }
            }
            event.setCursor(customBlock.getItemStack());
        }
    }


    public CustomBlock getCustomBlockById(String id) {
        return customBlocks.getOrDefault(id,null);
    }

    public boolean customBlockExists(String id) {
        return customBlocks.containsKey(id);
    }

    public List<CustomBlock> getAllCustomBlocks() {
        return new ArrayList<>(customBlocks.values());
    }

    public boolean giveCustomBlockToPlayer(Player player, String customBlockId, int amount) {
        CustomBlock customBlock = getCustomBlockById(customBlockId);
        if (customBlock != null) {
            ItemStack itemStack = customBlock.getItemStack().clone();
            itemStack.setAmount(amount);
            StorageUtils.addItemToInventoryOrDrop(player, itemStack);
            return true;
        } else {
            return false;
        }
    }

    public String getCustomBlockIdFromBlock(Block block) {
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

    public boolean isCustomBlock(Block block) {
        return block.getChunk().getPersistentDataContainer().has(new NamespacedKey(core,"storagemechanicb" + "x" + block.getX() + "x" + block.getY() + "x" + block.getZ()),PersistentDataType.STRING);
    }

    public String getCustomBlockIdFromItemStack(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer itemDataContainer = itemMeta.getPersistentDataContainer();

        if (itemDataContainer.has(new NamespacedKey(StorageMechanic.getInstance(), "storagemechanicb"), PersistentDataType.STRING)) {
            return itemDataContainer.get(new NamespacedKey(StorageMechanic.getInstance(), "storagemechanicb"), PersistentDataType.STRING);
        }
        return null;
    }

    public boolean isCustomBlockItemStack(ItemStack itemStack) {
        return getCustomBlockIdFromItemStack(itemStack) != null;
    }

    //EVENTS BLOCK

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        if (isCustomBlock(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if (isCustomBlock(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        if (isCustomBlock(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        if (isCustomBlock(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (isCustomBlock(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        /*for(int i=0;i<event.blockList().size();i++){
            if(isCustomBlock(event.blockList().get(i))) event.setCancelled(true););
        }*/
        event.blockList().removeIf(block -> isCustomBlock(block));
    }

    @EventHandler
    public void onLeafDecay(LeavesDecayEvent event) {
        if (isCustomBlock(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        for(int i=0;i<event.getBlocks().size();i++){
            if(isCustomBlock(event.getBlocks().get(i))) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        for(int i=0;i<event.getBlocks().size();i++){
            if(isCustomBlock(event.getBlocks().get(i))) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (isCustomBlock(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (isCustomBlock(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event) {
        if (isCustomBlock(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFluidLevelChange(FluidLevelChangeEvent event) {
        if (isCustomBlock(event.getBlock())) {
            event.setCancelled(true);
        }
    }

}
