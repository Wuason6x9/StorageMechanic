package dev.wuason.storagemechanic.storages.types.entity.skills;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageManager;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import org.bukkit.Bukkit;

public class SmLoadDefaultItemsMythicMechanic implements ITargetedEntitySkill {
    private StorageMechanic core;
    private String storageConfigId;
    public SmLoadDefaultItemsMythicMechanic(MythicLineConfig config, StorageMechanic core) {
        this.core = core;
    }
    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        StorageManager storageManager = core.getManagers().getStorageManager();
        if(!storageManager.storageExists(abstractEntity.getUniqueId().toString())) return SkillResult.ERROR;
        Storage storage = storageManager.getStorage(abstractEntity.getUniqueId().toString());
        Bukkit.getScheduler().runTaskAsynchronously(core,() -> storage.loadAllItemsDefault());

        return SkillResult.SUCCESS;
    }
}
