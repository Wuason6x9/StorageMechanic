package dev.wuason.storagemechanic.storages.types.furnitures.compatibilities.mythic;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.storages.types.furnitures.FurnitureStorageManager;
import dev.wuason.storagemechanic.storages.types.furnitures.compatibilities.mythic.skills.EventCancel;
import dev.wuason.storagemechanic.storages.types.furnitures.compatibilities.mythic.skills.MythicCrucibleFurnitureBreak;
import dev.wuason.storagemechanic.storages.types.furnitures.compatibilities.mythic.skills.MythicCrucibleFurniturePlace;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.bukkit.utils.serialize.Locus;
import io.lumine.mythic.core.skills.SkillTriggers;
import io.lumine.mythiccrucible.MythicCrucible;
import io.lumine.mythiccrucible.items.furniture.Furniture;
import io.lumine.mythiccrucible.items.furniture.FurnitureDataKeys;
import io.lumine.mythiccrucible.items.furniture.FurnitureItemContext;
import io.lumine.mythiccrucible.items.furniture.FurnitureManager;
import io.lumine.mythiccrucible.utils.CustomBlockData;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class MythicCrucibleFurnitureManager implements Listener {
    private String[] VERSIONS_NOT_DISPLAY_ENTITY = {"1.18","1.18.1","1.18.2","1.19","1.19.1","1.19.2","1.19.3"};

    private final String ADAPTER_TYPE = "mythiccrucible:";
    private FurnitureStorageManager furnitureStorageManager;
    private StorageMechanic core;

    public MythicCrucibleFurnitureManager(FurnitureStorageManager furnitureStorageManager, StorageMechanic core) {
        this.furnitureStorageManager = furnitureStorageManager;
        this.core = core;
        loadEvents();
    }
    public void loadEvents(){
        if(checkVersionDisplayEntity()){
            Bukkit.getPluginManager().registerEvents(new MythicCrucibleFurnitureDisplayEntityEvent(this),core);
        }


    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onFurnitureBreakBarrier(BlockBreakEvent event) {
        Block block = event.getBlock();
        CustomBlockData blockData = new CustomBlockData(block, MythicCrucible.inst());
        FurnitureManager furnitureManager = MythicCrucible.inst().getItemManager().getFurnitureManager();
        furnitureManager.getFurnitureData(blockData).ifPresent((furniture) -> {
            String baseLocationData = (String)blockData.get(FurnitureDataKeys.FURNITURE_BASE, PersistentDataType.STRING);
            Locus baseLocation = Locus.deserialize(baseLocationData);
            furnitureManager.getFurnitureFrame(baseLocation.toLocation(block.getWorld())).ifPresent((itemFrame) -> {
                event.setCancelled(true);
                furniture.remove(new Furniture(furniture, itemFrame), (Entity)event.getPlayer());
            });
        });
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerEntityInteract(PlayerInteractEntityEvent event){
        Entity entity = event.getRightClicked();
        if(entity.getType().equals(EntityType.ITEM_FRAME)){
            ItemFrame itemFrame = (ItemFrame) entity;
            MythicCrucible.inst().getItemManager().getFurnitureManager().getFurnitureData(itemFrame).ifPresent((furniture) -> {
                onMythicFurnitureInteract(furniture,event.getPlayer(),entity,event.getPlayer().getInventory().getItemInMainHand(),event);
            });
        }
    }
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteractBlock(PlayerInteractEvent event){
        if(event.getHand() == null || !event.getHand().equals(EquipmentSlot.HAND) || !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        Block block = event.getClickedBlock();
        MythicCrucible.inst().getItemManager().getFurnitureManager().getFurniture(block).ifPresent((f) -> {
            onMythicFurnitureInteract(f.getFurnitureData(),event.getPlayer(),f.getEntity().getBukkitEntity(),event.getPlayer().getInventory().getItemInMainHand(),event);
        });
    }

    @EventHandler
    public void onMythicMechanicLoad(MythicMechanicLoadEvent event){

        if(event.getMechanicName().equalsIgnoreCase(Skills.smFurniture.toString())){

            if(event.getContainer().getTrigger().equals(SkillTriggers.BLOCK_BREAK)){

                event.register(new MythicCrucibleFurnitureBreak(this));

            }

            if(event.getContainer().getTrigger().equals(SkillTriggers.BLOCK_PLACE)){

                event.register(new MythicCrucibleFurniturePlace(this));

            }

        }

    }


    public void onMythicFurnitureInteract(FurnitureItemContext furnitureItemContext, Player player, Entity entity, ItemStack itemHand, Cancellable cancellable){
        String adapterId = ADAPTER_TYPE + furnitureItemContext.getItem().getInternalName();
        EventCancel eventCancel = new EventCancel();
        furnitureStorageManager.onFurnitureInteract(adapterId, player,entity,itemHand,eventCancel);
        if(eventCancel.isCancelled()){
            cancellable.setCancelled(true);
        }
    }
    public void onMythicFurniturePlace(FurnitureItemContext furnitureItemContext, Player player, Entity entity, ItemStack itemHand, EventCancel cancellable){
        String adapterId = ADAPTER_TYPE + furnitureItemContext.getItem().getInternalName();
        EventCancel eventCancel = new EventCancel();
        furnitureStorageManager.onFurniturePlace(adapterId, player,entity,itemHand,eventCancel);
        if(eventCancel.isCancelled()){
            cancellable.setCancelled(true);
        }
    }
    public void onMythicFurnitureBreak(FurnitureItemContext furnitureItemContext, Player player, Entity entity, ItemStack itemHand, EventCancel cancellable){
        String adapterId = ADAPTER_TYPE + furnitureItemContext.getItem().getInternalName();
        EventCancel eventCancel = new EventCancel();
        furnitureStorageManager.onFurnitureBreak(adapterId, player,entity,itemHand,eventCancel);
        if(eventCancel.isCancelled()){
            cancellable.setCancelled(true);
        }
    }


    public boolean checkVersionDisplayEntity(){
        String actualVersion = Bukkit.getBukkitVersion();
        for(String v : VERSIONS_NOT_DISPLAY_ENTITY){if(actualVersion.contains(v)) return false;}
        return true;
    }

    public enum Skills{
        smFurniture

    }

}
