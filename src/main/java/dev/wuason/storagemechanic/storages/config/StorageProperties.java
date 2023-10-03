package dev.wuason.storagemechanic.storages.config;

public class StorageProperties {
    private boolean tempStorage = false;
    private boolean dropItemsPageOnClose = false;

    public StorageProperties(boolean tempStorage, boolean dropItemsPageOnClose) {
        this.tempStorage = tempStorage;
        this.dropItemsPageOnClose = dropItemsPageOnClose;
    }

    public boolean isTempStorage() {
        return tempStorage;
    }

    public boolean isDropItemsPageOnClose() {
        return dropItemsPageOnClose;
    }
}
