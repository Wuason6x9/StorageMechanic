package dev.wuason.storagemechanic.storages.types.entity.skills;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.actions.Action;
import dev.wuason.storagemechanic.actions.events.EventAction;
import dev.wuason.storagemechanic.actions.events.SkillMythicExecutorAction;
import dev.wuason.storagemechanic.compatibilities.Compatibilities;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageManager;
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
import org.bukkit.entity.Player;

public class SmExecuteAction implements ITargetedEntitySkill {

    private StorageMechanic core;

    private String actionConfigId;

    public SmExecuteAction(MythicLineConfig config, StorageMechanic core) {
        actionConfigId = config.getString("id");
        this.core = core;
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        if(actionConfigId == null) return SkillResult.ERROR;
        StorageManager storageManager = core.getManagers().getStorageManager();
        SkillCaster caster = skillMetadata.getCaster();
        String id = "";
        if(Compatibilities.isMythicCrucibleLoaded()){
            if(caster instanceof Furniture) {
                id = ((Furniture) caster).getEntity().getUniqueId().toString();
            }
        }
        if(caster instanceof ActiveMob) {
            id = ((ActiveMob) caster).getEntity().getUniqueId().toString();
        }
        if(!storageManager.storageExists(id)) return SkillResult.ERROR;
        Storage storage = storageManager.getStorage(id);
        Player player = null;
        if(skillMetadata.getTrigger() != null && skillMetadata.getTrigger().getBukkitEntity() instanceof Player){
            player = (Player) skillMetadata.getTrigger().getBukkitEntity();
        }
        EventAction eventAction = new SkillMythicExecutorAction(null,abstractEntity,skillMetadata);
        try{
            Action action = core.getManagers().getActionManager().createAction(storage, actionConfigId.intern(), player, eventAction);
            if(action == null) return SkillResult.ERROR;
            action.execute();
        }catch (Exception e){
            e.printStackTrace();
        }
        return SkillResult.SUCCESS;
    }
}
