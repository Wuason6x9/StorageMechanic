package dev.wuason.storagemechanic.storages.types.entity;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.storages.types.block.compatibilities.mythic.MythicPlaceBlockSkill;
import dev.wuason.storagemechanic.storages.types.entity.skills.*;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.core.skills.SkillTriggers;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EntityMythicManager implements Listener {
    private StorageMechanic core;

    public EntityMythicManager(StorageMechanic core) {
        this.core = core;
    }

    @EventHandler
    public void onMythicMechanicLoad(MythicMechanicLoadEvent mythicMechanicLoadEvent){

        if(mythicMechanicLoadEvent.getMechanicName().equalsIgnoreCase(Skills.smCreate.toString())){
            mythicMechanicLoadEvent.register(new SmCreateMythicMechanic(mythicMechanicLoadEvent.getConfig(), core));
        }
        if(mythicMechanicLoadEvent.getMechanicName().equalsIgnoreCase(Skills.smRemove.toString())){
            mythicMechanicLoadEvent.register(new SmRemoveMythicMechanic(mythicMechanicLoadEvent.getConfig(), core));
        }
        if(mythicMechanicLoadEvent.getMechanicName().equalsIgnoreCase(Skills.smLoad.toString())){
            mythicMechanicLoadEvent.register(new SmLoadMythicMechanic(mythicMechanicLoadEvent.getConfig(), core));
        }
        if(mythicMechanicLoadEvent.getMechanicName().equalsIgnoreCase(Skills.smSave.toString())){
            mythicMechanicLoadEvent.register(new SmSaveMythicMechanic(mythicMechanicLoadEvent.getConfig(), core));
        }
        if(mythicMechanicLoadEvent.getMechanicName().equalsIgnoreCase(Skills.smDrop.toString())){
            mythicMechanicLoadEvent.register(new SmDropMythicMechanic(mythicMechanicLoadEvent.getConfig(), core));
        }
        if(mythicMechanicLoadEvent.getMechanicName().equalsIgnoreCase(Skills.smOpen.toString())){
            mythicMechanicLoadEvent.register(new SmOpenMythicMechanic(mythicMechanicLoadEvent.getConfig(), core));
        }
        if(mythicMechanicLoadEvent.getMechanicName().equalsIgnoreCase(Skills.smLoadDefaultItems.toString())){
            mythicMechanicLoadEvent.register(new SmLoadDefaultItemsMythicMechanic(mythicMechanicLoadEvent.getConfig(), core));
        }

    }

    public enum Skills{
        smCreate,
        smRemove,
        smLoad,
        smSave,
        smDrop,
        smOpen,
        smLoadDefaultItems
    }
}
