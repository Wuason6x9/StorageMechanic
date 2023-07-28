package dev.wuason.storagemechanic.storages.types.furnitures.config;

public class FurnitureStorageProperties {
    private boolean breakable = false;

    public FurnitureStorageProperties(boolean breakable) {
        this.breakable = breakable;
    }

    public boolean isBreakable() {
        return breakable;
    }
}
