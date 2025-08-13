package dev.wuason.storagemechanic.storages.types.entity.skills;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.compatibilities.Compatibilities;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageManager;
import dev.wuason.storagemechanic.storages.types.entity.StorageTriggers;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythiccrucible.items.furniture.Furniture;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
        SkillCaster caster = skillMetadata.getCaster();
        String id = "";
        Location location = null;
        String idTriggerSkill = null;
        if(Compatibilities.isMythicCrucibleLoaded()){
            if(caster instanceof Furniture) {
                location = BukkitAdapter.adapt(((Furniture) caster).getLocation());
                id = ((Furniture) caster).getEntity().getUniqueId().toString();
                idTriggerSkill = ((Furniture) caster).getFurnitureData().getItem().getInternalName();
            }
        }
        if(caster instanceof ActiveMob) {
            location = BukkitAdapter.adapt(((ActiveMob) caster).getLocation());
            id = ((ActiveMob) caster).getEntity().getUniqueId().toString();
            idTriggerSkill = ((ActiveMob) caster).getType().getInternalName();
        }
        if (!core.getMechanics().getAntiGriefLib().canInteract((Player)skillMetadata.getTrigger().asPlayer().getBukkitEntity(),location)) return SkillResult.ERROR;
        //if(!ProtectionLib.canInteract((Player)skillMetadata.getTrigger().asPlayer().getBukkitEntity(),location)) return SkillResult.ERROR;
        if(!(skillMetadata.getTrigger().getBukkitEntity() instanceof Player)) return SkillResult.ERROR;
        if(!storageManager.storageExists(id)) return SkillResult.ERROR;
        Storage storage = storageManager.getStorage(id);
        Player player = (Player) skillMetadata.getTrigger().getBukkitEntity();
        String finalIdTriggerSkill = idTriggerSkill;
        Bukkit.getScheduler().runTask(core,() -> {
            boolean opened = storage.openStorageR(player, 0);
            if(opened && storage.getAllViewers().size()<2){
                core.getManagers().getMythicManager().runSkills(finalIdTriggerSkill,caster, StorageTriggers.OPEN_STORAGE, BukkitAdapter.adapt(player.getLocation()),BukkitAdapter.adapt(player),null);
            }
        });
        return SkillResult.SUCCESS;
    }
}
