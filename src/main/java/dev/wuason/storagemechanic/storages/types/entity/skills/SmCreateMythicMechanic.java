package dev.wuason.storagemechanic.storages.types.entity.skills;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageOriginContext;
import dev.wuason.storagemechanic.storages.types.furnitures.FurnitureStorageManager;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythiccrucible.items.furniture.Furniture;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.UUID;

public class SmCreateMythicMechanic implements ITargetedEntitySkill {
    private StorageMechanic core;
    private String storageConfigId;
    public SmCreateMythicMechanic(MythicLineConfig config, StorageMechanic core) {
        storageConfigId = config.getString("id","a");
        this.core = core;
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {

        SkillCaster caster = skillMetadata.getCaster();
        String id = "";
        Location location = null;
        String idTriggerSkill = null;
        String type = null;

        if(FurnitureStorageManager.isMythicCrucibleLoaded()){
            if(skillMetadata.getCaster() instanceof Furniture) {
                location = BukkitAdapter.adapt(((Furniture) caster).getLocation());
                id = ((Furniture) caster).getEntity().getUniqueId().toString();
                idTriggerSkill = ((Furniture) caster).getFurnitureData().getItem().getInternalName();
                type = "FURNITURE";
            }
        }
        if(skillMetadata.getCaster() instanceof ActiveMob) {
            location = BukkitAdapter.adapt(((ActiveMob) caster).getLocation());
            id = ((ActiveMob) caster).getEntity().getUniqueId().toString();
            idTriggerSkill = ((ActiveMob) caster).getType().getInternalName();
            type = "MOB";
        }

        if(core.getManagers().getStorageManager().storageExists(id)) return SkillResult.SUCCESS;
        if(!core.getManagers().getStorageConfigManager().existsStorageConfig(storageConfigId)){
            return SkillResult.INVALID_CONFIG;
        }
        String finalType = type;
        Storage storage = core.getManagers().getStorageManager().createStorage(storageConfigId, UUID.fromString(id),new StorageOriginContext(StorageOriginContext.context.ENTITY_STORAGE, new ArrayList<>(){{
            add(finalType);
        }}));
        return SkillResult.SUCCESS;
    }

}
