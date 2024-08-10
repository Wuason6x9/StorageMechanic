package dev.wuason.storagemechanic.storages.types.furnitures.config;

import dev.wuason.storagemechanic.storages.types.block.config.BlockStorageClickType;
import dev.wuason.storagemechanic.storages.types.block.config.BlockStorageProperties;
import dev.wuason.storagemechanic.storages.types.block.config.BlockStorageType;
import dev.wuason.storagemechanic.storages.types.block.mechanics.BlockMechanic;

public class FurnitureStorageConfig {
    private FurnitureStorageType furnitureStorageType;
    private String id;
    private FurnitureStorageProperties furnitureStorageProperties;
    private String furniture = null;
    //private BlockMechanic[] furnitureMechanics;
    private String storageConfigId;

    public FurnitureStorageConfig(FurnitureStorageType furnitureStorageType, String id, FurnitureStorageProperties furnitureStorageProperties, String furniture,/* BlockMechanic[] blockMechanics,*/ String storageID) {
        this.furnitureStorageType = furnitureStorageType;
        this.id = id;
        this.furnitureStorageProperties = furnitureStorageProperties;
        this.furniture = furniture;
        //this.blockMechanics = blockMechanics;
        this.storageConfigId = storageID;
    }

    public String getId() {
        return id;
    }


    /*
    public BlockMechanic[] getBlockMechanics() {
        return blockMechanics;
    }
    */

    public String getStorageConfigID() {
        return storageConfigId;
    }

    public FurnitureStorageType getFurnitureStorageType() {
        return furnitureStorageType;
    }

    public FurnitureStorageProperties getFurnitureStorageProperties() {
        return furnitureStorageProperties;
    }

    public String getFurniture() {
        return furniture;
    }

    public String getStorageConfigId() {
        return storageConfigId;
    }
}
