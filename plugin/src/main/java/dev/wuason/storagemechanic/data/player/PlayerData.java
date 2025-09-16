package dev.wuason.storagemechanic.data.player;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


public class PlayerData implements Serializable {
    public PlayerData(UUID uuid) {

        this.uuid = uuid;

    }

    private UUID uuid;
    private HashMap<String, String> storages = new HashMap<>(); //STORAGE ID & CONTEXT
    private Set<String> blockStorages = new HashSet<>();
    private Set<String> furnitureStorages = new HashSet<>();

    public HashMap<String, String> getStorages() {
        return storages;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setStorages(HashMap<String, String> storages) {
        this.storages = storages;
    }

    public Set<String> getBlockStorages() {
        return blockStorages;
    }

    public void setBlockStorages(Set<String> blockStorages) {
        this.blockStorages = blockStorages;
    }

    public Set<String> getFurnitureStorages() {
        return furnitureStorages;
    }
}
