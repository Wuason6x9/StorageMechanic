package dev.wuason.storagemechanic.storages.types.block.mechanics.integrated.hopper;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.storages.types.block.BlockStorage;
import dev.wuason.storagemechanic.storages.types.block.BlockStorageManager;
import dev.wuason.storagemechanic.storages.types.block.config.BlockHopperMechanicProperties;
import dev.wuason.storagemechanic.storages.types.block.config.BlockStorageConfig;
import dev.wuason.storagemechanic.storages.types.block.config.BlockStorageMechanicConfig;
import dev.wuason.storagemechanic.storages.types.block.mechanics.BlockMechanic;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class HopperBlockMechanic extends BlockMechanic implements Listener {
    public static final String HOPPER_MECHANIC_KEY = "HOPPER_MECHANIC";
    private StorageMechanic core;
    private HashMap<UUID, HopperActive> hopperActiveHashMap = new HashMap<>();
    private BlockStorageManager blockStorageManager;
    private static BlockFace[] FACES = {BlockFace.SOUTH,BlockFace.WEST,BlockFace.NORTH,BlockFace.EAST};
    public HopperBlockMechanic(StorageMechanic core) {
        super(HOPPER_MECHANIC_KEY);
        this.core = core;
    }

    public void finish(UUID id){
        hopperActiveHashMap.remove(id);
    }





    public void onBlockStoragePlace(Block block, Player player, String[] data){
        String blockStorageID = data[0];
        String blockStorageConfigID = data[1];
        core.getManagers().getBlockStorageConfigManager().findBlockStorageConfigById(blockStorageConfigID).ifPresent(config -> {
            BlockStorageMechanicConfig blockStorageMechanicConfig = config.getMechanicConfigHashMap().get(HOPPER_MECHANIC_KEY);
            if(blockStorageMechanicConfig == null || !blockStorageMechanicConfig.isEnabled()) return;

            Block blockUp = getUpBlock(block);
            Block blockDown = getDownBlock(block);

            if( blockUp != null && blockUp.getType().equals(Material.HOPPER) && ((Hopper)blockUp.getBlockData()).getFacing().equals(BlockFace.DOWN)){
                BlockStorage blockStorage = getBlockStorageManager().getBlockStorage(blockStorageID);
                HopperActive hopperActive = new HopperActive(blockStorage, TransferType.HOPPER_TO_STORAGE, player.getFacing(), blockUp, block, this, ((BlockHopperMechanicProperties) blockStorageMechanicConfig.getBlockMechanicProperties()).getTransferAmount(), ((BlockHopperMechanicProperties) blockStorageMechanicConfig.getBlockMechanicProperties()).getTick() );
                hopperActiveHashMap.put(hopperActive.getId(), hopperActive);
                hopperActive.start();
            }

            if(blockDown != null && blockDown.getType().equals(Material.HOPPER)){
                BlockStorage blockStorage = getBlockStorageManager().getBlockStorage(blockStorageID);
                HopperActive hopperActive = new HopperActive(blockStorage, TransferType.STORAGE_TO_HOPPER, player.getFacing(), blockDown, block, this, ((BlockHopperMechanicProperties) blockStorageMechanicConfig.getBlockMechanicProperties()).getTransferAmount(), ((BlockHopperMechanicProperties) blockStorageMechanicConfig.getBlockMechanicProperties()).getTick() );
                hopperActiveHashMap.put(hopperActive.getId(), hopperActive);
                hopperActive.start();
            }

            for(BlockFace face : FACES){
                Block b = block.getRelative(face);
                if(b != null && b.getType().equals(Material.HOPPER) && ((Hopper)blockUp.getBlockData()).getFacing().equals( face.getOppositeFace() )){

                    BlockStorage blockStorage = getBlockStorageManager().getBlockStorage(blockStorageID);
                    HopperActive hopperActive = new HopperActive(blockStorage, TransferType.HOPPER_TO_STORAGE, player.getFacing(), b, block, this, ((BlockHopperMechanicProperties) blockStorageMechanicConfig.getBlockMechanicProperties()).getTransferAmount(), ((BlockHopperMechanicProperties) blockStorageMechanicConfig.getBlockMechanicProperties()).getTick() );
                    hopperActiveHashMap.put(hopperActive.getId(), hopperActive);
                    hopperActive.start();

                }
            }
        });

    }

    @EventHandler
    public void onBlockPlaceHopper(BlockPlaceEvent event){
        if(event.getItemInHand() == null || !event.canBuild() || !event.getItemInHand().getType().equals(Material.HOPPER)) return;

        Block hopperBlock = event.getBlockPlaced();
        processBlockHopperEvent(event, getUpBlock(hopperBlock), TransferType.STORAGE_TO_HOPPER, hopperBlock);
        if((hopperBlock.getY() - event.getBlockAgainst().getY()) != -1 && (hopperBlock.getY() - event.getBlockAgainst().getY()) != 1){
            System.out.println("test");
            processBlockHopperEvent(event, event.getBlockAgainst(), TransferType.HOPPER_TO_STORAGE, hopperBlock);
        }
        processBlockHopperEvent(event, getDownBlock(hopperBlock), TransferType.HOPPER_TO_STORAGE, hopperBlock);
    }

    private void processBlockHopperEvent(BlockPlaceEvent event, Block targetBlock, TransferType transferType, Block hopperBlock) {
        System.out.println("test1");
        if(!getBlockStorageManager().isBlockStorageByBlock(targetBlock)) return;
        System.out.println("test2");

        BlockStorage blockStorage = getBlockStorageManager().getBlockStorageByBlock(targetBlock);
        System.out.println(blockStorage.getId());
        BlockStorageMechanicConfig blockStorageMechanicConfig = blockStorage.getBlockStorageConfig().getMechanicConfigHashMap().get(HOPPER_MECHANIC_KEY);
        System.out.println(blockStorageMechanicConfig);
        System.out.println(blockStorageMechanicConfig.isEnabled());
        if(blockStorageMechanicConfig == null || !blockStorageMechanicConfig.isEnabled()) return;
        System.out.println(blockStorage.getId());

        BlockHopperMechanicProperties hopperConfigProperties = (BlockHopperMechanicProperties) blockStorageMechanicConfig.getBlockMechanicProperties();
        HopperActive hopperActive = new HopperActive(blockStorage, transferType, event.getPlayer().getFacing(), hopperBlock, targetBlock, this, hopperConfigProperties.getTransferAmount(), hopperConfigProperties.getTick());
        hopperActiveHashMap.put(hopperActive.getId(), hopperActive);
        hopperActive.start();
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

    public BlockStorageManager getBlockStorageManager(){
        if(blockStorageManager == null) blockStorageManager = core.getManagers().getBlockStorageManager();
        return blockStorageManager;
    }

    public Block getUpBlock(Block block){
        return block.getRelative(BlockFace.UP);
    }
    public Block getDownBlock(Block block){
        return block.getRelative(BlockFace.DOWN);
    }

    public List<Block> getBlocksSide(Block block){
        List<Block> list = new ArrayList<>();
        for(BlockFace face : FACES){
            Block b = block.getRelative(face);
            if(b != null && !b.getType().equals(Material.AIR)) list.add(block);
        }
        return list;
    }

    public StorageMechanic getCore() {
        return core;
    }
}
