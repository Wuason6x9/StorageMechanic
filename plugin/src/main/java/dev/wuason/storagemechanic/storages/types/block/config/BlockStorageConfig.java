package dev.wuason.storagemechanic.storages.types.block.config;

import java.util.HashMap;

public class BlockStorageConfig {
    private BlockStorageType blockStorageType;
    private String id;
    private BlockStorageProperties blockStorageProperties;
    private BlockStorageClickType blockStorageClickType;
    private String block = null;
    private HashMap<String, BlockStorageMechanicConfig> mechanicConfigHashMap;
    private String storageID;

    public BlockStorageConfig(BlockStorageType blockStorageType, String id, BlockStorageProperties blockStorageProperties, BlockStorageClickType blockStorageClickType, String block, String storageID, HashMap<String, BlockStorageMechanicConfig> mechanicConfigHashMap) {
        this.blockStorageType = blockStorageType;
        this.id = id;
        this.blockStorageProperties = blockStorageProperties;
        this.blockStorageClickType = blockStorageClickType;
        this.block = block;
        this.mechanicConfigHashMap = mechanicConfigHashMap;
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

    public HashMap<String, BlockStorageMechanicConfig> getMechanicConfigHashMap() {
        return mechanicConfigHashMap;
    }

    public String getStorageConfigID() {
        return storageID;
    }
}
