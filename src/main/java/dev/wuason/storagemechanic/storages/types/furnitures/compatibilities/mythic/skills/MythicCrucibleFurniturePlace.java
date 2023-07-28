package dev.wuason.storagemechanic.storages.types.furnitures.compatibilities.mythic.skills;

import dev.wuason.storagemechanic.storages.types.furnitures.compatibilities.mythic.MythicCrucibleFurnitureManager;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.utils.serialize.BlockLocus;
import io.lumine.mythiccrucible.MythicCrucible;
import io.lumine.mythiccrucible.items.furniture.Furniture;
import io.lumine.mythiccrucible.items.furniture.FurnitureDataKeys;
import io.lumine.mythiccrucible.items.furniture.FurnitureItemContext;
import io.lumine.mythiccrucible.items.furniture.FurnitureManager;
import io.lumine.mythiccrucible.utils.CustomBlockData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MythicCrucibleFurniturePlace implements INoTargetSkill {
    MythicCrucibleFurnitureManager mcFurnitureManager;

    public MythicCrucibleFurniturePlace(MythicCrucibleFurnitureManager mcFurnitureManager) {
        this.mcFurnitureManager = mcFurnitureManager;
    }
    @Override
    public SkillResult cast(SkillMetadata skillMetadata) {
        if(skillMetadata.getCaster() instanceof Furniture){
            Furniture furniture = (Furniture) skillMetadata.getCaster();
            Player player = (Player) skillMetadata.getTrigger().getBukkitEntity();
            EventCancel eventCancel = new EventCancel();
            Entity entity = furniture.getEntity().getBukkitEntity();
            mcFurnitureManager.onMythicFurniturePlace(furniture.getFurnitureData(),player,entity,player.getInventory().getItemInMainHand(),eventCancel);
            if(eventCancel.isCancelled()){
                removeFurniture(furniture);
            }
            return SkillResult.SUCCESS;
        }
        return SkillResult.ERROR;
    }

    public void removeFurniture(Furniture furniture){
        FurnitureManager furnitureManager = MythicCrucible.inst().getItemManager().getFurnitureManager();
        furnitureManager.unloadFurniture(furniture); //REMOVE FURNITURE OF REGISTER
        Entity entity = furniture.getEntity().getBukkitEntity();
        World world = entity.getWorld();
        Map<BlockLocus, Integer> lightOffsets = getLightOffsets(furniture.getFurnitureData());
        if(!furniture.getFurnitureData().getBarrierOffsets().isEmpty() || !lightOffsets.isEmpty()){
            BlockLocus baseLocus = BlockLocus.of(entity.getLocation().getBlock());
            float yaw = (Float)entity.getPersistentDataContainer().get(FurnitureDataKeys.FURNITURE_ORIENTATION, PersistentDataType.FLOAT);
            for(BlockLocus blockLocus : furniture.getFurnitureData().getBarrierOffsets()){
                Location barrierLocation = blockLocus.add(baseLocus.getX(), baseLocus.getY(), baseLocus.getZ()).transform2D((double)yaw, baseLocus.getX(), baseLocus.getZ(), 0, 0).toLocation(world);
                CustomBlockData data = new CustomBlockData(barrierLocation.getBlock(), MythicCrucible.inst());
                data.clear();
                barrierLocation.getBlock().setType(Material.AIR);
            }
            for(Map.Entry<BlockLocus, Integer> entry : lightOffsets.entrySet()){
                BlockLocus locus = entry.getKey();
                Location lightLocation = locus.add(baseLocus.getX(), baseLocus.getY(), baseLocus.getZ()).transform2D((double)yaw, baseLocus.getX(), baseLocus.getZ(), 0, 0).toLocation(world);
                CustomBlockData data = new CustomBlockData(lightLocation.getBlock(), MythicCrucible.inst());
                data.clear();
                lightLocation.getBlock().setType(Material.AIR);
            }
        }
        //SEATS
        for(Entity e : furniture.getFurnitureData().getSeats(entity)){
            e.remove();
        }
        //HITBOX
        Optional<Entity> maybeHitbox = furniture.getFurnitureData().getHitbox(entity);
        if (maybeHitbox.isPresent()) {
            ((Entity)maybeHitbox.get()).remove();
        }
        CustomBlockData data = new CustomBlockData(entity.getLocation().getBlock(), MythicCrucible.inst());
        data.clear();
        entity.remove();
    }



    public Map<BlockLocus, Integer> getLightOffsets(FurnitureItemContext f){
        Field field = null;
        try {
            field = f.getClass().getDeclaredField("lightOffsets");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        field.setAccessible(true);
        Map<BlockLocus, Integer> map = null;
        try {
            map = (Map<BlockLocus, Integer>) field.get(f);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return map;
    }
}
