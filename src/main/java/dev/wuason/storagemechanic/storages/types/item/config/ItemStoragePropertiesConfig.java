package dev.wuason.storagemechanic.storages.types.item.config;

public class ItemStoragePropertiesConfig {

    private boolean storageable = false;

    public ItemStoragePropertiesConfig(boolean storageable) {
        this.storageable = storageable;
    }

    public boolean isStorageable() {
        return storageable;
    }
}
