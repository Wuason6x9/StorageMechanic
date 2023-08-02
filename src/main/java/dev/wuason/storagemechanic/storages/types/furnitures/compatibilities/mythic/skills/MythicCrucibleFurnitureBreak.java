package dev.wuason.storagemechanic.storages.types.furnitures.compatibilities.mythic.skills;

import dev.wuason.storagemechanic.storages.types.furnitures.compatibilities.mythic.MythicCrucibleFurnitureManager;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythiccrucible.items.furniture.Furniture;
import io.lumine.mythiccrucible.items.furniture.FurnitureDataKeys;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class MythicCrucibleFurnitureBreak implements INoTargetSkill {
    MythicCrucibleFurnitureManager mcFurnitureManager;

    public MythicCrucibleFurnitureBreak(MythicCrucibleFurnitureManager mcFurnitureManager) {
        this.mcFurnitureManager = mcFurnitureManager;
    }

    @Override
    public SkillResult cast(SkillMetadata skillMetadata) {
        if(skillMetadata.getTrigger() == null) return SkillResult.ERROR;
        if(skillMetadata.getCaster() instanceof Furniture){
            Furniture furniture = (Furniture) skillMetadata.getCaster();
            Player player = (Player) skillMetadata.getTrigger().getBukkitEntity();
            EventCancel eventCancel = new EventCancel();
            Entity entity = furniture.getEntity().getBukkitEntity();
            mcFurnitureManager.onMythicFurnitureBreak(furniture.getFurnitureData(),player,entity,player.getInventory().getItemInMainHand(),eventCancel);
            if(eventCancel.isCancelled()){
                setHealth(furniture.getFurnitureData().getHealth(),entity);
                skillMetadata.cancelEvent();
            }
            return SkillResult.SUCCESS;
        }

        return SkillResult.ERROR;
    }

    public void setHealth(int health, Entity entity){
        PersistentDataContainer data = entity.getPersistentDataContainer();
        data.set(FurnitureDataKeys.FURNITURE_HEALTH, PersistentDataType.INTEGER, health);
    }
}