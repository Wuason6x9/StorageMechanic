package dev.wuason.storagemechanic.storages.types.block.mechanics.integrated.hopper;

import dev.wuason.storagemechanic.storages.types.block.BlockStorage;
import dev.wuason.storagemechanic.storages.types.block.config.BlockStorageConfig;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class HopperInfo {
    private Location hopperLoc;
    private Block blockStorageBlock;
    private BlockStorage blockStorage;
    private Player player;
    private TransferType transferType;
    private BlockFace blockFaceOrigin;
    private int transferAmount;
    private long tick;

    public HopperInfo(Location hopperLoc, Block blockStorageBlock, BlockStorage blockStorage, Player player, TransferType transferType, int transferAmount, long tick) {
        this.hopperLoc = hopperLoc;
        this.blockStorageBlock = blockStorageBlock;
        this.blockStorage = blockStorage;
        this.player = player;
        this.transferType = transferType;
        this.transferAmount = transferAmount;
        this.tick = tick;
    }

    public Location getHopperLoc() {
        return hopperLoc;
    }

    public Block getBlockStorageBlock() {
        return blockStorageBlock;
    }

    public BlockStorage getBlockStorage() {
        return blockStorage;
    }

    public Player getPlayer() {
        return player;
    }

    public TransferType getTransferType() {
        return transferType;
    }

    public int getTransferAmount() {
        return transferAmount;
    }

    public long getTick() {
        return tick;
    }
}
