package dev.wuason.storagemechanic.storages.types.entity.skills;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageManager;
import dev.wuason.storagemechanic.storages.types.furnitures.FurnitureStorageManager;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythiccrucible.items.furniture.Furniture;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SmLoadDefaultItemsMythicMechanic implements ITargetedEntitySkill {
    private StorageMechanic core;
    private String storageConfigId;
    public SmLoadDefaultItemsMythicMechanic(MythicLineConfig config, StorageMechanic core) {
        this.core = core;
    }
    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        SkillCaster caster = skillMetadata.getCaster();
        String id = "";
        if(FurnitureStorageManager.isMythicCrucibleLoaded()){
            if(caster instanceof Furniture) {
                id = ((Furniture) caster).getEntity().getUniqueId().toString();
            }
        }
        if(caster instanceof ActiveMob) {
            id = ((ActiveMob) caster).getEntity().getUniqueId().toString();
        }
        if(id == "") return SkillResult.SUCCESS;
        StorageManager storageManager = core.getManagers().getStorageManager();
        if(!storageManager.storageExists(id)) return SkillResult.ERROR;
        Storage storage = storageManager.getStorage(id);
        Bukkit.getScheduler().runTaskAsynchronously(core,() -> storage.loadAllItemsDefault());
        return SkillResult.SUCCESS;
    }
}