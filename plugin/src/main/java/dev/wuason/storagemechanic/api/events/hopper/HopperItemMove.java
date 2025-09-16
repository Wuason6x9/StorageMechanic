package dev.wuason.storagemechanic.api.events.hopper;

import dev.wuason.storagemechanic.storages.types.block.BlockStorage;
import dev.wuason.storagemechanic.storages.types.block.mechanics.integrated.hopper.TransferType;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class HopperItemMove extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private TransferType transferType;
    private Block hopperBlock;
    private Block storageBlock;
    private BlockStorage blockStorage;
    private Hopper hopperState;

    public HopperItemMove(TransferType transferType, Block hopperBlock, Block storageBlock, BlockStorage blockStorage, Hopper hopper) {
        super(true);
        this.transferType = transferType;
        this.hopperBlock = hopperBlock;
        this.storageBlock = storageBlock;
        this.blockStorage = blockStorage;
        this.hopperState = hopper;
    }

    public TransferType getTransferType() {
        return transferType;
    }

    public Block getHopperBlock() {
        return hopperBlock;
    }

    public Block getStorageBlock() {
        return storageBlock;
    }

    public BlockStorage getBlockStorage() {
        return blockStorage;
    }

    public Hopper getHopperState() {
        return hopperState;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return getHandlersList();
    }

    public static HandlerList getHandlersList() {
        return HANDLERS;
    }
}
