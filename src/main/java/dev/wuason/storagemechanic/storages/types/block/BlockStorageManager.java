package dev.wuason.storagemechanic.storages.types.block;

import dev.wuason.mechanics.compatibilities.adapter.Adapter;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.libs.protectionlib.ProtectionLib;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.data.DataManager;
import dev.wuason.storagemechanic.data.SaveCause;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageOriginContext;
import dev.wuason.storagemechanic.storages.types.block.compatibilities.ItemsAdderEvents;
import dev.wuason.storagemechanic.storages.types.block.compatibilities.OraxenEvents;
import dev.wuason.storagemechanic.storages.types.block.compatibilities.mythic.MythicCrucibleBlockEvents;
import dev.wuason.storagemechanic.storages.types.block.config.BlockStorageConfig;
import dev.wuason.storagemechanic.storages.types.block.mechanics.BlockMechanicManager;
import dev.wuason.storagemechanic.storages.types.block.mechanics.integrated.hopper.HopperBlockMechanic;
import dev.wuason.storagemechanic.utils.StorageUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BlockStorageManager implements Listener {
    private BlockMechanicManager blockMechanicManager;
    private StorageMechanic core;

    private volatile HashMap<String,BlockStorage> blockStorages = new HashMap<>();
    private DataManager dataManager;
    private BukkitTask taskSaves = null;
    private ArrayList<BlockStorage> blockStoragesToSave = new ArrayList<>();
    private final NamespacedKey BLOCK_SHULKER_NAMESPACEDKEY = new NamespacedKey(StorageMechanic.getInstance(),"blockstorageshulker");

    public BlockStorageManager(StorageMechanic core, DataManager dataManager, BlockMechanicManager blockMechanicManager) {
        this.core = core;
        this.blockMechanicManager = blockMechanicManager;
        this.dataManager = dataManager;
        loadEvents();
    }
    public void loadEvents(){

        if(Bukkit.getPluginManager().getPlugin("ItemsAdder") != null){
            ItemsAdderEvents itemsAdderEvents = new ItemsAdderEvents(this);
            Bukkit.getPluginManager().registerEvents(itemsAdderEvents,core);
        }
        if(Bukkit.getPluginManager().getPlugin("MythicCrucible") != null){
            MythicCrucibleBlockEvents mythicCrucibleBlockEvents = new MythicCrucibleBlockEvents(this);
            Bukkit.getPluginManager().registerEvents(mythicCrucibleBlockEvents,core);
        }
        if(Bukkit.getPluginManager().getPlugin("Oraxen") != null){
            OraxenEvents oraxenEvents = new OraxenEvents(this);
            Bukkit.getPluginManager().registerEvents(oraxenEvents,core);
        }

    }

    public BlockStorage createBlockStorage(String blockStorageConfigID, Location blockLocation, Player player,String id){
        if(!blockStorages.containsKey(StorageUtils.getStoragePhysicalId(blockLocation)) && core.getManagers().getBlockStorageConfigManager().blockStorageConfigExists(blockStorageConfigID)){
            BlockStorageConfig blockStorageConfig = core.getManagers().getBlockStorageConfigManager().findBlockStorageConfigById(blockStorageConfigID).orElse(null);

            Storage storage = core.getManagers().getStorageManager().createStorage(blockStorageConfig.getStorageConfigID(),new StorageOriginContext(StorageOriginContext.Context.BLOCK_STORAGE, new ArrayList<>(){{
                add(blockStorageConfigID);
                add(id);
                add(player.getUniqueId().toString());
            }}));
            HashMap<String,Storage> hashMap = new HashMap<>();
            hashMap.put(player.getUniqueId().toString(),storage);
            ArrayList<Location> locations = new ArrayList<>();
            locations.add(blockLocation);
            BlockStorage blockStorage = new BlockStorage(id,blockStorageConfigID,hashMap,player.getUniqueId(),locations);

            blockStorages.put(blockStorage.getId(),blockStorage);

            return blockStorage;

        }

        return null;
    }
    public BlockStorage createBlockStorage(String blockStorageConfigID, ArrayList<Location> locations, Player player,String id){

        if(!blockStorages.containsKey(id) && core.getManagers().getBlockStorageConfigManager().blockStorageConfigExists(blockStorageConfigID)){

            BlockStorageConfig blockStorageConfig = core.getManagers().getBlockStorageConfigManager().findBlockStorageConfigById(blockStorageConfigID).orElse(null);

            HashMap<String,Storage> hashMap = new HashMap<>();
            BlockStorage blockStorage = new BlockStorage(id,blockStorageConfigID,hashMap,player.getUniqueId(),locations);

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

            BlockStorageConfig blockStorageConfig = core.getManagers().getBlockStorageConfigManager().findBlockStorageConfigById(blockStorageConfigID).orElse(null);

            Storage storage = core.getManagers().getStorageManager().getStorage(storageID);
            HashMap<String,Storage> hashMap = new HashMap<>();
            hashMap.put(player.getUniqueId().toString(),storage);
            BlockStorage blockStorage = new BlockStorage(id,blockStorageConfigID,hashMap,player.getUniqueId(),locations);

            blockStorages.put(blockStorage.getId(),blockStorage);

            return blockStorage;

        }

        return null;
    }

    //ONUNLOADCHUNKS
    @EventHandler
    public void BlockStorageUnLoadChunk(ChunkUnloadEvent event){

        PersistentDataContainer persistentDataContainer = event.getChunk().getPersistentDataContainer();

        for(NamespacedKey namespacedKey : persistentDataContainer.getKeys()){

            if(namespacedKey.getKey().contains("blockstorage_") || namespacedKey.getKey().contains("blockstorageshulker_")){

                String[] blockStorageData = persistentDataContainer.get(namespacedKey,PersistentDataType.STRING).split(":");

                BlockStorage blockStorage = getBlockStorage(blockStorageData[0]);

                if(blockStorage != null){

                    TaskSave(blockStorage);

                }

            }

        }

    }
    public void TaskSave(BlockStorage blockStorage){
        blockStoragesToSave.add(blockStorage);
        TaskStart();
    }
    public void TaskStart(){
        if(taskSaves == null || taskSaves.isCancelled()){
            taskSaves = Bukkit.getScheduler().runTaskAsynchronously(core,() -> {
                while(!blockStoragesToSave.isEmpty()){
                    BlockStorage blockStorage = blockStoragesToSave.get(0);
                    saveBlockStorage(blockStorage,SaveCause.NORMAL_SAVE);
                    blockStoragesToSave.remove(blockStorage);
                }

                taskSaves.cancel();
            });
        }
    }

    //ONPLACE
    @EventHandler(priority = EventPriority.NORMAL)
    public void BlockStoragePlaceEvent(BlockPlaceEvent event){
        if(event.getHand() == EquipmentSlot.HAND && event.getPlayer() != null && event.canBuild()){
            PersistentDataContainer persistentDataContainerItem = event.getItemInHand().getItemMeta().getPersistentDataContainer();
            if(persistentDataContainerItem.has(new NamespacedKey(core,"blockStorageShulker"), PersistentDataType.STRING)){

                core.getManagers().getBlockStorageConfigManager().findBlockStorageConfigById(persistentDataContainerItem.get(new NamespacedKey(core,"blockStorageShulker"),PersistentDataType.STRING).split(":")[1]).ifPresent(blockConfig -> {

                    if(!blockConfig.getBlock().contains("ia")){
                        if(StorageUtils.canNotBuild(event.getPlayer(), event.getBlockPlaced())) {
                            event.setCancelled(true);
                            return;
                        };
                        onBlockPlace(event.getBlockPlaced(), event.getPlayer(), event.getItemInHand());
                    }

                });

            }
        }
    }




    public void onBlockPlace(Block block, Player player,ItemStack itemHand){
        if(itemHand.getItemMeta() == null) return;
        PersistentDataContainer persistentDataContainerItem = itemHand.getItemMeta().getPersistentDataContainer();
        if(persistentDataContainerItem.has(new NamespacedKey(core,"blockStorageShulker"), PersistentDataType.STRING)){

            if(itemHand != null) player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

            String[] data = persistentDataContainerItem.get(new NamespacedKey(core,"blockStorageShulker"),PersistentDataType.STRING).split(":");

            String blockStorageID = data[0];
            String blockStorageConfigID = data[1];
            String ownerUUID = data[2];
            //NameSpace
            PersistentDataContainer persistentDataContainerBlock = block.getChunk().getPersistentDataContainer();
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();
            NamespacedKey namespacedKey = new NamespacedKey(core,"BlockStorageShulker_" + x + "_" + y + "_" + z);

            persistentDataContainerBlock.set(namespacedKey,PersistentDataType.STRING, blockStorageID + ":" + blockStorageConfigID + ":" + ownerUUID);
            //set the new location
            BlockStorage blockStorage = getBlockStorage(blockStorageID);
            blockStorage.addLocation(block.getLocation());

            //others
            BlockStorageConfig blockStorageConfig = core.getManagers().getBlockStorageConfigManager().findBlockStorageConfigById(blockStorageConfigID).get();
            HopperBlockMechanic hopperBlockMechanic = (HopperBlockMechanic) core.getManagers().getBlockMechanicManager().getMechanic(HopperBlockMechanic.HOPPER_MECHANIC_KEY);
            hopperBlockMechanic.onBlockStoragePlace(block,player,blockStorage,blockStorageConfig);
        }

    }



    //ONBREAK

    @EventHandler(priority = EventPriority.HIGHEST)
    public void BlockBreakEvent(BlockBreakEvent event){ //AHORA

        if(event.getBlock() != null){
            if(isBlockStorageByBlock(event.getBlock())){
                Block block = event.getBlock();
                BlockStorage blockStorage = getBlockStorageByBlock(event.getBlock());
                BlockStorageConfig blockStorageConfig = blockStorage.getBlockStorageConfig();

                switch (blockStorageConfig.getBlockStorageType()){
                    case ENDER_CHEST -> {
                        //Eliminar localizacion
                        blockStorage.removeLocation(block.getLocation());
                        //ELIMINAR DE MEMORIA Y DE PERSISTENCIA
                        removeBlockStoragePersistence(block);

                    }
                    case SHULKER -> { //GUARDAR EN DATA ✅
                        removeBlockStoragePersistence(block);
                        blockStorage.removeLocation(block.getLocation());
                        ItemStack item = Adapter.getInstance().getItemStack(blockStorageConfig.getBlock());
                        ItemMeta itemMeta = item.getItemMeta();
                        itemMeta.getPersistentDataContainer().set(new NamespacedKey(core,"blockStorageShulker"),PersistentDataType.STRING, blockStorage.getId() + ":" + blockStorageConfig.getId() + ":" + blockStorage.getOwnerUUID());
                        item.setItemMeta(itemMeta);
                        block.getWorld().dropItem(block.getLocation(),item);
                        blockStorage.delete();
                        saveBlockStorage(blockStorage,SaveCause.NORMAL_SAVE);
                    }
                    default -> {
                        if(!blockStorageConfig.getBlockStorageProperties().isBreakable()){
                            boolean allEmpty = true;
                            for(Storage storage : blockStorage.getStorages().values()){
                                if(!storage.isEmpty()){
                                    allEmpty = false;
                                }
                            }
                            if(!allEmpty){
                                AdventureUtils.playerMessage(core.getManagers().getConfigManager().getLangDocumentYaml().getString("messages.type.blockstorage.isBreakeable"), event.getPlayer());
                                event.setCancelled(true);
                                return;
                            }
                        }

                        for(Storage storage : blockStorage.getStorages().values()){
                            storage.dropAllItems(block.getLocation());
                            storage.closeAllInventory();
                            core.getManagers().getStorageManager().removeStorage(storage.getId());
                        }
                        blockStorage.getStorages().clear();
                        //ELIMINAR DE MEMORIA Y DE PERSISTENCIA
                        removeBlockStoragePersistence(block);
                        removeBlockStorage(blockStorage.getId());
                        blockStorage.delete();
                    }
                }
            }
        }
    }


    //-------ONINTERACT--------

    @EventHandler(priority = EventPriority.HIGHEST)
    public void BlockInteractEvent(PlayerInteractEvent event){
        if(event.getPlayer().isSneaking()) return;
        if(event.getHand() == null || !event.getHand().equals(EquipmentSlot.HAND)) return;
        if(event.getAction().toString().contains("AIR") || event.getAction().equals(Action.PHYSICAL)) return;
        String adapterID = Adapter.getInstance().getAdapterID(event.getClickedBlock());
        if(adapterID.contains("or:") && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        onBlockInteract(event.getClickedBlock(),event.getItem(),event.getPlayer(),event,event.getAction(),adapterID);
    }

    public void onBlockInteract(Block block, ItemStack itemHand, Player player, Cancellable cancellable, Action action, String adapterID){
        if(!player.isSneaking()){
            if(!ProtectionLib.canInteract(player,block.getLocation())) return;
            if(block != null && player != null){

                PersistentDataContainer persistentDataContainer = block.getChunk().getPersistentDataContainer();
                int x = block.getX();
                int y = block.getY();
                int z = block.getZ();
                NamespacedKey namespacedKey = new NamespacedKey(core,"BlockStorage_" + x + "_" + y + "_" + z);

                String[] blockStorageData = new String[3];
                if(persistentDataContainer.has(namespacedKey,PersistentDataType.STRING)){
                    blockStorageData = persistentDataContainer.get(namespacedKey,PersistentDataType.STRING).split(":");
                }
                //SI EXISTE EL STORAGE
                if(blockStorageData[0] != null){
                    if(blockStorageExists(blockStorageData[0])){
                        BlockStorageConfig blockStorageConfig = core.getManagers().getBlockStorageConfigManager().findBlockStorageConfigByItemID(adapterID);
                        if(action == blockStorageConfig.getBlockStorageClickType().getAction()){
                            BlockStorage blockStorage = getBlockStorage(blockStorageData[0]);
                            cancellable.setCancelled(true);
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
                            cancellable.setCancelled(true);
                            BlockStorage blockStorage = null;
                            switch (blockStorageConfig.getBlockStorageType()){
                                case SHULKER -> {
                                    NamespacedKey namespacedShulker = new NamespacedKey(core,"BlockStorageShulker_" + x + "_" + y + "_" + z);
                                    if(persistentDataContainer.has(namespacedShulker,PersistentDataType.STRING)){
                                        String[] blockStorageShulkerData = persistentDataContainer.get(namespacedShulker,PersistentDataType.STRING).split(":");
                                        blockStorage = getBlockStorage(blockStorageShulkerData[0]);
                                        persistentDataContainer.set(namespacedKey,PersistentDataType.STRING, blockStorageShulkerData[0] + ":" + blockStorageShulkerData[1] + ":" + blockStorageShulkerData[2]);
                                        persistentDataContainer.remove(namespacedShulker);
                                    }
                                    else {
                                        blockStorage = createBlockStorage(blockStorageConfig.getId(),block.getLocation(),player,StorageUtils.getStoragePhysicalId(block.getLocation()));
                                        persistentDataContainer.set(namespacedKey,PersistentDataType.STRING, blockStorage.getId() + ":" + blockStorage.getBlockStorageConfigID() + ":" + blockStorage.getOwnerUUID());
                                    }
                                }
                                case ENDER_CHEST -> {
                                    blockStorage = getBlockStorage(blockStorageConfig.getId());
                                    ArrayList<Location> locations = new ArrayList<>();
                                    if(blockStorage == null){
                                        blockStorage = createBlockStorage(blockStorageConfig.getId(),locations,player,blockStorageConfig.getId());
                                    }
                                    locations = blockStorage.getLocs();
                                    locations.add(new Location(block.getWorld(),block.getLocation().getX(),block.getY(),block.getZ()));
                                    blockStorage.createStoragePlayer(player);
                                    persistentDataContainer.set(namespacedKey,PersistentDataType.STRING, blockStorage.getId() + ":" + blockStorage.getBlockStorageConfigID() + ":" + blockStorage.getOwnerUUID());
                                }
                                default -> {
                                    blockStorage = createBlockStorage(blockStorageConfig.getId(),block.getLocation(),player,StorageUtils.getStoragePhysicalId(block.getLocation()));
                                    persistentDataContainer.set(namespacedKey,PersistentDataType.STRING, blockStorage.getId() + ":" + blockStorage.getBlockStorageConfigID() + ":" + blockStorage.getOwnerUUID());
                                }
                            }
                            if(adapterID.contains("or:") || adapterID.contains("mc:") || adapterID.contains("sm:") || adapterID.contains("mmoitems:") || adapterID.contains("eb:") || adapterID.contains("mythiccrucible:")){
                                openBlockStorage(blockStorage.getId(), player);
                            }
                        }
                    }
                }
            }
        }

    }

    public NamespacedKey getNamespacedKey(Block block){
        return new NamespacedKey(core,"BlockStorage_" + block.getX() + "_" + block.getY() + "_" + block.getZ());
    }
    public boolean removeBlockStoragePersistence(Block block){
        if(block.getChunk().getPersistentDataContainer().has(new NamespacedKey(core,"BlockStorage_" + block.getX() + "_" + block.getY() + "_" + block.getZ()),PersistentDataType.STRING)){
            block.getChunk().getPersistentDataContainer().remove(new NamespacedKey(core,"BlockStorage_" + block.getX() + "_" + block.getY() + "_" + block.getZ()));
            return true;
        }
        return false;
    }
    public boolean removeBlockStoragePersistence(PersistentDataContainer persistentDataContainer,int x, int y, int z){
        if(persistentDataContainer.has(new NamespacedKey(core,"BlockStorage_" + x + "_" + y + "_" + z),PersistentDataType.STRING)){
            persistentDataContainer.remove(new NamespacedKey(core,"BlockStorage_" + x + "_" + y + "_" + z));
            return true;
        }
        return false;
    }
    public boolean removeBlockStoragePersistenceShulker(PersistentDataContainer persistentDataContainer,int x, int y, int z){
        if(persistentDataContainer.has(new NamespacedKey(core,"BlockStorageShulker_" + x + "_" + y + "_" + z),PersistentDataType.STRING)){
            persistentDataContainer.remove(new NamespacedKey(core,"BlockStorageShulker_" + x + "_" + y + "_" + z));
            return true;
        }
        return false;
    }
    public void removeAllBlockStorageChunk(Chunk chunk){
        for(NamespacedKey namespacedKey : chunk.getPersistentDataContainer().getKeys()){
            if(namespacedKey.getKey().contains("BlockStorage")){
                chunk.getPersistentDataContainer().remove(namespacedKey);
            }
        }
    }


    //BASICS METHODS
    public void removeBlockStorage(String blockStorageId) {
        if(blockStorages.containsKey(blockStorageId)) blockStorages.remove(blockStorageId);
        if(dataManager.getStorageManagerData().getBlockStorageManagerData().existBlockStorageData(blockStorageId)) dataManager.getStorageManagerData().getBlockStorageManagerData().removeBlockStorageData(blockStorageId);
    }

    // Método para buscar un BlockStorage por su ID
    public BlockStorage getBlockStorage(String blockStorageId) {
        if(blockStorages.containsKey(blockStorageId)) return blockStorages.get(blockStorageId);
        if(dataManager.getStorageManagerData().getBlockStorageManagerData().existBlockStorageData(blockStorageId)) return loadBlockStorage(blockStorageId);
        return null;
    }

    // Método para verificar si existe un BlockStorage por su ID
    public boolean blockStorageExists(String blockStorageId) {
        if(blockStorages.containsKey(blockStorageId)) return true;
        if(dataManager.getStorageManagerData().getBlockStorageManagerData().existBlockStorageData(blockStorageId)) return true;
        return false ;
    }

    //methods Data
    public void saveBlockStorage(BlockStorage blockStorage, SaveCause saveCause){
        String id = blockStorage.getId();
        if(blockStorages.containsKey(id)) blockStorages.remove(id);
        for(Storage storage : blockStorage.getStorages().values()){
            core.getManagers().getStorageManager().saveStorage(storage,saveCause);
        }
        dataManager.getStorageManagerData().getBlockStorageManagerData().saveBlockStorage(blockStorage);
    }
    public void saveBlockStorageNoRemove(BlockStorage blockStorage){
        for(Storage storage : blockStorage.getStorages().values()){
            core.getManagers().getStorageManager().saveStorageNoRemove(storage);
        }
        dataManager.getStorageManagerData().getBlockStorageManagerData().saveBlockStorage(blockStorage);
    }
    public BlockStorage loadBlockStorage(String id){
        if(!blockStorages.containsKey(id)){
            BlockStorage blockStorage = dataManager.getStorageManagerData().getBlockStorageManagerData().loadBlockStorageData(id);
            if(blockStorage == null) return null;
            blockStorages.put(blockStorage.getId(),blockStorage);
            return blockStorage;
        }
        return null;
    }

    public boolean isShulker(ItemStack itemStack){
        return itemStack.getItemMeta().getPersistentDataContainer().has(BLOCK_SHULKER_NAMESPACEDKEY, PersistentDataType.STRING);
    }
    public String getShulkerData(ItemStack itemStack){
        return itemStack.getItemMeta().getPersistentDataContainer().get(BLOCK_SHULKER_NAMESPACEDKEY,PersistentDataType.STRING);
    }

    public void saveAllBlockStorages(){
        for(Map.Entry<String,BlockStorage> blockStorageMap : blockStorages.entrySet()){
            saveBlockStorageNoRemove(blockStorageMap.getValue());
        }
    }
    public void stop(){

        while(!blockStorages.values().isEmpty()){
            BlockStorage blockStorage = (BlockStorage) (blockStorages.values().toArray())[0];

            saveBlockStorage(blockStorage,SaveCause.STOPPING_SAVE);
        }

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

    public boolean isBlockStorageByBlock(Block block){
        if(block.getChunk().getPersistentDataContainer().has(new NamespacedKey(core,"BlockStorage_" + block.getX() + "_" + block.getY() + "_" + block.getZ()),PersistentDataType.STRING)) return true;
        if(block.getChunk().getPersistentDataContainer().has(new NamespacedKey(core,"BlockStorageShulker_" + block.getX() + "_" + block.getY() + "_" + block.getZ()),PersistentDataType.STRING)) return true;
        return false;
    }
    public String getBlockStorageIDByBlock(Block block){
        if(block.getChunk().getPersistentDataContainer().has(new NamespacedKey(core,"BlockStorage_" + block.getX() + "_" + block.getY() + "_" + block.getZ()),PersistentDataType.STRING)) return block.getChunk().getPersistentDataContainer().get(new NamespacedKey(core,"BlockStorage_" + block.getX() + "_" + block.getY() + "_" + block.getZ()),PersistentDataType.STRING).split(":")[0];
        if(block.getChunk().getPersistentDataContainer().has(new NamespacedKey(core,"BlockStorageShulker_" + block.getX() + "_" + block.getY() + "_" + block.getZ()),PersistentDataType.STRING)) return block.getChunk().getPersistentDataContainer().get(new NamespacedKey(core,"BlockStorageShulker_" + block.getX() + "_" + block.getY() + "_" + block.getZ()),PersistentDataType.STRING).split(":")[0];
        return null;
    }

    public BlockStorage getBlockStorageByBlock(Block block){
        return getBlockStorage(getBlockStorageIDByBlock(block));
    }
    public List<NamespacedKey> getAllBlockStorages(Chunk chunk){
        List<NamespacedKey> list = new ArrayList<>();
        for(NamespacedKey namespacedKey : chunk.getPersistentDataContainer().getKeys()){
            if(namespacedKey.getKey().contains("blockstorage_") || namespacedKey.getKey().contains("blockstorageshulker_")){
                list.add(namespacedKey);
            }
        }
        return list;
    }
    public Block getBlockByNameSpace(NamespacedKey namespacedKey, Chunk chunk){
        String[] data = namespacedKey.getKey().split("_");
        return new Location(chunk.getWorld(), Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3])).getBlock();
    }

    public BlockMechanicManager getBlockMechanicManager() {
        return blockMechanicManager;
    }

    public BlockStorage getBlockStorageByNameSpace(NamespacedKey namespacedKey, Chunk chunk){
        if(chunk.getPersistentDataContainer().has(namespacedKey, PersistentDataType.STRING)) return getBlockStorage(chunk.getPersistentDataContainer().get(namespacedKey ,PersistentDataType.STRING).split(":")[0] );
        return null;
    }
    public BlockStorageConfig getBlockStorageConfigByNameSpace(NamespacedKey namespacedKey, Chunk chunk){
        if(chunk.getPersistentDataContainer().has(namespacedKey, PersistentDataType.STRING)) return core.getManagers().getBlockStorageConfigManager().getBlockStorageConfig(chunk.getPersistentDataContainer().get(namespacedKey ,PersistentDataType.STRING).split(":")[1] );
        return null;
    }

}
