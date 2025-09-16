package dev.wuason.storagemechanic.storages.config;

public enum StorageInventoryTypeConfig {
    CHEST(54),
    DROPPER(9),
    HOPPER(5),
    DISPENSER(9);

    private int size;

    private StorageInventoryTypeConfig(int size) {
        this.size = size;
    }


    public int getSize() {
        return size;
    }
}
