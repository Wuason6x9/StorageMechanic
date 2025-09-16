package dev.wuason.storagemechanic.data.storage.type.furniture;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

public class FurnitureStorageData implements Serializable {
    private UUID ownerUUID;
    private HashMap<String, String> storagesID;
    private String furnitureStorageID;
    private String furnitureStorageConfigID;

    private String[] locs;

    public FurnitureStorageData(UUID ownerUUID, HashMap<String, String> storagesID, String furnitureStorageID, String furnitureStorageConfigID, String[] locs) {
        this.ownerUUID = ownerUUID;
        this.storagesID = storagesID;
        this.furnitureStorageID = furnitureStorageID;
        this.furnitureStorageConfigID = furnitureStorageConfigID;
        this.locs = locs;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public HashMap<String, String> getStoragesID() {
        return storagesID;
    }

    public String getFurnitureStorageID() {
        return furnitureStorageID;
    }

    public String getFurnitureStorageConfigID() {
        return furnitureStorageConfigID;
    }

    public String[] getLocs() {
        return locs;
    }


}
