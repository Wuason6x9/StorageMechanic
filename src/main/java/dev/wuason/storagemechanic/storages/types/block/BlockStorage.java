package dev.wuason.storagemechanic.storages.types.block;

import dev.wuason.storagemechanic.Managers;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageManager;
import dev.wuason.storagemechanic.storages.types.block.config.BlockStorageConfig;
import dev.wuason.storagemechanic.utils.StorageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class BlockStorage {
    private String id;
    private String blockStorageConfigID;

    private HashMap<String,Storage> storages = new HashMap<>();
    private UUID owner;
    private ArrayList<Location> locs = new ArrayList<>();

    public BlockStorage(String id, String blockStorageConfigID, HashMap<String,Storage> storages,Player player,ArrayList<Location> locs) {
        this.id = id;
        this.blockStorageConfigID = blockStorageConfigID;
        this.storages = storages;
        this.owner = player.getUniqueId();
        this.locs = locs;
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
    public UUID getOwnerUUID(){
        return owner;
    }

    public String getBlockStorageConfigID() {
        return blockStorageConfigID;
    }
    public BlockStorageConfig getBlockStorageConfig() {
        return StorageMechanic.getInstance().getManagers().getBlockStorageConfigManager().findBlockStorageConfigById(blockStorageConfigID);
    }

    public HashMap<String, Storage> getStorages() {
        return storages;
    }

    public Location getFirstLocation() {
        return locs.get(0);
    }

    public boolean existStoragePlayer(Player player){
        return storages.containsKey(player.getUniqueId().toString());
    }
    public Storage getStoragePlayer(Player player){
        return existStoragePlayer(player) ? storages.get(player.getUniqueId().toString()) : null;
    }
    public Storage removeStoragePlayer(Player player){
        return existStoragePlayer(player) ? storages.remove(player.getUniqueId().toString()) : null;
    }
    public Storage createStoragePlayer(Player player){
        Storage storage = null;
        if(!existStoragePlayer(player)){
            Managers managers = StorageMechanic.getInstance().getManagers();
            storage = managers.getStorageManager().createStorage(managers.getBlockStorageConfigManager().findBlockStorageConfigById(blockStorageConfigID).getStorageConfigID());
            storages.put(player.getUniqueId().toString(),storage);
        }
        return storage;
    }

    public boolean existStoragePlayer(String id){
        if (id == null) return false;
        return storages.containsKey(id);
    }

    public Storage getStoragePlayer(String id){
        if (id == null) return null;
        return storages.get(id);
    }

    public Storage removeStoragePlayer(String id){
        if (id == null) return null;
        return storages.remove(id);
    }

    public Storage createStoragePlayer(String id){
        if (id == null || existStoragePlayer(id)) return null;

        Managers managers = StorageMechanic.getInstance().getManagers();
        StorageManager storageManager = managers.getStorageManager();
        BlockStorageConfig blockStorageConfig = managers.getBlockStorageConfigManager().findBlockStorageConfigById(blockStorageConfigID);

        Storage storage = storageManager.createStorage(blockStorageConfig.getStorageConfigID());
        storages.put(id, storage);

        return storage;
    }


    public boolean canDeleteBlockStorage() {
        // No se puede eliminar si hay más de una ubicación
        return locs.size() <= 1;
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


}
