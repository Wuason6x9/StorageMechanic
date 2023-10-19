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
import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.HashMap;
import java.util.UUID;

public class HopperBlockMechanic extends BlockMechanic implements Listener {
    public static final String HOPPER_MECHANIC_KEY = "HOPPER_MECHANIC";
    private StorageMechanic core;
    private HashMap<UUID, HopperActive> hopperActiveHashMap = new HashMap<>();
    private BlockStorageManager blockStorageManager;
    public HopperBlockMechanic(StorageMechanic core) {
        super(ID);
        this.core = core;
    }

    public void finish(UUID id){
        hopperActiveHashMap.remove(id);
    }



    @EventHandler
    public void onBlockPlaceHopper(BlockPlaceEvent event){
        if(event.getItemInHand() != null || !event.canBuild() || !event.getItemInHand().getType().equals(Material.HOPPER)) return;

        Block hopperBlock = event.getBlockPlaced();
        processBlockHopperEvent(event, getUpBlock(hopperBlock), TransferType.STORAGE_TO_HOPPER_DOWN, hopperBlock);
        processBlockHopperEvent(event, event.getBlockAgainst(), TransferType.HOPPER_TO_STORAGE_SIDE, hopperBlock);
        processBlockHopperEvent(event, getDownBlock(hopperBlock), TransferType.HOPPER_TO_STORAGE_UP, hopperBlock);
    }

    private void processBlockHopperEvent(BlockPlaceEvent event, Block targetBlock, TransferType transferType, Block hopperBlock) {
        if(!getBlockStorageManager().isBlockStorageByBlock(targetBlock)) return;

        BlockStorage blockStorage = getBlockStorageManager().getBlockStorageByBlock(targetBlock);
        BlockStorageMechanicConfig blockStorageMechanicConfig = blockStorage.getBlockStorageConfig().getMechanicConfigHashMap().get(HOPPER_MECHANIC_KEY);

        if(blockStorageMechanicConfig == null || !blockStorageMechanicConfig.isEnabled()) return;

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
        return block.getLocation().add(0,1,0).getBlock();
    }

    public StorageMechanic getCore() {
        return core;
    }
}
