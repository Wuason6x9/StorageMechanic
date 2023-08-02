package dev.wuason.storagemechanic.storages.types.furnitures.config;

public class FurnitureStorageProperties {
    private boolean breakable = false;
    private boolean storageable = false;

    public FurnitureStorageProperties(boolean breakable, boolean storageable) {
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
