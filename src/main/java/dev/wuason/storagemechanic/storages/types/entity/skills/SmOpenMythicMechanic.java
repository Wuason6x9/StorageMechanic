package dev.wuason.storagemechanic.storages.types.entity.skills;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageManager;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.th0rgal.protectionlib.ProtectionLib;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SmOpenMythicMechanic implements ITargetedEntitySkill {
    private StorageMechanic core;
    private String storageConfigId;
    public SmOpenMythicMechanic(MythicLineConfig config, StorageMechanic core) {
        this.core = core;
    }
    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        StorageManager storageManager = core.getManagers().getStorageManager();
        if(!ProtectionLib.canInteract((Player)skillMetadata.getTrigger().asPlayer().getBukkitEntity(),abstractEntity.getBukkitEntity().getLocation())) return SkillResult.ERROR;
        if(!(skillMetadata.getTrigger().getBukkitEntity() instanceof Player)) return SkillResult.ERROR;
        if(!storageManager.storageExists(abstractEntity.getUniqueId().toString())) return SkillResult.ERROR;
        Storage storage = storageManager.getStorage(abstractEntity.getUniqueId().toString());
        Player player = (Player) skillMetadata.getTrigger().getBukkitEntity();
        Bukkit.getScheduler().runTask(core,() -> storage.openStorage(player,0));
        return SkillResult.SUCCESS;
    }
}
