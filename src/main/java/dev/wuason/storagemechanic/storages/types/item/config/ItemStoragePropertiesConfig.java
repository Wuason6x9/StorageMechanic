package dev.wuason.storagemechanic.storages.types.item.config;

public class ItemStoragePropertiesConfig {

    private boolean storageable = false;
    private boolean damageable = false;
    private boolean dropAllItemsOnDeath = false;

    public ItemStoragePropertiesConfig(boolean storageable, boolean damageable, boolean dropAllItemsOnDeath) {
        this.storageable = storageable;
        this.damageable = damageable;
        this.dropAllItemsOnDeath = dropAllItemsOnDeath;
    }

    public boolean isStorageable() {
        return storageable;
    }

    public boolean isDamageable() {
        return damageable;
    }

    public boolean isDropAllItemsOnDeath() {
        return dropAllItemsOnDeath;
    }
}
