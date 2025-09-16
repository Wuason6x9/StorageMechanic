package dev.wuason.storagemechanic.storages.types.block.mechanics.integrated.hopper;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.storages.types.block.BlockStorage;
import dev.wuason.storagemechanic.storages.types.block.BlockStorageManager;
import dev.wuason.storagemechanic.storages.types.block.config.BlockHopperMechanicProperties;
import dev.wuason.storagemechanic.storages.types.block.config.BlockStorageConfig;
import dev.wuason.storagemechanic.storages.types.block.config.BlockStorageMechanicConfig;
import dev.wuason.storagemechanic.storages.types.block.mechanics.BlockMechanic;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class HopperBlockMechanic extends BlockMechanic implements Listener {
    public static final String HOPPER_MECHANIC_KEY = "HOPPER_MECHANIC";
    private StorageMechanic core;
    private HashMap<UUID, HopperActive> hopperActiveHashMap = new HashMap<>();
    private HashMap<String, UUID> hopperUUIDCurrentActiveHashMap = new HashMap<>();
    private BlockStorageManager blockStorageManager;
    private static BlockFace[] FACES = {BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST};

    public HopperBlockMechanic(StorageMechanic core) {
        super(HOPPER_MECHANIC_KEY);
        this.core = core;
    }

    public void finish(UUID id) {

        hopperUUIDCurrentActiveHashMap.remove(hopperActiveHashMap.get(id).getDataLine());
        hopperActiveHashMap.remove(id);

    }

    public void checkBlockHopperAndTransfer(Block block) {
        Block blockUp = getUpBlock(block);
        Block blockFacing = block.getRelative(((Hopper) block.getBlockData()).getFacing());
        if (blockUp != null && !blockUp.getType().equals(Material.AIR) && getBlockStorageManager().isBlockStorageByBlock(blockUp)) {
            BlockStorage blockStorage = getBlockStorageManager().getBlockStorageByBlock(blockUp);
            startTransfer(TransferType.STORAGE_TO_HOPPER, blockStorage, block, blockUp, BlockFace.UP);
        }
        if (blockFacing != null && !blockFacing.getType().equals(Material.AIR) && getBlockStorageManager().isBlockStorageByBlock(blockFacing)) {
            BlockStorage blockStorage = getBlockStorageManager().getBlockStorageByBlock(blockFacing);
            startTransfer(TransferType.HOPPER_TO_STORAGE, blockStorage, block, blockFacing, BlockFace.UP);
        }
    }

    public void checkBlockStorageAndTransfer(Block block, String[] data) {// 0:BlockStorage 1:BlockStorageConfigId 2:OwnerUUID
        String blockStorageId = data[0];
        String blockStorageConfigId = data[1];
        core.getManagers().getBlockStorageConfigManager().findBlockStorageConfigById(blockStorageConfigId).ifPresent(config -> { //LAG MODIFICARLO
            BlockStorageMechanicConfig blockStorageMechanicConfig = config.getMechanicConfigHashMap().get(HOPPER_MECHANIC_KEY);
            if (blockStorageMechanicConfig == null || !blockStorageMechanicConfig.isEnabled()) return;
            Block blockUp = getUpBlock(block);
            Block blockDown = getDownBlock(block);
            if (blockUp != null && blockUp.getType().equals(Material.HOPPER) && ((Hopper) blockUp.getBlockData()).getFacing().equals(BlockFace.DOWN)) {
                startTransfer(TransferType.HOPPER_TO_STORAGE, blockStorageId, blockUp, block, BlockFace.UP, blockStorageMechanicConfig);
            }
            if (blockDown != null && blockDown.getType().equals(Material.HOPPER)) {
                startTransfer(TransferType.STORAGE_TO_HOPPER, blockStorageId, blockDown, block, BlockFace.DOWN, blockStorageMechanicConfig);
            }
            for (BlockFace face : FACES) {
                Block b = block.getRelative(face);
                if (b != null && b.getType().equals(Material.HOPPER) && ((Hopper) b.getBlockData()).getFacing().equals(face.getOppositeFace())) {
                    startTransfer(TransferType.HOPPER_TO_STORAGE, blockStorageId, b, block, face, blockStorageMechanicConfig);
                }
            }
        });
    }

    public void checkBlockStorageAndTransfer(String[] data) {// 0:BlockStorage 1:BlockStorageConfigId 2:OwnerUUID
        String blockStorageId = data[0];
        String blockStorageConfigId = data[1];
        core.getManagers().getBlockStorageConfigManager().findBlockStorageConfigById(blockStorageConfigId).ifPresent(config -> { //LAG MODIFICARLO
            BlockStorageMechanicConfig blockStorageMechanicConfig = config.getMechanicConfigHashMap().get(HOPPER_MECHANIC_KEY);
            if (blockStorageMechanicConfig == null || !blockStorageMechanicConfig.isEnabled()) return;
            BlockStorage blockStorage = getBlockStorageManager().getBlockStorage(blockStorageId);
            Block block = blockStorage.getLocation(0).getBlock();
            Block blockUp = getUpBlock(block);
            Block blockDown = getDownBlock(block);
            if (blockUp != null && blockUp.getType().equals(Material.HOPPER) && ((Hopper) blockUp.getBlockData()).getFacing().equals(BlockFace.DOWN)) {
                startTransfer(TransferType.HOPPER_TO_STORAGE, blockStorage, blockUp, block, BlockFace.UP, blockStorageMechanicConfig);
            }
            if (blockDown != null && blockDown.getType().equals(Material.HOPPER)) {
                startTransfer(TransferType.STORAGE_TO_HOPPER, blockStorage, blockDown, block, BlockFace.DOWN, blockStorageMechanicConfig);
            }
            for (BlockFace face : FACES) {
                Block b = block.getRelative(face);
                if (b != null && b.getType().equals(Material.HOPPER) && ((Hopper) b.getBlockData()).getFacing().equals(face.getOppositeFace())) {
                    startTransfer(TransferType.HOPPER_TO_STORAGE, blockStorage, b, block, face, blockStorageMechanicConfig);
                }
            }
        });
    }

    public void checkBlockStorageAndTransfer(Block block, BlockStorage blockStorage, BlockStorageConfig blockStorageConfig) {// 0:BlockStorage 1:BlockStorageConfigId 2:OwnerUUID
        BlockStorageMechanicConfig blockStorageMechanicConfig = blockStorageConfig.getMechanicConfigHashMap().get(HOPPER_MECHANIC_KEY);
        if (blockStorageMechanicConfig == null || !blockStorageMechanicConfig.isEnabled()) return;
        Block blockUp = getUpBlock(block);
        Block blockDown = getDownBlock(block);
        if (blockUp != null && blockUp.getType().equals(Material.HOPPER) && ((Hopper) blockUp.getBlockData()).getFacing().equals(BlockFace.DOWN)) {
            startTransfer(TransferType.HOPPER_TO_STORAGE, blockStorage, blockUp, block, BlockFace.UP, blockStorageMechanicConfig);
        }
        if (blockDown != null && blockDown.getType().equals(Material.HOPPER)) {
            startTransfer(TransferType.STORAGE_TO_HOPPER, blockStorage, blockDown, block, BlockFace.DOWN, blockStorageMechanicConfig);
        }
        for (BlockFace face : FACES) {
            Block b = block.getRelative(face);
            if (b != null && b.getType().equals(Material.HOPPER) && ((Hopper) b.getBlockData()).getFacing().equals(face.getOppositeFace())) {
                startTransfer(TransferType.HOPPER_TO_STORAGE, blockStorage, b, block, face, blockStorageMechanicConfig);
            }
        }
    }

    public void startTransfer(TransferType transferType, String blockStorageId, Block hopperBlock, Block storageBlock, BlockFace blockFace, BlockStorageMechanicConfig blockStorageMechanicConfig) {
        if (hopperUUIDCurrentActiveHashMap.containsKey(getDataLine(hopperBlock.getLocation(), storageBlock.getLocation(), transferType, blockStorageId)))
            return;
        BlockStorage blockStorage = getBlockStorageManager().getBlockStorage(blockStorageId);
        HopperActive hopperActive = new HopperActive(blockStorage, transferType, blockFace, hopperBlock, storageBlock, this, ((BlockHopperMechanicProperties) blockStorageMechanicConfig.getBlockMechanicProperties()).getTransferAmount(), ((BlockHopperMechanicProperties) blockStorageMechanicConfig.getBlockMechanicProperties()).getTick());
        hopperActiveHashMap.put(hopperActive.getId(), hopperActive);
        hopperUUIDCurrentActiveHashMap.put(hopperActive.getDataLine(), hopperActive.getId());
        hopperActive.start();
    }

    public void startTransfer(TransferType transferType, BlockStorage blockStorage, Block hopperBlock, Block storageBlock, BlockFace blockFace, BlockStorageMechanicConfig blockStorageMechanicConfig) {
        if (hopperUUIDCurrentActiveHashMap.containsKey(getDataLine(hopperBlock.getLocation(), storageBlock.getLocation(), transferType, blockStorage.getId())))
            return;
        HopperActive hopperActive = new HopperActive(blockStorage, transferType, blockFace, hopperBlock, storageBlock, this, ((BlockHopperMechanicProperties) blockStorageMechanicConfig.getBlockMechanicProperties()).getTransferAmount(), ((BlockHopperMechanicProperties) blockStorageMechanicConfig.getBlockMechanicProperties()).getTick());
        hopperActiveHashMap.put(hopperActive.getId(), hopperActive);
        hopperUUIDCurrentActiveHashMap.put(hopperActive.getDataLine(), hopperActive.getId());
        hopperActive.start();
    }

    public void startTransfer(TransferType transferType, BlockStorage blockStorage, Block hopperBlock, Block storageBlock, BlockFace blockFace) {
        if (hopperUUIDCurrentActiveHashMap.containsKey(getDataLine(hopperBlock.getLocation(), storageBlock.getLocation(), transferType, blockStorage.getId())))
            return;
        BlockStorageMechanicConfig blockStorageMechanicConfig = blockStorage.getBlockStorageConfig().getMechanicConfigHashMap().get(HOPPER_MECHANIC_KEY);
        HopperActive hopperActive = new HopperActive(blockStorage, transferType, blockFace, hopperBlock, storageBlock, this, ((BlockHopperMechanicProperties) blockStorageMechanicConfig.getBlockMechanicProperties()).getTransferAmount(), ((BlockHopperMechanicProperties) blockStorageMechanicConfig.getBlockMechanicProperties()).getTick());
        hopperActiveHashMap.put(hopperActive.getId(), hopperActive);
        hopperUUIDCurrentActiveHashMap.put(hopperActive.getDataLine(), hopperActive.getId());
        hopperActive.start();
    }


    public void onBlockStoragePlace(Block block, Player player, String[] data) {
        checkBlockStorageAndTransfer(block, data);
    }

    public void onBlockStoragePlace(Block block, Player player, BlockStorage blockStorage, BlockStorageConfig blockStorageConfig) {
        checkBlockStorageAndTransfer(block, blockStorage, blockStorageConfig);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        for (NamespacedKey namespacedKey : getBlockStorageManager().getAllBlockStorages(event.getChunk())) {
            BlockStorageConfig blockStorageConfig = getBlockStorageManager().getBlockStorageConfigByNameSpace(namespacedKey, event.getChunk());
            if (blockStorageConfig == null || !blockStorageConfig.getMechanicConfigHashMap().containsKey(HOPPER_MECHANIC_KEY))
                continue;
            BlockStorageMechanicConfig blockStorageMechanicConfig = blockStorageConfig.getMechanicConfigHashMap().get(HOPPER_MECHANIC_KEY);
            if (!blockStorageMechanicConfig.isEnabled()) continue;
            BlockStorage blockStorage = getBlockStorageManager().getBlockStorageByNameSpace(namespacedKey, event.getChunk());
            Block block = getBlockStorageManager().getBlockByNameSpace(namespacedKey, event.getChunk());
            checkBlockStorageAndTransfer(block, blockStorage, blockStorageConfig);
        }
    }

    @EventHandler
    public void onPickUpItemHopper(InventoryPickupItemEvent event) {
        if (event.getInventory() == null || !(event.getInventory().getHolder() instanceof org.bukkit.block.Hopper))
            return;
        checkBlockHopperAndTransfer(((org.bukkit.block.Hopper) event.getInventory().getHolder()).getBlock());
    }

    public void onItemMoveMechanic(Block hopperBlock, Block storageBlock, org.bukkit.block.Hopper hopperState, BlockStorage blockStorage, TransferType transferType) {
        Bukkit.getScheduler().runTask(core, () -> {

            checkBlockStorageAndTransfer(storageBlock, blockStorage, blockStorage.getBlockStorageConfig());

            checkBlockHopperAndTransfer(hopperBlock);
        });
    }

    @EventHandler
    public void onItemMove(InventoryMoveItemEvent inventoryMoveItemEvent) {
        if (inventoryMoveItemEvent.getSource().getHolder() instanceof org.bukkit.block.Hopper) {
            org.bukkit.block.Hopper hopper = (org.bukkit.block.Hopper) inventoryMoveItemEvent.getSource().getHolder();
            if (hopper.getBlock() != null) checkBlockHopperAndTransfer(hopper.getBlock());
        }
        if (inventoryMoveItemEvent.getDestination().getHolder() instanceof org.bukkit.block.Hopper) {
            org.bukkit.block.Hopper hopper = (org.bukkit.block.Hopper) inventoryMoveItemEvent.getDestination().getHolder();
            if (hopper.getBlock() != null) checkBlockHopperAndTransfer(hopper.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onClickHopperInventory(InventoryClickEvent event) {
        if (event.getInventory() == null || !(event.getInventory().getHolder() instanceof org.bukkit.block.Hopper))
            return; //((org.bukkit.block.Hopper)event.getClickedInventory().getHolder())
        checkBlockHopperAndTransfer(((org.bukkit.block.Hopper) event.getInventory().getHolder()).getBlock());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onOpenHopperInventory(InventoryOpenEvent event) {
        if (event.getInventory() == null || !(event.getInventory().getHolder() instanceof org.bukkit.block.Hopper))
            return; //((org.bukkit.block.Hopper)event.getClickedInventory().getHolder())
        checkBlockHopperAndTransfer(((org.bukkit.block.Hopper) event.getInventory().getHolder()).getBlock());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCloseHopperInventory(InventoryCloseEvent event) {
        if (event.getInventory() == null || !(event.getInventory().getHolder() instanceof org.bukkit.block.Hopper))
            return; //((org.bukkit.block.Hopper)event.getClickedInventory().getHolder())
        checkBlockHopperAndTransfer(((org.bukkit.block.Hopper) event.getInventory().getHolder()).getBlock());
    }


    @EventHandler
    public void onBlockPlaceHopper(BlockPlaceEvent event) {
        if (event.getItemInHand() == null || !event.canBuild() || !event.getItemInHand().getType().equals(Material.HOPPER))
            return;

        Block hopperBlock = event.getBlockPlaced();
        processBlockHopperEvent(event, getUpBlock(hopperBlock), TransferType.STORAGE_TO_HOPPER, hopperBlock);
        if ((hopperBlock.getY() - event.getBlockAgainst().getY()) != -1 && (hopperBlock.getY() - event.getBlockAgainst().getY()) != 1) {
            processBlockHopperEvent(event, event.getBlockAgainst(), TransferType.HOPPER_TO_STORAGE, hopperBlock);
        }
        processBlockHopperEvent(event, getDownBlock(hopperBlock), TransferType.HOPPER_TO_STORAGE, hopperBlock);
    }

    private void processBlockHopperEvent(BlockPlaceEvent event, Block targetBlock, TransferType transferType, Block hopperBlock) {
        if (!getBlockStorageManager().isBlockStorageByBlock(targetBlock)) return;
        BlockStorage blockStorage = getBlockStorageManager().getBlockStorageByBlock(targetBlock);
        if (hopperUUIDCurrentActiveHashMap.containsKey(getDataLine(hopperBlock.getLocation(), targetBlock.getLocation(), transferType, blockStorage.getId())))
            return;
        BlockStorageMechanicConfig blockStorageMechanicConfig = blockStorage.getBlockStorageConfig().getMechanicConfigHashMap().get(HOPPER_MECHANIC_KEY);
        if (blockStorageMechanicConfig == null || !blockStorageMechanicConfig.isEnabled()) return;

        BlockHopperMechanicProperties hopperConfigProperties = (BlockHopperMechanicProperties) blockStorageMechanicConfig.getBlockMechanicProperties();
        HopperActive hopperActive = new HopperActive(blockStorage, transferType, event.getPlayer().getFacing(), hopperBlock, targetBlock, this, hopperConfigProperties.getTransferAmount(), hopperConfigProperties.getTick());
        hopperActiveHashMap.put(hopperActive.getId(), hopperActive);
        hopperUUIDCurrentActiveHashMap.put(hopperActive.getDataLine(), hopperActive.getId());
        hopperActive.start();
    }

    public String[] getDataArray(String str) {
        return str.split(":");
    }

    public String getDataLine(Location hopper, Location storage, TransferType transferType, String blockStorageId) {
        return getDataLocationLine(hopper) + ":" + getDataLocationLine(storage) + ":" + transferType + ":" + blockStorageId;
    }

    public String getDataLine(String[] data) {
        return data[0] + ":" + data[1] + ":" + data[2] + ":" + data[3];
    }

    public String getDataLocationLine(Location loc) {
        return loc.getBlockX() + "-" + loc.getBlockY() + "-" + loc.getBlockZ() + "-" + loc.getWorld().getUID();
    }

    public Location getDataLocation(String line) {
        String[] d = line.split("-");
        Location loc = new Location(Bukkit.getWorld(UUID.fromString(d[3])), Integer.parseInt(d[0]), Integer.parseInt(d[1]), Integer.parseInt(d[2]));
        return loc;
    }



    /*public void run(HopperInfo hopperInfo){
        Hopper hopper = (Hopper) hopperBlock.getState();


        if(getBlockStorageManager().isBlockStorageByBlock(blockUp)){
            BlockStorage blockStorage = getBlockStorageManager().getBlockStorageByBlock(blockUp);

            HopperActive hopperActive = new HopperActive(blockStorage,TransferType.STORAGE_TO_HOPPER_DOWN,hopperBlock.getFace(blockUp),blockUp,hopperBlock,this);
            hopperActiveHashMap.put(hopperActive.getId(),hopperActive);
            hopperActive.start();

        }
    }*/

    public BlockStorageManager getBlockStorageManager() {
        if (blockStorageManager == null) blockStorageManager = core.getManagers().getBlockStorageManager();
        return blockStorageManager;
    }

    public Block getUpBlock(Block block) {
        return block.getRelative(BlockFace.UP);
    }

    public Block getDownBlock(Block block) {
        return block.getRelative(BlockFace.DOWN);
    }

    public List<Block> getBlocksSide(Block block) {
        List<Block> list = new ArrayList<>();
        for (BlockFace face : FACES) {
            Block b = block.getRelative(face);
            if (b != null && !b.getType().equals(Material.AIR)) list.add(block);
        }
        return list;
    }

    public StorageMechanic getCore() {
        return core;
    }

    public HashMap<UUID, HopperActive> getHopperActiveHashMap() {
        return hopperActiveHashMap;
    }

    public HashMap<String, UUID> getHopperUUIDCurrentActiveHashMap() {
        return hopperUUIDCurrentActiveHashMap;
    }
}
