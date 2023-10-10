package dev.wuason.storagemechanic.storages.types.block.mechanics.integrated.hopper;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageItemDataInfo;
import dev.wuason.storagemechanic.storages.types.block.BlockStorage;
import dev.wuason.storagemechanic.storages.types.block.config.BlockHopperMechanicProperties;
import dev.wuason.storagemechanic.storages.types.block.config.BlockStorageMechanicConfig;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

public class HopperActive {
    private BlockStorage blockStorage;
    private TransferType transferType;
    private BlockFace blockFaceOrigin;
    private BukkitTask bukkitTask;
    private Block destination;
    private Block origin;
    private HopperBlockMechanic hopperBlockMechanic;
    private UUID id;
    private int transferAmount = 1;
    private long tick = 8L;

    public HopperActive(BlockStorage blockStorage, TransferType transferType, BlockFace blockFaceOrigin, Block destination, Block origin, HopperBlockMechanic hopperBlockMechanic, int transferAmount, long tick) {
        this.blockStorage = blockStorage;
        this.transferType = transferType;
        this.blockFaceOrigin = blockFaceOrigin;
        this.destination = destination;
        this.origin = origin;
        this.hopperBlockMechanic = hopperBlockMechanic;
        id = UUID.randomUUID();
        this.transferAmount = transferAmount;
        this.tick = tick;

    }

    public void start(){

        if(transferType.equals(TransferType.STORAGE_TO_HOPPER_DOWN)){

            Hopper hopper = (Hopper) destination.getState();

            bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(StorageMechanic.getInstance(),() ->{

                boolean r = transferOriginToDestination(hopper);

                if(!r) {
                    System.out.println("Finished!");
                    finish();
                }

            },0L,tick);
        }
    }

    public boolean transferOriginToDestination(Hopper hopper){
        if(transferType.equals(TransferType.STORAGE_TO_HOPPER_DOWN)){
            if(hopper.isLocked()) return false;  // Si está bloqueado, no se hace nada.

            Storage storage = blockStorage.getStorages().entrySet().iterator().next().getValue();

            // Si el inventario del hopper está lleno
            if(hopper.getInventory().firstEmpty() == -1){
                for(int i = 0; i < hopper.getInventory().getSize(); i++){
                    ItemStack hopperItemStack = hopper.getInventory().getItem(i);

                    int maxStackHopperItem = hopperItemStack.getMaxStackSize();
                    int amountHopperItem = hopperItemStack.getAmount();

                    // si el item del hopper esta al maximo
                    if(amountHopperItem >= maxStackHopperItem) continue;

                    StorageItemDataInfo similar = storage.getFirstItemStackSimilar(hopperItemStack);
                    System.out.println(similar);
                    if (similar == null) continue;
                    int amountSimilar = similar.getItemStack().getAmount();


                    int a = maxStackHopperItem - amountHopperItem; // 64 | 53  = 11

                    if( a > transferAmount ){ // 11 > 5 yes
                        System.out.println("> transfer amount");
                        int b = a - transferAmount; // sobras de la cantidad que puede llegar a transferir 5 = 6
                        int c = amountSimilar - transferAmount;
                        if(c>=0){
                            hopperItemStack.setAmount(amountHopperItem + transferAmount);
                            similar.getItemStack().setAmount(c);
                            continue;
                        }


                    }



                    /*int rest = amountSimilar - transferAmount;
                    if(rest == 0) similar.remove();
                    else similar.getItemStack().setAmount(rest);
                    hopperItemStack.setAmount(amountHopperItem + transferAmount);
                    return true;*/


                }
            }
            else {
                StorageItemDataInfo storageItem = storage.getFirstItemStack();
                if(storageItem != null){
                    ItemStack storageStack = storageItem.getItemStack();
                    int transferableAmount = Math.min(transferAmount, storageStack.getAmount());

                    // Intenta agregar al hopper
                    HashMap<Integer, ItemStack> notFit = hopper.getInventory().addItem(new ItemStack(storageStack.getType(), transferableAmount));

                    // Si todos los ítems fueron añadidos exitosamente al hopper
                    if (notFit.isEmpty()) {
                        int newStorageAmount = storageStack.getAmount() - transferableAmount;
                        if(newStorageAmount == 0) storageItem.remove();
                        else storageStack.setAmount(newStorageAmount);
                        return true;  // Retorna true después de transferir el primer ItemStack
                    }
                }
            }
            return false;
        }
        return false;
    }

    public boolean isFinished(){
        return bukkitTask.isCancelled();
    }

    public void finish(){
        bukkitTask.cancel();
        hopperBlockMechanic.finish(id);
    }

    public BlockStorage getBlockStorage() {
        return blockStorage;
    }

    public TransferType getTransferType() {
        return transferType;
    }

    public BlockFace getBlockFaceOrigin() {
        return blockFaceOrigin;
    }

    public BukkitTask getBukkitTask() {
        return bukkitTask;
    }

    public Block getDestination() {
        return destination;
    }

    public Block getOrigin() {
        return origin;
    }

    public HopperBlockMechanic getHopperBlockMechanic() {
        return hopperBlockMechanic;
    }

    public UUID getId() {
        return id;
    }

    public BlockStorageMechanicConfig getBlockMechanicConfig(){
        return blockStorage.getBlockStorageConfig().getMechanicConfigHashMap().get(hopperBlockMechanic.getId());
    }
}
