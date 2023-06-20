package dev.wuason.storagemechanic.storages.types.block.config;

public class BlockStorageProperties {
    private boolean breakable = false;

    public BlockStorageProperties(boolean breakable) {
        this.breakable = breakable;
    }

    public boolean isBreakable() {
        return breakable;
    }
}
