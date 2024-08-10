package dev.wuason.storagemechanic.storages.types.block.config;

public class BlockStorageProperties {
    private boolean breakable = false;
    private boolean storageable = false;

    public BlockStorageProperties(boolean breakable, boolean storageable) {
        this.breakable = breakable;
        this.storageable = storageable;
    }

    public boolean isBreakable() {
        return breakable;
    }

    public boolean isStorageable() {
        return storageable;
    }
}
