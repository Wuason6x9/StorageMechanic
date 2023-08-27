package dev.wuason.storagemechanic.storages.types.entity.skills;

import dev.wuason.protectionlib.ProtectionLib;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.compatibilities.MythicMobs;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageManager;
import dev.wuason.storagemechanic.storages.types.entity.StorageTriggers;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.mobs.MobExecutor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

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
        ActiveMob activeMob = (ActiveMob)skillMetadata.getCaster();
        MythicMob mythicMob = activeMob.getType();
        if(storage.getAllViewers().size()<2){
            core.getManagers().getMythicManager().runSkills(mythicMob.getInternalName(),activeMob, StorageTriggers.OPEN_STORAGE, BukkitAdapter.adapt(player.getLocation()),BukkitAdapter.adapt(player),null);
        }
        return SkillResult.SUCCESS;
    }
}
