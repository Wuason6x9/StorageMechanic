package dev.wuason.storagemechanic.storages.types.entity.skills;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.storages.Storage;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythiccrucible.items.furniture.Furniture;

public class SmCreateMythicMechanic implements ITargetedEntitySkill {
    private StorageMechanic core;
    private String storageConfigId;
    public SmCreateMythicMechanic(MythicLineConfig config, StorageMechanic core) {
        storageConfigId = config.getString("id","a");
        this.core = core;
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        if(core.getManagers().getStorageManager().storageExists(abstractEntity.getUniqueId().toString())) return SkillResult.SUCCESS;
        if(!core.getManagers().getStorageConfigManager().existsStorageConfig(storageConfigId)){
            return SkillResult.INVALID_CONFIG;
        }
        Storage storage = core.getManagers().getStorageManager().createStorage(storageConfigId, abstractEntity.getUniqueId());
        return SkillResult.SUCCESS;
    }

}
