package dev.wuason.storagemechanic.storages.types.entity.skills;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.compatibilities.Compatibilities;
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
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SmDropMythicMechanic implements ITargetedEntitySkill {

    private StorageMechanic core;
    private String storageConfigId;

    public SmDropMythicMechanic(MythicLineConfig config, StorageMechanic core) {
        this.core = core;
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        SkillCaster caster = skillMetadata.getCaster();
        String id = "";
        Location location = null;
        if (Compatibilities.isMythicCrucibleLoaded()) {
            if (caster instanceof Furniture) {
                id = ((Furniture) caster).getEntity().getUniqueId().toString();
                location = ((Furniture) caster).getEntity().getBukkitEntity().getLocation();
            }
        }
        if (caster instanceof ActiveMob) {
            id = ((ActiveMob) caster).getEntity().getUniqueId().toString();
            location = ((ActiveMob) caster).getEntity().getBukkitEntity().getLocation();
        }
        if (id == "") return SkillResult.SUCCESS;
        StorageManager storageManager = core.getManagers().getStorageManager();
        if (!storageManager.storageExists(id)) return SkillResult.ERROR;
        Storage storage = storageManager.getStorage(id);
        Location finalLocation = location;
        Bukkit.getScheduler().runTask(core, () -> storage.dropAllItems(finalLocation));
        return SkillResult.SUCCESS;
    }
}
