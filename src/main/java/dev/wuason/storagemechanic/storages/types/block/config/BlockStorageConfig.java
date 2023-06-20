package dev.wuason.storagemechanic.storages.types.block.config;

import dev.wuason.storagemechanic.storages.types.block.mechanics.BlockMechanic;

public class BlockStorageConfig {
    private BlockStorageType blockStorageType;
    private String id;
    private BlockStorageProperties blockStorageProperties;
    private BlockStorageClickType blockStorageClickType;
    private String block = null;
    private BlockMechanic[] blockMechanics;
    private String storageID;

    public BlockStorageConfig(BlockStorageType blockStorageType, String id, BlockStorageProperties blockStorageProperties, BlockStorageClickType blockStorageClickType, String block, BlockMechanic[] blockMechanics, String storageID) {
        this.blockStorageType = blockStorageType;
        this.id = id;
        this.blockStorageProperties = blockStorageProperties;
        this.blockStorageClickType = blockStorageClickType;
        this.block = block;
        this.blockMechanics = blockMechanics;
        this.storageID = storageID;
    }

    public BlockStorageType getBlockStorageType() {
        return blockStorageType;
    }

    public String getId() {
        return id;
    }

    public BlockStorageProperties getBlockStorageProperties() {
        return blockStorageProperties;
    }

    public BlockStorageClickType getBlockStorageClickType() {
        return blockStorageClickType;
    }

    public String getBlock() {
        return block;
    }

    public BlockMechanic[] getBlockMechanics() {
        return blockMechanics;
    }

    public String getStorageConfigID() {
        return storageID;
    }
}
