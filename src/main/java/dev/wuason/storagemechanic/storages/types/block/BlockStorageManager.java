package dev.wuason.storagemechanic.storages.types.block;

import dev.wuason.mechanics.utils.Adapter;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageManager;
import dev.wuason.storagemechanic.storages.types.block.config.BlockStorageConfig;
import dev.wuason.storagemechanic.storages.types.block.config.BlockStorageConfigManager;
import dev.wuason.storagemechanic.storages.types.block.config.BlockStorageType;
import dev.wuason.storagemechanic.storages.types.block.mechanics.BlockMechanicManager;
import dev.wuason.storagemechanic.utils.StorageUtils;
import io.th0rgal.oraxen.utils.actions.ClickAction;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BlockStorageManager implements Listener {
    private BlockMechanicManager blockMechanicManager;
    private StorageMechanic core;

    private HashMap<String,BlockStorage> blockStorages = new HashMap<>();
    private HashMap<Chunk,BlockStorage> blockStorageChunks = new HashMap<>();

    public BlockStorageManager(StorageMechanic core) {
        this.core = core;
        this.blockMechanicManager = new BlockMechanicManager(core);

    }

    public BlockStorage createBlockStorage(String blockStorageConfigID, Location blockLocation, Player player,String id){

        if(!blockStorages.containsKey(StorageUtils.getBlockStorageId(blockLocation)) && core.getManagers().getBlockStorageConfigManager().blockStorageConfigExists(blockStorageConfigID)){

            BlockStorageConfig blockStorageConfig = core.getManagers().getBlockStorageConfigManager().findBlockStorageConfigById(blockStorageConfigID);

            Storage storage = core.getManagers().getStorageManager().createStorage(blockStorageConfig.getStorageConfigID());
            HashMap<String,Storage> hashMap = new HashMap<>();
            hashMap.put(player.getUniqueId().toString(),storage);
            ArrayList<Location> locations = new ArrayList<>();
            locations.add(blockLocation);
            BlockStorage blockStorage = new BlockStorage(id,blockStorageConfigID,hashMap,player,locations);

            blockStorages.put(blockStorage.getId(),blockStorage);

            return blockStorage;

        }

        return null;
    }

    public boolean openBlockStorage(String blockStorageId, Player player) {

        if (!blockStorages.containsKey(blockStorageId)) {
            return false; // Si no existe, retornamos false
        }

        BlockStorage blockStorage = blockStorages.get(blockStorageId);

        BlockStorageConfig blockStorageConfig = blockStorage.getBlockStorageConfig();

        switch (blockStorageConfig.getBlockStorageType()){
            case ENDER_CHEST -> {
                blockStorage.getStoragePlayer(player.getUniqueId().toString()).openStorage(player,0);
            }
            case PERSONAL -> {
                blockStorage.getStoragePlayer(player.getUniqueId() + "").openStorage(player,0);
            }
            default -> {
                blockStorage.getStoragePlayer(blockStorage.getOwnerUUID().toString()).openStorage(player,0);
            }
        }



        return true;
    }

    public BlockStorage createBlockStorage(String blockStorageConfigID, ArrayList<Location> locations, Player player,String id, String storageID){

        if(!blockStorages.containsKey(id) && core.getManagers().getBlockStorageConfigManager().blockStorageConfigExists(blockStorageConfigID)){

            BlockStorageConfig blockStorageConfig = core.getManagers().getBlockStorageConfigManager().findBlockStorageConfigById(blockStorageConfigID);

            Storage storage = core.getManagers().getStorageManager().getStorage(storageID);
            HashMap<String,Storage> hashMap = new HashMap<>();
            hashMap.put(player.getUniqueId().toString(),storage);
            BlockStorage blockStorage = new BlockStorage(id,blockStorageConfigID,hashMap,player,locations);

            blockStorages.put(blockStorage.getId(),blockStorage);

            return blockStorage;

        }

        return null;
    }


    //ONPLACE
    public void BlockStoragePlaceEvent(Block block, ItemStack itemStack, Player player){

        PersistentDataContainer persistentDataContainer = itemStack.getItemMeta().getPersistentDataContainer();

        if(persistentDataContainer.has(new NamespacedKey(core,"blockstorageshulker"), PersistentDataType.STRING)){
            //seguir
            String[] data = persistentDataContainer.get(new NamespacedKey(core,"blockstorageshulker"),PersistentDataType.STRING).split(":");

            String storageID = data[0];
            String blockStorageConfigID = data[1];

            //load storage
            //create BlockStorage

        }

    }
    //ONBREAK
    public void BlockStorageBreakEvent(Block block, ItemStack itemStack, Player player){



    }
    //-------ONINTERACT--------

    //----BLOCK----
    @EventHandler
    public void BlockInteractEvent(PlayerInteractEvent event){
        if(event.getHand().equals(EquipmentSlot.HAND)){
            Block block = event.getClickedBlock();
            Player player = event.getPlayer();
            Action action = event.getAction();

            System.out.println(block.getChunk().getPersistentDataContainer().getKeys());

            if(block != null && player != null){

                PersistentDataContainer persistentDataContainer = block.getChunk().getPersistentDataContainer();
                int x = block.getX();
                int y = block.getY();
                int z = block.getZ();
                NamespacedKey namespacedKey = new NamespacedKey(core,"BlockStorage_" + x + "_" + y + "_" + z);

                String[] blockStorageData = new String[3];
                String adapterID = Adapter.getAdapterID(block);
                if(persistentDataContainer.has(namespacedKey,PersistentDataType.STRING)){
                    blockStorageData = persistentDataContainer.get(namespacedKey,PersistentDataType.STRING).split(":");
                }
                //SI EXISTE EL STORAGE
                if(blockStorageData[0] != null){
                    if(blockStorageExists(blockStorageData[0])){
                        BlockStorageConfig blockStorageConfig = core.getManagers().getBlockStorageConfigManager().findBlockStorageConfigByItemID(adapterID);
                        if(action == blockStorageConfig.getBlockStorageClickType().getAction()){
                            BlockStorage blockStorage = findBlockStorageById(blockStorageData[0]);
                            event.setCancelled(true);
                            switch (blockStorageConfig.getBlockStorageType()){
                                case PERSONAL -> {
                                    if(!blockStorage.existStoragePlayer(player)){
                                        blockStorage.createStoragePlayer(player.getUniqueId().toString());
                                    }
                                    openBlockStorage(blockStorageData[0],player);
                                }
                                case ENDER_CHEST ->{
                                    if(!blockStorage.existStoragePlayer(player)){
                                        blockStorage.createStoragePlayer(player);
                                    }
                                    openBlockStorage(blockStorageData[0],player);
                                }
                                default -> {
                                    openBlockStorage(blockStorageData[0],player);

                                }
                            }
                        }
                    }
                }
                //SI NO EXISTE EL STORAGE
                else {
                    BlockStorageConfig blockStorageConfig = core.getManagers().getBlockStorageConfigManager().findBlockStorageConfigByItemID(adapterID);
                    if(blockStorageConfig != null){
                        if(action == blockStorageConfig.getBlockStorageClickType().getAction()){
                            event.setCancelled(true);
                            switch (blockStorageConfig.getBlockStorageType()){
                                case ENDER_CHEST -> {
                                    BlockStorage blockStorage = createBlockStorage(blockStorageConfig.getId(),block.getLocation(),player,blockStorageConfig.getId());
                                    persistentDataContainer.set(namespacedKey,PersistentDataType.STRING, blockStorage.getId() + ":" + blockStorage.getBlockStorageConfigID() + ":" + blockStorage.getOwnerUUID());
                                    openBlockStorage(blockStorage.getId(),player);
                                }
                                default -> {
                                    BlockStorage blockStorage = createBlockStorage(blockStorageConfig.getId(),block.getLocation(),player,StorageUtils.getBlockStorageId(block.getLocation()));
                                    persistentDataContainer.set(namespacedKey,PersistentDataType.STRING, blockStorage.getId() + ":" + blockStorage.getBlockStorageConfigID() + ":" + blockStorage.getOwnerUUID());
                                    openBlockStorage(blockStorage.getId(),player);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public BlockStorageSerializable blockStorageToBlockStorageSerializable(BlockStorage blockStorage){
        UUID ownerUUID = blockStorage.getOwnerUUID();
        HashMap<String,String> hashMap = new HashMap<>();
        String blockStorageID = blockStorage.getId();
        String blockStorageConfigID = blockStorage.getBlockStorageConfigID();
        blockStorage.getStorages().forEach((s, storage) -> hashMap.put(s,storage.getId()));

        String[] locs = new String[blockStorage.getLocs().size()];

        for(int i=0;i<blockStorage.getLocs().size();i++){

            Location location = blockStorage.getLocs().get(i);

            String loc = location.getWorld().getUID() + "_" + location.getX() + "_" + location.getY() + "_" + location.getZ();

            locs[i] = loc;

        }

        return new BlockStorageSerializable(ownerUUID, hashMap, blockStorageID, blockStorageConfigID,locs);
    }

    public BlockStorage blockStorageSerializableToBlockStorage(BlockStorageSerializable blockStorageSerializable){
        StorageManager storageManager = core.getManagers().getStorageManager();
        UUID ownerUUID = blockStorageSerializable.getOwnerUUID();
        HashMap<String,Storage> hashMap = new HashMap<>();
        String blockStorageID = blockStorageSerializable.getBlockStorageID();
        String blockStorageConfigID = blockStorageSerializable.getBlockStorageConfigID();
        blockStorageSerializable.getStoragesID().forEach((s, s2) -> hashMap.put(s,storageManager.getStorage(s2)));

        Player player = Bukkit.getPlayer(ownerUUID); // Necesitas obtener al jugador desde su UUID, este código puede variar dependiendo de la API que uses.

        ArrayList<Location> locs = new ArrayList<>();
        String[] locsSerializable = blockStorageSerializable.getLocs();
        for(int i=0;i<blockStorageSerializable.getLocs().length;i++){

            String[] loc = locsSerializable[i].split("_");
            World world = Bukkit.getWorld(loc[0]);
            double x = Double.parseDouble(loc[1]);
            double y = Double.parseDouble(loc[2]);
            double z = Double.parseDouble(loc[3]);
            Location location = new Location(world,x,y,z);
            locs.add(location);

        }

        return new BlockStorage(blockStorageID, blockStorageConfigID, hashMap, player,locs);
    }


    public void removeBlockStorage(String blockStorageId) {
        this.blockStorages.remove(blockStorageId);
    }

    // Método para buscar un BlockStorage por su ID
    public BlockStorage findBlockStorageById(String blockStorageId) {
        return this.blockStorages.get(blockStorageId);
    }

    // Método para verificar si existe un BlockStorage por su ID
    public boolean blockStorageExists(String blockStorageId) {
        return this.blockStorages.containsKey(blockStorageId) ;
    }

    // Método para obtener todos los BlockStorages
    public HashMap<String, BlockStorage> getAllBlockStorages() {
        return this.blockStorages;
    }

    // Método para limpiar todos los BlockStorages
    public void clearAllBlockStorages() {
        this.blockStorages.clear();
    }

    public void updateBlockStorage(String blockStorageId, BlockStorage newBlockStorage) {
        this.blockStorages.put(blockStorageId, newBlockStorage);
    }

    // Método para obtener todos los IDs de los BlockStorages almacenados
    public Set<String> getAllBlockStorageIds() {
        return this.blockStorages.keySet();
    }

    // Método para obtener una lista de todos los BlockStorages almacenados
    public List<BlockStorage> getAllBlockStoragesAsList() {
        return new ArrayList<>(this.blockStorages.values());
    }

    // Método para verificar si un objeto BlockStorage específico está almacenado
    public boolean containsBlockStorage(BlockStorage blockStorage) {
        return this.blockStorages.containsValue(blockStorage);
    }

    // Método para cambiar el HashMap a un HashMap inmutable (no se pueden hacer más cambios)
    public void makeImmutable() {
        this.blockStorages = (HashMap<String, BlockStorage>) Collections.unmodifiableMap(this.blockStorages);
    }

    // Método para filtrar los BlockStorages por una condición específica (por ejemplo, sólo los BlockStorages que fueron creados por un jugador específico)
    public List<BlockStorage> filterBlockStorages(Predicate<BlockStorage> condition) {
        return this.blockStorages.values().stream().filter(condition).collect(Collectors.toList());
    }

    // Método para ordenar los BlockStorages por una condición específica (por ejemplo, ordenar por la fecha de creación si esa información está disponible)
    public List<BlockStorage> sortBlockStorages(Comparator<BlockStorage> condition) {
        return this.blockStorages.values().stream().sorted(condition).collect(Collectors.toList());
    }

    // Método para transformar los BlockStorages a otro tipo de objetos (por ejemplo, convertir a DTO si se necesita enviar los datos a una API)
    public <T> List<T> mapBlockStorages(Function<BlockStorage, T> mapper) {
        return (List<T>) this.blockStorages.values().stream().map(mapper).collect(Collectors.toList());
    }


    public BlockMechanicManager getBlockMechanicManager() {
        return blockMechanicManager;
    }

    public void addBlockStorageChunk(Chunk chunk, BlockStorage blockStorage){
        blockStorageChunks.put(chunk, blockStorage);
    }

    // Método para eliminar un BlockStorage de un Chunk específico.
    public void removeBlockStorageChunk(Chunk chunk){
        blockStorageChunks.remove(chunk);
    }

    // Método para obtener un BlockStorage de un Chunk específico.
    public BlockStorage getBlockStorageChunk(Chunk chunk){
        return blockStorageChunks.get(chunk);
    }

    // Método para comprobar si un Chunk específico contiene un BlockStorage.
    public boolean containsChunk(Chunk chunk){
        return blockStorageChunks.containsKey(chunk);
    }

    // Método para obtener todos los Chunks que contienen BlockStorages.
    public Set<Chunk> getChunks(){
        return blockStorageChunks.keySet();
    }

}
