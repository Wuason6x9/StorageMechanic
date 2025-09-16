package dev.wuason.storagemechanic.storages.types.furnitures;

import dev.wuason.storagemechanic.Managers;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.data.player.PlayerData;
import dev.wuason.storagemechanic.data.player.PlayerDataManager;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageManager;
import dev.wuason.storagemechanic.storages.StorageOriginContext;
import dev.wuason.storagemechanic.storages.types.furnitures.config.FurnitureStorageConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class FurnitureStorage {

    private String id;
    private String furnitureStorageConfigID;

    private HashMap<String, Storage> storages = new HashMap<>();
    private UUID owner;
    private ArrayList<Location> locs = new ArrayList<>();
    public final static String STORAGE_CONTEXT = "FURNITURE_STORAGE";


    public FurnitureStorage(String id, String furnitureStorageConfigID, HashMap<String, Storage> storages, UUID owner, ArrayList<Location> locs) {
        this.id = id;
        this.furnitureStorageConfigID = furnitureStorageConfigID;
        this.storages = storages;
        if (owner != null) this.owner = owner;
        this.locs = locs;
        PlayerDataManager playerDataManager = StorageMechanic.getInstance().getManagers().getDataManager().getPlayerDataManager();
        //Storage player data
        if (!storages.isEmpty()) {
            for (String playerUUID : storages.keySet()) {
                UUID uuid = UUID.fromString(playerUUID);
                Storage storage = storages.get(playerUUID);
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                if (playerDataManager.existPlayerData(uuid)) {
                    PlayerData playerData = playerDataManager.getPlayerData(uuid);
                    if (playerData != null) {
                        playerData.getStorages().put(storage.getId(), STORAGE_CONTEXT + "_" + furnitureStorageConfigID + "_" + id);
                        if (!offlinePlayer.isOnline()) {
                            playerDataManager.savePlayerData(uuid);
                        }
                    }
                }
            }
        }
        //FurnitureStorage player data
        if (owner != null) {
            if (playerDataManager.existPlayerData(owner)) {
                PlayerData playerData = playerDataManager.getPlayerData(owner);
                if (playerData != null) {
                    playerData.getFurnitureStorages().add(id);
                    OfflinePlayer player = Bukkit.getOfflinePlayer(owner);
                    if (!player.isOnline()) {
                        playerDataManager.savePlayerData(owner);
                    }
                }
            }
        }
    }

    public String getId() {
        return id;
    }

    public Player getOwnerPlayer() {
        return Bukkit.getPlayer(owner);
    }

    public OfflinePlayer getOwnerOfflinePlayer() {
        return Bukkit.getOfflinePlayer(owner);
    }

    public UUID getOwnerUUID() {
        return owner;
    }


    public FurnitureStorageConfig getFurnitureStorageConfig() {
        return StorageMechanic.getInstance().getManagers().getFurnitureStorageConfigManager().findFurnitureStorageConfigById(furnitureStorageConfigID).orElse(null);
    }

    public HashMap<String, Storage> getStorages() {
        return storages;
    }

    public Location getFirstLocation() {
        return locs.get(0);
    }

    public boolean existStoragePlayer(Player player) {
        return storages.containsKey(player.getUniqueId().toString());
    }

    public Storage getStoragePlayer(Player player) {
        return existStoragePlayer(player) ? storages.get(player.getUniqueId().toString()) : null;
    }

    public Storage removeStoragePlayer(Player player) {
        return existStoragePlayer(player) ? storages.remove(player.getUniqueId().toString()) : null;
    }

    public Storage createStoragePlayer(Player player) {
        Storage storage = null;
        if (!existStoragePlayer(player)) {
            Managers managers = StorageMechanic.getInstance().getManagers();
            storage = managers.getStorageManager().createStorage(managers.getFurnitureStorageConfigManager().findFurnitureStorageConfigById(furnitureStorageConfigID).get().getStorageConfigID(), new StorageOriginContext(StorageOriginContext.Context.FURNITURE_STORAGE, new ArrayList<>() {{
                add(furnitureStorageConfigID);
                add(getId());
                add(player.getUniqueId().toString());
            }}));
            storages.put(player.getUniqueId().toString(), storage);
            //Storage Player Data
            PlayerDataManager playerDataManager = managers.getDataManager().getPlayerDataManager();
            UUID uuid = player.getUniqueId();
            if (playerDataManager.existPlayerData(uuid)) {
                PlayerData playerData = playerDataManager.getPlayerData(uuid);
                if (playerData != null) {
                    playerData.getStorages().put(storage.getId(), STORAGE_CONTEXT + "_" + furnitureStorageConfigID + "_" + id);
                    if (!player.isOnline()) {
                        playerDataManager.savePlayerData(uuid);
                    }
                }
            }
        }
        return storage;
    }

    public boolean existStoragePlayer(String id) {
        if (id == null) return false;
        return storages.containsKey(id);
    }

    public Storage getStoragePlayer(String id) {
        if (id == null) return null;
        return storages.get(id);
    }

    public Storage removeStoragePlayer(String id) {
        if (id == null) return null;
        return storages.remove(id);
    }

    public Storage createStoragePlayer(String id) {
        if (id == null || existStoragePlayer(id)) return null;

        Managers managers = StorageMechanic.getInstance().getManagers();
        StorageManager storageManager = managers.getStorageManager();
        FurnitureStorageConfig furnitureStorageConfig = managers.getFurnitureStorageConfigManager().findFurnitureStorageConfigById(furnitureStorageConfigID).orElse(null);

        Storage storage = storageManager.createStorage(furnitureStorageConfig.getStorageConfigID(), new StorageOriginContext(StorageOriginContext.Context.FURNITURE_STORAGE, new ArrayList<>() {{
            add(furnitureStorageConfigID);
            add(getId());
            add(id);
        }}));
        storages.put(id, storage);

        return storage;
    }


    public Location removeLocationAt(int index) {
        return locs.remove(index);
    }

    public int indexOfLocation(Location loc) {
        return locs.indexOf(loc);
    }

    public boolean isLocationAt(int index, Location loc) {
        return locs.get(index).equals(loc);
    }

    public void replaceLocationAt(int index, Location loc) {
        locs.set(index, loc);
    }

    public ArrayList<Location> cloneLocs() {
        return (ArrayList<Location>) locs.clone();
    }

    public List<Location> subListLocs(int start, int end) {
        return locs.subList(start, end);
    }

    public void addAllLocs(Collection<Location> locCollection) {
        locs.addAll(locCollection);
    }

    public void removeAllLocs(Collection<Location> locCollection) {
        locs.removeAll(locCollection);
    }

    public boolean retainAllLocs(Collection<Location> locCollection) {
        return locs.retainAll(locCollection);
    }

    public Iterator<Location> locsIterator() {
        return locs.iterator();
    }

    public int getLocationCount() {
        return locs.size();
    }

    public Location getLocation(int index) {
        return locs.get(index);
    }

    public void addLocation(Location loc) {
        locs.add(loc);
    }

    public void addLocationAt(int index, Location loc) {
        locs.add(index, loc);
    }

    public void setLocs(ArrayList<Location> locs) {
        this.locs = locs;
    }

    public ArrayList<Location> getLocs() {
        return this.locs;
    }

    public boolean isLocsEmpty() {
        return locs.isEmpty();
    }

    public void clearLocs() {
        locs.clear();
    }

    public void removeLocation(Location loc) {
        locs.remove(loc);
    }

    public boolean containsLocation(Location loc) {
        return locs.contains(loc);
    }

    public void delete() { //DELETE ALL FROM PLAYER DATA

        PlayerDataManager playerDataManager = StorageMechanic.getInstance().getManagers().getDataManager().getPlayerDataManager();

        //Furniture STORAGE
        if (playerDataManager.existPlayerData(owner)) {
            OfflinePlayer OwnerOfflinePlayer = getOwnerOfflinePlayer();
            PlayerData playerData = playerDataManager.getPlayerData(owner);
            if (playerData != null) {

                playerData.getFurnitureStorages().remove(id);

                if (!OwnerOfflinePlayer.isOnline()) {
                    playerDataManager.savePlayerData(owner);
                }
            }
        }

        //STORAGES
        if (!storages.isEmpty()) {
            for (String playerUUID : storages.keySet()) {

                UUID uuid = UUID.fromString(playerUUID);
                Storage storage = storages.get(playerUUID);
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);

                if (playerDataManager.existPlayerData(uuid)) {

                    PlayerData playerData = playerDataManager.getPlayerData(uuid);

                    if (playerData != null) {

                        playerData.getStorages().remove(storage.getId());

                        if (!offlinePlayer.isOnline()) {
                            playerDataManager.savePlayerData(uuid);
                        }

                    }
                }

            }
        }
    }

    public String getFurnitureStorageConfigID() {
        return furnitureStorageConfigID;
    }

}
