package dev.wuason.storagemechanic.storages.types.entity;

import com.google.common.collect.Maps;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.storages.types.block.compatibilities.mythic.MythicPlaceBlockSkill;
import dev.wuason.storagemechanic.storages.types.entity.skills.*;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillTrigger;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.core.skills.SkillTriggers;
import io.lumine.mythic.core.skills.TriggeredSkill;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;

public class EntityMythicManager implements Listener {
    private StorageMechanic core;


    public EntityMythicManager(StorageMechanic core) {
        this.core = core;
    }

    @EventHandler
    public void onMythicMechanicLoad(MythicMechanicLoadEvent event){
        if(event.getMechanicName().equalsIgnoreCase(Skills.smCreate.toString())){
            event.register(new SmCreateMythicMechanic(event.getConfig(), core));
        }
        if(event.getMechanicName().equalsIgnoreCase(Skills.smRemove.toString())){
            event.register(new SmRemoveMythicMechanic(event.getConfig(), core));
        }
        if(event.getMechanicName().equalsIgnoreCase(Skills.smLoad.toString())){
            event.register(new SmLoadMythicMechanic(event.getConfig(), core));
        }
        if(event.getMechanicName().equalsIgnoreCase(Skills.smSave.toString())){
            event.register(new SmSaveMythicMechanic(event.getConfig(), core));
        }
        if(event.getMechanicName().equalsIgnoreCase(Skills.smDrop.toString())){
            event.register(new SmDropMythicMechanic(event.getConfig(), core));
        }
        if(event.getMechanicName().equalsIgnoreCase(Skills.smOpen.toString())){
            event.register(new SmOpenMythicMechanic(event.getConfig(), core));
        }
        if(event.getMechanicName().equalsIgnoreCase(Skills.smLoadDefaultItems.toString())){
            event.register(new SmLoadDefaultItemsMythicMechanic(event.getConfig(), core));
        }
        if(event.getMechanicName().equalsIgnoreCase(Skills.smCollect.toString())){
            event.register(new SmCollectMythicMechanic(core));
        }
        if(event.getMechanicName().equalsIgnoreCase(Skills.smExecuteAction.toString())){
            event.register(new SmExecuteAction(event.getConfig(), core));
        }

    }


    public enum Skills{
        smCreate,
        smRemove,
        smLoad,
        smSave,
        smDrop,
        smOpen,
        smLoadDefaultItems,
        smCollect,
        smExecuteAction

    }
}
