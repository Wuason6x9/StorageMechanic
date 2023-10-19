package dev.wuason.storagemechanic.storages.types.block.mechanics.integrated.hopper;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageItemDataInfo;
import dev.wuason.storagemechanic.storages.types.block.BlockStorage;
import dev.wuason.storagemechanic.storages.types.block.config.BlockHopperMechanicProperties;
import dev.wuason.storagemechanic.storages.types.block.config.BlockStorageMechanicConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Hopper;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
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

        if(transferType.equals(TransferType.HOPPER_TO_STORAGE_SIDE) || transferType.equals(TransferType.HOPPER_TO_STORAGE_UP)){

            Hopper hopper = (Hopper) destination.getState();
            BlockData blockData0 = destination.getBlockData().clone();
            BlockData blockData1 = origin.getBlockData().clone();

            bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(StorageMechanic.getInstance(),() ->{

                if(!destination.getBlockData().equals(blockData0) || !origin.getBlockData().equals(blockData1)){
                    System.out.println("Finished! forced!");
                    finish();
                    return;
                }

                boolean r = transferOriginToDestination(hopper);

                if(!r) {
                    System.out.println("Finished!");
                    finish();
                }

            },0L,tick);
        }

        if(transferType.equals(TransferType.STORAGE_TO_HOPPER_DOWN)){

            Hopper hopper = (Hopper) destination.getState();
            BlockData blockData0 = destination.getBlockData().clone();
            BlockData blockData1 = origin.getBlockData().clone();

            bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(StorageMechanic.getInstance(),() ->{

                if(!destination.getBlockData().equals(blockData0) || !origin.getBlockData().equals(blockData1)){
                    System.out.println("Finished! forced!");
                    finish();
                    return;
                }

                boolean r = transferOriginToDestination(hopper);

                if(!r) {
                    System.out.println("Finished!");
                    finish();
                }

            },0L,tick);
        }
    }

    public boolean transferOriginToDestination(Hopper hopper){
        if(transferType.equals(TransferType.HOPPER_TO_STORAGE_SIDE) || transferType.equals(TransferType.HOPPER_TO_STORAGE_UP)){
            if(hopper.isLocked()) return false;

            Storage storage = blockStorage.getStorages().entrySet().iterator().next().getValue();

            for(int i=0;i<hopper.getInventory().getSize();i++){

                ItemStack itemHopper = hopper.getInventory().getItem(i);

                if(itemHopper == null || itemHopper.getType().equals(Material.AIR)) continue;

                int itemHopperAmount = itemHopper.getAmount();

                int amountToTransfer = Math.min(transferAmount, itemHopperAmount);

                ItemStack itemTransfer = itemHopper.clone();
                itemTransfer.setAmount(amountToTransfer);
                itemHopper.setAmount(itemHopperAmount - transferAmount);
                List<ItemStack> list = storage.addItemStackToAllPagesWithRestrictions(itemTransfer);

                if(list.size()==0) return true;

                ItemStack itemStackReturned = list.get(0);

                if(itemStackReturned.getAmount() == amountToTransfer) {
                    itemHopper.setAmount(itemHopperAmount);
                    continue;
                }

                itemHopper.setAmount(itemHopper.getAmount() + itemStackReturned.getAmount());

                return true;

            }

            return false;

        }

        if(transferType.equals(TransferType.STORAGE_TO_HOPPER_DOWN)){
            if(hopper.isLocked()) return false;  // Si está bloqueado, no se hace nada.

            Storage storage = blockStorage.getStorages().entrySet().iterator().next().getValue();

            // Si el inventario del hopper está lleno
            if(hopper.getInventory().firstEmpty() == -1) {
                for(int i = 0; i < hopper.getInventory().getSize(); i++) {
                    ItemStack hopperItemStack = hopper.getInventory().getItem(i);
                    if(hopperItemStack == null) continue;  // Agregado para evitar Null

                    int hopperItemMaxStack = hopperItemStack.getMaxStackSize();
                    int hopperItemAmount = hopperItemStack.getAmount();
                    if(hopperItemAmount >= hopperItemMaxStack) continue;  // Slot está completamente lleno

                    StorageItemDataInfo similar = storage.getFirstItemStackSimilar(hopperItemStack);  // Obtener item similar
                    if(similar == null) continue;  // No hay item similar

                    int amountSimilar = similar.getItemStack().getAmount();
                    int spaceAvailableInHopper = hopperItemMaxStack - hopperItemAmount;

                    // Calcula la cantidad real a transferir
                    int actualTransferAmount = Math.min(Math.min(transferAmount, spaceAvailableInHopper), amountSimilar);

                    if(actualTransferAmount > 0) {
                        hopperItemStack.setAmount(hopperItemAmount + actualTransferAmount);
                        similar.getItemStack().setAmount(amountSimilar - actualTransferAmount);

                        if(similar.getItemStack().getAmount() == 0) {
                            similar.remove();
                        }

                        return true;  // Se ha transferido un item, devolver true
                    }
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
