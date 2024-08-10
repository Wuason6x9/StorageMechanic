package dev.wuason.storagemechanic.storages.types.block.config;

public class BlockHopperMechanicProperties extends BlockMechanicProperties {
    private long tick = 8;
    private int transferAmount = 1;

    public BlockHopperMechanicProperties(long tick, int transferAmount) {
        this.tick = tick;
        this.transferAmount = transferAmount;
    }

    public long getTick() {
        return tick;
    }

    public int getTransferAmount() {
        return transferAmount;
    }
}
