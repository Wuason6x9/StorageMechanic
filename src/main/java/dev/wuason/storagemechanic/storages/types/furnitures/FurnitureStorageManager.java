package dev.wuason.storagemechanic.storages.types.furnitures;

import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.protectionlib.ProtectionLib;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.data.DataManager;
import dev.wuason.storagemechanic.data.SaveCause;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageOriginContext;
import dev.wuason.storagemechanic.storages.types.block.BlockStorage;
import dev.wuason.storagemechanic.storages.types.furnitures.compatibilities.ItemsAdderFurnitureEvents;
import dev.wuason.storagemechanic.storages.types.furnitures.compatibilities.OraxenFurnitureEvents;
import dev.wuason.storagemechanic.storages.types.furnitures.compatibilities.mythic.MythicCrucibleFurnitureManager;
import dev.wuason.storagemechanic.storages.types.furnitures.compatibilities.mythic.skills.EventCancel;
import dev.wuason.storagemechanic.storages.types.furnitures.config.FurnitureStorageConfig;
import dev.wuason.storagemechanic.utils.StorageUtils;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FurnitureStorageManager {
    private boolean itemsAdderLoaded = false;
    private boolean oraxenLoaded = false;
    private static boolean mythicCrucibleLoaded = false;
    private StorageMechanic core;
    private ItemsAdderFurnitureEvents iaEvents;
    private OraxenFurnitureEvents orEvents;
    private MythicCrucibleFurnitureManager mythicCrucibleFurnitureManager;
    private DataManager dataManager;

    private final String KEY_STORAGE = "furniturestorage";
    private final String KEY_SHULKER = "furniturestorageshulker";
    private final NamespacedKey FURNITURE_SHULKER_NAMESPACEDKEY = new NamespacedKey(StorageMechanic.getInstance(),"furniturestorageshulker");
    private volatile HashMap<String, FurnitureStorage> furnitureStorages = new HashMap<>();

    private BukkitTask taskSaves = null;
    private ArrayList<FurnitureStorage> furnitureStoragesToSave = new ArrayList<>();



    public FurnitureStorageManager(StorageMechanic core, DataManager dataManager) {
        this.core = core;
        this.dataManager = dataManager;
        load();
    }

    public void load(){
        PluginManager pm = Bukkit.getPluginManager();
        if(isItemsAdderLoaded()){
            iaEvents = new ItemsAdderFurnitureEvents(this);
            pm.registerEvents(iaEvents,core);
        }
        if(isOraxenLoaded()){
            orEvents = new OraxenFurnitureEvents(this);
            pm.registerEvents(orEvents,core);
        }
        /*if(isMythicCrucibleLoaded()){
            mythicCrucibleFurnitureManager = new MythicCrucibleFurnitureManager(this, core);
            pm.registerEvents(mythicCrucibleFurnitureManager,core);
        }*/

    }





    public FurnitureStorage createFurnitureStorage(String furnitureStorageConfigID, Location furnitureLocation, Player player, String id){
        if(!furnitureStorages.containsKey(StorageUtils.getBlockStorageId(furnitureLocation)) && core.getManagers().getFurnitureStorageConfigManager().furnitureStorageConfigExists(furnitureStorageConfigID)){
            FurnitureStorageConfig furnitureStorageConfig = core.getManagers().getFurnitureStorageConfigManager().findFurnitureStorageConfigById(furnitureStorageConfigID).orElse(null);

            Storage storage = core.getManagers().getStorageManager().createStorage(furnitureStorageConfig.getStorageConfigID(), new StorageOriginContext(StorageOriginContext.context.FURNITURE_STORAGE, new ArrayList<>(){{
                add(furnitureStorageConfigID);
                add(id);
                add(player.getUniqueId().toString());
            }}));
            HashMap<String,Storage> hashMap = new HashMap<>();
            hashMap.put(player.getUniqueId().toString(),storage);
            ArrayList<Location> locations = new ArrayList<>();
            locations.add(furnitureLocation);
            FurnitureStorage furnitureStorage = new FurnitureStorage(id,furnitureStorageConfigID,hashMap,player.getUniqueId(),locations);

            furnitureStorages.put(furnitureStorage.getId(),furnitureStorage);

            return furnitureStorage;
        }

        return null;
    }
    public FurnitureStorage createFurnitureStorage(String furnitureStorageConfigID, ArrayList<Location> locations, Player player,String id){

        if(!furnitureStorages.containsKey(id) && core.getManagers().getFurnitureStorageConfigManager().furnitureStorageConfigExists(furnitureStorageConfigID)){

            FurnitureStorageConfig furnitureStorageConfig = core.getManagers().getFurnitureStorageConfigManager().findFurnitureStorageConfigById(furnitureStorageConfigID).orElse(null);

            HashMap<String,Storage> hashMap = new HashMap<>();
            FurnitureStorage furnitureStorage = new FurnitureStorage(id,furnitureStorageConfigID,hashMap,player.getUniqueId(),locations);

            furnitureStorages.put(furnitureStorage.getId(),furnitureStorage);

            return furnitureStorage;

        }

        return null;
    }

    public FurnitureStorage createFurnitureStorage(String furnitureStorageConfigID, ArrayList<Location> locations, Player player,String id, String storageID){

        if(!furnitureStorages.containsKey(id) && core.getManagers().getFurnitureStorageConfigManager().furnitureStorageConfigExists(furnitureStorageConfigID)){

            FurnitureStorageConfig furnitureStorageConfig = core.getManagers().getFurnitureStorageConfigManager().findFurnitureStorageConfigById(furnitureStorageConfigID).orElse(null);

            Storage storage = core.getManagers().getStorageManager().getStorage(storageID);
            HashMap<String,Storage> hashMap = new HashMap<>();
            hashMap.put(player.getUniqueId().toString(),storage);
            FurnitureStorage furnitureStorage = new FurnitureStorage(id,furnitureStorageConfigID,hashMap,player.getUniqueId(),locations);

            furnitureStorages.put(furnitureStorage.getId(),furnitureStorage);

            return furnitureStorage;
        }

        return null;
    }


    public boolean openFurnitureStorage(String furnitureStorageId, Player player) {

        if (!furnitureStorages.containsKey(furnitureStorageId)) return false;

        FurnitureStorage furnitureStorage = furnitureStorages.get(furnitureStorageId);

        FurnitureStorageConfig furnitureStorageConfig = furnitureStorage.getFurnitureStorageConfig();

        switch (furnitureStorageConfig.getFurnitureStorageType()){
            case ENDER_CHEST -> {
                furnitureStorage.getStoragePlayer(player.getUniqueId().toString()).openStorage(player,0);
            }
            case PERSONAL -> {
                furnitureStorage.getStoragePlayer(player.getUniqueId() + "").openStorage(player,0);
            }
            default -> {
                furnitureStorage.getStoragePlayer(furnitureStorage.getOwnerUUID().toString()).openStorage(player,0);
            }
        }

        return true;
    }


    //EVENTS
    //SAVE FURNITURE STORAGES
    @EventHandler
    public void FurnitureStorageUnLoadChunk(ChunkUnloadEvent event){

        PersistentDataContainer persistentDataContainer = event.getChunk().getPersistentDataContainer();

        for(NamespacedKey namespacedKey : persistentDataContainer.getKeys()){

            if(namespacedKey.getKey().contains(KEY_STORAGE + "_")){

                String[] furnitureStorageData = persistentDataContainer.get(namespacedKey, PersistentDataType.STRING).split(":");

                FurnitureStorage furnitureStorage = getFurnitureStorage(furnitureStorageData[0]);

                if(furnitureStorage != null){

                    TaskSave(furnitureStorage);

                }

            }

        }

    }
    public void TaskSave(FurnitureStorage furnitureStorage){
        furnitureStoragesToSave.add(furnitureStorage);
        TaskStart();
    }
    public void TaskStart(){
        if(taskSaves == null || taskSaves.isCancelled()){
            taskSaves = Bukkit.getScheduler().runTaskAsynchronously(core,() -> {
                while(!furnitureStoragesToSave.isEmpty()){
                    FurnitureStorage furnitureStorage = furnitureStoragesToSave.get(0);
                    saveFurnitureStorage(furnitureStorage, SaveCause.NORMAL_SAVE);
                    furnitureStoragesToSave.remove(furnitureStorage);
                }

                taskSaves.cancel();
            });
        }
    }








    //INTERACT
    public void onFurnitureInteract(String adapterId, Player player, Entity entity, ItemStack itemHand, EventCancel cancellable){

        if(player.isSneaking() || entity == null || player == null) return;
        if(!ProtectionLib.canInteract(player,entity.getLocation())) return;
        Location location = entity.getLocation();
        PersistentDataContainer persistentDataContainer = location.getChunk().getPersistentDataContainer();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        NamespacedKey namespacedKey = new NamespacedKey(core, KEY_STORAGE + "_" + x + "_" + y + "_" + z);

        String[] furnitureStorageData = new String[3];

        if(persistentDataContainer.has(namespacedKey,PersistentDataType.STRING)){

            furnitureStorageData = persistentDataContainer.get(namespacedKey,PersistentDataType.STRING).split(":");

        }
        //SI EXISTE EL STORAGE
        if(furnitureStorageData[0] != null){
            if(furnitureStorageExists(furnitureStorageData[0])){
                FurnitureStorageConfig furnitureStorageConfig = core.getManagers().getFurnitureStorageConfigManager().findFurnitureStorageConfigByItemID(adapterId);
                FurnitureStorage furnitureStorage = getFurnitureStorage(furnitureStorageData[0]);
                cancellable.setCancelled(true);
                switch (furnitureStorageConfig.getFurnitureStorageType()){
                    case PERSONAL -> {
                        if(!furnitureStorage.existStoragePlayer(player)){
                            furnitureStorage.createStoragePlayer(player.getUniqueId().toString());
                        }
                        String furnitureStorageId = furnitureStorageData[0];
                        Bukkit.getScheduler().runTask(core, () -> openFurnitureStorage(furnitureStorageId, player));
                    }
                    case ENDER_CHEST ->{
                        if(!furnitureStorage.existStoragePlayer(player)){
                            furnitureStorage.createStoragePlayer(player);
                        }
                        String furnitureStorageId = furnitureStorageData[0];
                        Bukkit.getScheduler().runTask(core, () -> openFurnitureStorage(furnitureStorageId, player));
                    }
                    default -> {
                        String furnitureStorageId = furnitureStorageData[0];
                        Bukkit.getScheduler().runTask(core, () -> openFurnitureStorage(furnitureStorageId, player));
                    }
                }

            }
        }
        //SI NO EXISTE EL STORAGE
        else {
            FurnitureStorageConfig furnitureStorageConfig = core.getManagers().getFurnitureStorageConfigManager().findFurnitureStorageConfigByItemID(adapterId);
            if(furnitureStorageConfig != null){
                cancellable.setCancelled(true);
                FurnitureStorage furnitureStorage = null;
                switch (furnitureStorageConfig.getFurnitureStorageType()){
                    case SHULKER -> {
                        NamespacedKey namespacedShulker = new NamespacedKey(core,KEY_SHULKER + "_" + x + "_" + y + "_" + z);
                        if(persistentDataContainer.has(namespacedShulker,PersistentDataType.STRING)){
                            String[] furnitureStorageShulkerData = persistentDataContainer.get(namespacedShulker,PersistentDataType.STRING).split(":");
                            furnitureStorage = loadFurnitureStorage(furnitureStorageShulkerData[0]);
                            persistentDataContainer.set(namespacedKey,PersistentDataType.STRING, furnitureStorageShulkerData[0] + ":" + furnitureStorageShulkerData[1] + ":" + furnitureStorageShulkerData[2]);
                            persistentDataContainer.remove(namespacedShulker);
                        }
                        else {
                            furnitureStorage = createFurnitureStorage(furnitureStorageConfig.getId(),entity.getLocation(),player,StorageUtils.getBlockStorageId(entity.getLocation()));
                            persistentDataContainer.set(namespacedKey,PersistentDataType.STRING, furnitureStorage.getId() + ":" + furnitureStorage.getFurnitureStorageConfigID() + ":" + furnitureStorage.getOwnerUUID());
                        }
                    }
                    case ENDER_CHEST -> {
                        furnitureStorage = getFurnitureStorage(furnitureStorageConfig.getId());
                        ArrayList<Location> locations = new ArrayList<>();
                        if(furnitureStorage == null){
                            furnitureStorage = createFurnitureStorage(furnitureStorageConfig.getId(),locations,player,furnitureStorageConfig.getId());
                        }
                        locations = furnitureStorage.getLocs();
                        locations.add(new Location(location.getWorld(),location.getX(),location.getY(),location.getZ()));
                        furnitureStorage.createStoragePlayer(player);
                        persistentDataContainer.set(namespacedKey,PersistentDataType.STRING, furnitureStorage.getId() + ":" + furnitureStorage.getFurnitureStorageConfigID() + ":" + furnitureStorage.getOwnerUUID());
                    }
                    default -> {
                        furnitureStorage = createFurnitureStorage(furnitureStorageConfig.getId(),location,player,StorageUtils.getBlockStorageId(location));
                        persistentDataContainer.set(namespacedKey,PersistentDataType.STRING, furnitureStorage.getId() + ":" + furnitureStorage.getFurnitureStorageConfigID() + ":" + furnitureStorage.getOwnerUUID());
                    }
                }
                String furnitureStorageId = furnitureStorage.getId();
                Bukkit.getScheduler().runTask(core, () -> openFurnitureStorage(furnitureStorageId, player));
            }

        }


    }
    //PLACE
    public void onFurniturePlace(String adapterId, Player player, Entity entity, ItemStack itemHand, EventCancel event){

        if(player != null && entity != null && itemHand != null && !itemHand.getType().equals(Material.AIR)){

            PersistentDataContainer persistentDataContainerItem = itemHand.getItemMeta().getPersistentDataContainer();

            if(persistentDataContainerItem.has(FURNITURE_SHULKER_NAMESPACEDKEY, PersistentDataType.STRING)){

                core.getManagers().getFurnitureStorageConfigManager().findFurnitureStorageConfigById(persistentDataContainerItem.get(FURNITURE_SHULKER_NAMESPACEDKEY,PersistentDataType.STRING).split(":")[1]).ifPresent(furnitureConfig -> {

                    player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

                    String[] data = persistentDataContainerItem.get(FURNITURE_SHULKER_NAMESPACEDKEY,PersistentDataType.STRING).split(":");

                    String furnitureStorageID = data[0];
                    String furnitureStorageConfigID = data[1];
                    String ownerUUID = data[2];
                    //NameSpace
                    PersistentDataContainer persistentDataContainerFurniture = entity.getLocation().getChunk().getPersistentDataContainer();
                    int x = entity.getLocation().getBlockX();
                    int y = entity.getLocation().getBlockY();
                    int z = entity.getLocation().getBlockZ();
                    NamespacedKey namespacedKey = new NamespacedKey(core,KEY_SHULKER + "_" + x + "_" + y + "_" + z);

                    persistentDataContainerFurniture.set(namespacedKey,PersistentDataType.STRING, furnitureStorageID + ":" + furnitureStorageConfigID + ":" + ownerUUID);

                });

            }
        }

    }
    //BREAK
    public void onFurnitureBreak(String adapterId, Player player, Entity entity, ItemStack itemHand, EventCancel eventCancel){

        if(entity == null || player == null || adapterId == null) return;
        if(isFurnitureStorageByLoc(entity.getLocation())){

            FurnitureStorage furnitureStorage = getFurnitureStorageByLoc(entity.getLocation());
            FurnitureStorageConfig furnitureStorageConfig = furnitureStorage.getFurnitureStorageConfig();

            switch (furnitureStorageConfig.getFurnitureStorageType()){
                case ENDER_CHEST -> {
                    //ELIMINAR DE MEMORIA Y DE PERSISTENCIA
                    removeFurnitureStoragePersistence(entity.getLocation());
                }
                case SHULKER -> { //GUARDAR EN DATA ✅
                    removeFurnitureStoragePersistence(entity.getLocation());
                    ItemStack item = Mechanics.getInstance().getManager().getAdapterManager().getItemStack(furnitureStorageConfig.getFurniture());
                    ItemMeta itemMeta = item.getItemMeta();
                    itemMeta.getPersistentDataContainer().set(FURNITURE_SHULKER_NAMESPACEDKEY,PersistentDataType.STRING, furnitureStorage.getId() + ":" + furnitureStorageConfig.getId() + ":" + furnitureStorage.getOwnerUUID());
                    item.setItemMeta(itemMeta);
                    Bukkit.getScheduler().runTask(core, () -> entity.getWorld().dropItem(entity.getLocation(),item));
                    furnitureStorage.delete();
                    saveFurnitureStorage(furnitureStorage,SaveCause.NORMAL_SAVE);
                }
                default -> {
                    if(!furnitureStorageConfig.getFurnitureStorageProperties().isBreakable()){
                        boolean allEmpty = true;
                        for(Storage storage : furnitureStorage.getStorages().values()){
                            if(!storage.isEmpty()){
                                allEmpty = false;
                            }
                        }
                        if(!allEmpty){
                            AdventureUtils.playerMessage(core.getConfigDocumentYaml().getString("messages.type.furniturestorage.isBreakeable"), player);
                            eventCancel.setCancelled(true);
                            return;
                        }
                    }

                    for(Storage storage : furnitureStorage.getStorages().values()){
                        storage.dropAllItems(entity.getLocation());
                        storage.closeAllInventory();
                        core.getManagers().getStorageManager().removeStorage(storage.getId());
                    }
                    furnitureStorage.getStorages().clear();
                    //ELIMINAR DE MEMORIA Y DE PERSISTENCIA
                    removeFurnitureStoragePersistence(entity.getLocation());
                    removeFurnitureStorage(furnitureStorage.getId());
                    furnitureStorage.delete();
                }
            }
        }


    }




    public NamespacedKey getNamespacedKey(Location location){
        return new NamespacedKey(core,KEY_STORAGE + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ());
    }
    public boolean removeFurnitureStoragePersistence(Location location){
        if(location.getChunk().getPersistentDataContainer().has(new NamespacedKey(core,KEY_STORAGE + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ()),PersistentDataType.STRING)){
            location.getChunk().getPersistentDataContainer().remove(new NamespacedKey(core,KEY_STORAGE + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ()));
            return true;
        }
        return false;
    }
    public boolean removeFurnitureStoragePersistence(PersistentDataContainer persistentDataContainer, int x, int y, int z){
        if(persistentDataContainer.has(new NamespacedKey(core,KEY_STORAGE + "_" + x + "_" + y + "_" + z),PersistentDataType.STRING)){
            persistentDataContainer.remove(new NamespacedKey(core,KEY_STORAGE + "_" + x + "_" + y + "_" + z));
            return true;
        }
        return false;
    }
    public boolean removeFurnitureStoragePersistenceShulker(PersistentDataContainer persistentDataContainer,int x, int y, int z){
        if(persistentDataContainer.has(new NamespacedKey(core,KEY_SHULKER + "_" + x + "_" + y + "_" + z),PersistentDataType.STRING)){
            persistentDataContainer.remove(new NamespacedKey(core,KEY_SHULKER + "_" + x + "_" + y + "_" + z));
            return true;
        }
        return false;
    }
    public void removeAllFurnitureStorageChunk(Chunk chunk){
        for(NamespacedKey namespacedKey : chunk.getPersistentDataContainer().getKeys()){
            if(namespacedKey.getKey().contains(KEY_STORAGE + "_")){
                chunk.getPersistentDataContainer().remove(namespacedKey);
            }
        }
    }


    //BASICS METHODS
    public void removeFurnitureStorage(String furnitureStorageId) {
        if(furnitureStorages.containsKey(furnitureStorageId)) furnitureStorages.remove(furnitureStorageId);
        if(dataManager.getStorageManagerData().getFurnitureStorageManagerData().existFurnitureStorageData(furnitureStorageId)) dataManager.getStorageManagerData().getFurnitureStorageManagerData().removeFurnitureStorageData(furnitureStorageId);
    }

    // Método para buscar un FurnitureStorage por su ID
    public FurnitureStorage getFurnitureStorage(String furnitureStorageId) {
        if(furnitureStorages.containsKey(furnitureStorageId)) return furnitureStorages.get(furnitureStorageId);
        if(dataManager.getStorageManagerData().getFurnitureStorageManagerData().existFurnitureStorageData(furnitureStorageId)) return loadFurnitureStorage(furnitureStorageId);
        return null;
    }

    // Método para verificar si existe un FurnitureStorage por su ID
    public boolean furnitureStorageExists(String furnitureStorageId) {
        if(furnitureStorages.containsKey(furnitureStorageId)) return true;
        if(dataManager.getStorageManagerData().getFurnitureStorageManagerData().existFurnitureStorageData(furnitureStorageId)) return true;
        return false ;
    }

    //methods Data
    public void saveFurnitureStorage(FurnitureStorage furnitureStorage, SaveCause saveCause){
        String id = furnitureStorage.getId();
        if(furnitureStorages.containsKey(id)) furnitureStorages.remove(id);
        for(Storage storage : furnitureStorage.getStorages().values()){
            core.getManagers().getStorageManager().saveStorage(storage,saveCause);
        }
        dataManager.getStorageManagerData().getFurnitureStorageManagerData().saveFurnitureStorage(furnitureStorage);
    }
    public void saveFurnitureStorageNoRemove(FurnitureStorage furnitureStorage){
        for(Storage storage : furnitureStorage.getStorages().values()){
            core.getManagers().getStorageManager().saveStorageNoRemove(storage);
        }
        dataManager.getStorageManagerData().getFurnitureStorageManagerData().saveFurnitureStorage(furnitureStorage);
    }

    public void saveAllFurnitureStorages(){
        for(Map.Entry<String, FurnitureStorage> blockStorageMap : furnitureStorages.entrySet()){
            saveFurnitureStorageNoRemove(blockStorageMap.getValue());
        }
    }

    public FurnitureStorage loadFurnitureStorage(String id){
        if(!furnitureStorages.containsKey(id)){
            FurnitureStorage furnitureStorage = dataManager.getStorageManagerData().getFurnitureStorageManagerData().loadFurnitureStorageData(id);
            if(furnitureStorage == null) return null;
            furnitureStorages.put(furnitureStorage.getId(),furnitureStorage);
            return furnitureStorage;
        }
        return null;
    }

    public boolean isShulker(ItemStack itemStack){
        return itemStack.getItemMeta().getPersistentDataContainer().has(FURNITURE_SHULKER_NAMESPACEDKEY, PersistentDataType.STRING);
    }
    public String getShulkerData(ItemStack itemStack){
        return itemStack.getItemMeta().getPersistentDataContainer().get(FURNITURE_SHULKER_NAMESPACEDKEY,PersistentDataType.STRING);
    }

    public void stop(){

        while(!furnitureStorages.values().isEmpty()){
            FurnitureStorage furnitureStorage = (FurnitureStorage) (furnitureStorages.values().toArray())[0];

            saveFurnitureStorage(furnitureStorage,SaveCause.STOPPING_SAVE);
        }

    }



    //METODOS
    // Método para obtener todos los FurnitureStorages
    public HashMap<String, FurnitureStorage> getAllFurnitureStorages() {
        return this.furnitureStorages;
    }

    // Método para limpiar todos los FurnitureStorages
    public void clearAllFurnitureStorages() {
        this.furnitureStorages.clear();
    }

    public void updateFurnitureStorage(String furnitureStorageId, FurnitureStorage newFurnitureStorage) {
        this.furnitureStorages.put(furnitureStorageId, newFurnitureStorage);
    }

    // Método para obtener todos los IDs de los FurnitureStorages almacenados
    public Set<String> getAllFurnitureStorageIds() {
        return this.furnitureStorages.keySet();
    }

    // Método para obtener una lista de todos los FurnitureStorages almacenados
    public List<FurnitureStorage> getAllFurnitureStoragesAsList() {
        return new ArrayList<>(this.furnitureStorages.values());
    }

    // Método para verificar si un objeto FurnitureStorage específico está almacenado
    public boolean containsFurnitureStorage(FurnitureStorage furnitureStorage) {
        return this.furnitureStorages.containsValue(furnitureStorage);
    }

    // Método para cambiar el HashMap a un HashMap inmutable (no se pueden hacer más cambios)
    public void makeImmutable() {
        this.furnitureStorages = (HashMap<String, FurnitureStorage>) Collections.unmodifiableMap(this.furnitureStorages);
    }

    // Método para filtrar los FurnitureStorages por una condición específica (por ejemplo, sólo los FurnitureStorages que fueron creados por un jugador específico)
    public List<FurnitureStorage> filterFurnitureStorages(Predicate<FurnitureStorage> condition) {
        return this.furnitureStorages.values().stream().filter(condition).collect(Collectors.toList());
    }

    // Método para ordenar los FurnitureStorages por una condición específica (por ejemplo, ordenar por la fecha de creación si esa información está disponible)
    public List<FurnitureStorage> sortFurnitureStorages(Comparator<FurnitureStorage> condition) {
        return this.furnitureStorages.values().stream().sorted(condition).collect(Collectors.toList());
    }

    // Método para transformar los FurnitureStorages a otro tipo de objetos (por ejemplo, convertir a DTO si se necesita enviar los datos a una API)
    public <T> List<T> mapFurnitureStorages(Function<FurnitureStorage, T> mapper) {
        return (List<T>) this.furnitureStorages.values().stream().map(mapper).collect(Collectors.toList());
    }





    public boolean isFurnitureStorageByLoc(Location location){
        if(location.getChunk().getPersistentDataContainer().has(new NamespacedKey(core, KEY_STORAGE + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ()), PersistentDataType.STRING)) return true;
        if(location.getChunk().getPersistentDataContainer().has(new NamespacedKey(core, KEY_SHULKER + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ()), PersistentDataType.STRING)) return true;
        return false;
    }

    public String getFurnitureStorageIDByLoc(Location location){
        if(location.getChunk().getPersistentDataContainer().has(new NamespacedKey(core,KEY_STORAGE + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ()),PersistentDataType.STRING)) return location.getChunk().getPersistentDataContainer().get(new NamespacedKey(core,KEY_STORAGE + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ()),PersistentDataType.STRING).split(":")[0];
        if(location.getChunk().getPersistentDataContainer().has(new NamespacedKey(core,KEY_SHULKER + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ()),PersistentDataType.STRING)) return location.getChunk().getPersistentDataContainer().get(new NamespacedKey(core,KEY_SHULKER + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ()),PersistentDataType.STRING).split(":")[0];
        return null;
    }

    public FurnitureStorage getFurnitureStorageByLoc(Location location){
        return getFurnitureStorage(getFurnitureStorageIDByLoc(location));
    }



    public boolean isItemsAdderLoaded(){
        if(!itemsAdderLoaded) itemsAdderLoaded = Bukkit.getPluginManager().getPlugin("ItemsAdder") != null;
        return itemsAdderLoaded;
    }
    public boolean isOraxenLoaded(){
        if(!oraxenLoaded) oraxenLoaded = Bukkit.getPluginManager().getPlugin("Oraxen") != null;
        return oraxenLoaded;
    }
    public static boolean isMythicCrucibleLoaded(){
        if(!mythicCrucibleLoaded) mythicCrucibleLoaded = Bukkit.getPluginManager().getPlugin("MythicCrucible") != null;
        return mythicCrucibleLoaded;
    }

    public StorageMechanic getCore() {
        return core;
    }

    public ItemsAdderFurnitureEvents getIaEvents() {
        return iaEvents;
    }

    public OraxenFurnitureEvents getOrEvents() {
        return orEvents;
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}
