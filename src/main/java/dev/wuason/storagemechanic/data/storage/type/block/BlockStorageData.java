package dev.wuason.storagemechanic.data.storage.type.block;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

public class BlockStorageData implements Serializable {
    private UUID ownerUUID;
    private HashMap<String,String> storagesID;
    private String blockStorageID;
    private String blockStorageConfigID;

    private String[] locs;

    public BlockStorageData(UUID ownerUUID, HashMap<String,String> storagesID, String blockStorageID, String blockStorageConfigID, String[] locs) {
        this.ownerUUID = ownerUUID;
        this.storagesID = storagesID;
        this.blockStorageID = blockStorageID;
        this.blockStorageConfigID = blockStorageConfigID;
        this.locs = locs;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public HashMap<String,String> getStoragesID() {
        return storagesID;
    }

    public String getBlockStorageID() {
        return blockStorageID;
    }

    public String getBlockStorageConfigID() {
        return blockStorageConfigID;
    }

    public String[] getLocs() {
        return locs;
    }
}
