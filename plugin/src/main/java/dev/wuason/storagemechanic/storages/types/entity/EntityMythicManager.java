package dev.wuason.storagemechanic.storages.types.entity;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.storages.types.entity.skills.*;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EntityMythicManager implements Listener {
    private StorageMechanic core;


    public EntityMythicManager(StorageMechanic core) {
        this.core = core;
    }

    @EventHandler
    public void onMythicMechanicLoad(MythicMechanicLoadEvent event) {
        if (event.getMechanicName().equalsIgnoreCase(Skills.smCreate.toString())) {
            event.register(new SmCreateMythicMechanic(event.getConfig(), core));
        }
        if (event.getMechanicName().equalsIgnoreCase(Skills.smRemove.toString())) {
            event.register(new SmRemoveMythicMechanic(event.getConfig(), core));
        }
        if (event.getMechanicName().equalsIgnoreCase(Skills.smLoad.toString())) {
            event.register(new SmLoadMythicMechanic(event.getConfig(), core));
        }
        if (event.getMechanicName().equalsIgnoreCase(Skills.smSave.toString())) {
            event.register(new SmSaveMythicMechanic(event.getConfig(), core));
        }
        if (event.getMechanicName().equalsIgnoreCase(Skills.smDrop.toString())) {
            event.register(new SmDropMythicMechanic(event.getConfig(), core));
        }
        if (event.getMechanicName().equalsIgnoreCase(Skills.smOpen.toString())) {
            event.register(new SmOpenMythicMechanic(event.getConfig(), core));
        }
        if (event.getMechanicName().equalsIgnoreCase(Skills.smLoadDefaultItems.toString())) {
            event.register(new SmLoadDefaultItemsMythicMechanic(event.getConfig(), core));
        }
        if (event.getMechanicName().equalsIgnoreCase(Skills.smCollect.toString())) {
            event.register(new SmCollectMythicMechanic(core));
        }
        if (event.getMechanicName().equalsIgnoreCase(Skills.smExecuteAction.toString())) {
            event.register(new SmExecuteAction(event.getConfig(), core));
        }

    }


    public enum Skills {
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
