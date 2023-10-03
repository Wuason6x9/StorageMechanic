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
import org.bukkit.entity.Player;

public class SmRemoveMythicMechanic implements ITargetedEntitySkill {
    private StorageMechanic core;
    private boolean dropItems;
    public SmRemoveMythicMechanic(MythicLineConfig config, StorageMechanic core) {
        this.core = core;
        dropItems = config.getBoolean("drop",false);
    }
    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        SkillCaster caster = skillMetadata.getCaster();
        String id = "";
        Location loc = null;
        if(FurnitureStorageManager.isMythicCrucibleLoaded()){
            if(caster instanceof Furniture) {
                id = ((Furniture) caster).getEntity().getUniqueId().toString();
                loc = ((Furniture) caster).getEntity().getBukkitEntity().getLocation();
            }
        }
        if(caster instanceof ActiveMob) {
            id = ((ActiveMob) caster).getEntity().getUniqueId().toString();
            loc = ((ActiveMob) caster).getEntity().getBukkitEntity().getLocation();
        }
        if(id == "") return SkillResult.SUCCESS;
        StorageManager storageManager = core.getManagers().getStorageManager();
        if(!storageManager.storageExists(id)) return SkillResult.ERROR;
        Storage storage = storageManager.getStorage(id);
        Bukkit.getScheduler().runTask(core,() -> storage.closeAllInventory());
        if(dropItems) {
            Location finalLoc = loc;
            Bukkit.getScheduler().runTaskAsynchronously(core,() -> storage.dropAllItems(finalLoc));
        }
        storageManager.removeStorage(id);
        return null;
    }
}
