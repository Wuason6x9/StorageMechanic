package dev.wuason.storagemechanic.storages.types.entity.skills;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.compatibilities.Compatibilities;
import dev.wuason.storagemechanic.data.SaveCause;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageManager;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythiccrucible.items.furniture.Furniture;

public class SmSaveMythicMechanic implements ITargetedEntitySkill {
    private StorageMechanic core;
    private String storageConfigId;

    public SmSaveMythicMechanic(MythicLineConfig config, StorageMechanic core) {
        this.core = core;
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        SkillCaster caster = skillMetadata.getCaster();
        String id = "";
        if (Compatibilities.isMythicCrucibleLoaded()) {
            if (caster instanceof Furniture) {
                id = ((Furniture) caster).getEntity().getUniqueId().toString();
            }
        }
        if (caster instanceof ActiveMob) {
            id = ((ActiveMob) caster).getEntity().getUniqueId().toString();
        }
        if (id == "") return SkillResult.SUCCESS;
        StorageManager storageManager = core.getManagers().getStorageManager();
        if (!storageManager.storageExists(id)) return SkillResult.ERROR;
        Storage storage = storageManager.getStorage(id);
        storageManager.saveStorage(storage, SaveCause.NORMAL_SAVE);
        return SkillResult.SUCCESS;
    }
}
