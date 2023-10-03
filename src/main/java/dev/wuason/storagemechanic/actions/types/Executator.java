package dev.wuason.storagemechanic.actions.types;

import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.types.block.BlockStorage;
import dev.wuason.storagemechanic.storages.types.furnitures.FurnitureStorage;
import dev.wuason.storagemechanic.storages.types.item.config.ItemStorageConfig;

public enum Executator {

    BLOCK_STORAGE(BlockStorage.class),
    STORAGE(Storage.class),
    FURNITURE_STORAGE(FurnitureStorage.class),
    API(null), //CAMBIAR
    ENTITY_STORAGE(null),
    ITEM_STORAGE(ItemStorageConfig.class);


    private Executator(Class<?> executatorClass){
        this.executatorClass = executatorClass;
    }

    private Class<?> executatorClass;

    public Class<?> getExecutatorClass() {
        return executatorClass;
    }
}
