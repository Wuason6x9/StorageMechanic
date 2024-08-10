package dev.wuason.storagemechanic.storages.types.entity;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.compatibilities.Compatibilities;
import dev.wuason.storagemechanic.storages.StorageOriginContext;
import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillTrigger;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicPostReloadedEvent;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.mobs.MobExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.core.skills.TriggeredSkill;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.function.Consumer;

public class MythicManager implements Listener {
    private EntityMythicManager entityMythicManager;
    private StorageTriggers storageTriggers;
    private StorageMechanic core;
    MythicCrucibleImpl mythicCrucibleImpl = null;
    private HashMap<String, HashMap<SkillTrigger, Queue<SkillMechanic>>> triggerSkills = new HashMap<>();
    public MythicManager(StorageMechanic core) {
        this.core = core;
        load();
    }

    public void load(){
        storageTriggers = new StorageTriggers();
        entityMythicManager = new EntityMythicManager(core);
        Bukkit.getPluginManager().registerEvents(entityMythicManager,core);
        if(Compatibilities.isMythicCrucibleLoaded()){
            mythicCrucibleImpl = new MythicCrucibleImpl();
        }
    }

    @EventHandler
    public void onReloadConfig(MythicPostReloadedEvent event){
        triggerSkills = new HashMap<>();
        Collection<MythicMob> mythicMobs = MythicBukkit.inst().getMobManager().getMobTypes();

        for(MythicMob mythicMob : mythicMobs){
            HashMap<SkillTrigger, Queue<SkillMechanic>> map = new HashMap<>();
            for(SkillTrigger trigger : StorageTriggers.getTriggers()){
                Queue<SkillMechanic> skillMechanics = mythicMob.getSkills(trigger);
                if(!skillMechanics.isEmpty()){
                    map.put(trigger,skillMechanics);
                }
            }
            triggerSkills.put(mythicMob.getInternalName(), map);
        }
        if(mythicCrucibleImpl != null){
            mythicCrucibleImpl.a(triggerSkills);
        }

    }
    public boolean runSkills(String id, SkillCaster caster, SkillTrigger cause, AbstractLocation origin, AbstractEntity trigger, Consumer<SkillMetadata> transformer) {
        if (!triggerSkills.containsKey(id) || !triggerSkills.get(id).containsKey(cause)) {
            return false;
        } else {
            TriggeredSkill ts = new TriggeredSkill(cause, caster, origin, trigger, (Collection)triggerSkills.get(id).get(cause), true, (meta) -> {
                if (transformer != null) {
                    transformer.accept(meta);
                }

            });
            return false;
        }
    }

    public void executeCloseStorageSkill(StorageOriginContext storageOriginContext, String storageId, StorageInventory storageInventory){
        UUID uuid = null;
        try {
            uuid = UUID.fromString(storageId);

            if(storageOriginContext.getContext().equals(StorageOriginContext.Context.ENTITY_STORAGE)){

                String type = storageOriginContext.getData().get(0);
                String id = null;
                SkillCaster skillCaster = null;
                switch (type){
                    case "MOB" ->{
                        MobExecutor mobManager = MythicBukkit.inst().getMobManager();
                        if(mobManager.isActiveMob(uuid)){
                            ActiveMob activeMob = mobManager.getActiveMob(uuid).get();
                            MythicMob mythicMob = activeMob.getType();
                            id = mythicMob.getInternalName();
                            skillCaster = activeMob;
                        }
                    }
                    case "FURNITURE" ->{
                        if(mythicCrucibleImpl != null){
                            Object[] objects = mythicCrucibleImpl.b(uuid);
                            skillCaster = (SkillCaster) objects[0];
                            id = (String) objects[1];
                        }

                    }
                }

                core.getManagers().getMythicManager().runSkills(id,skillCaster,StorageTriggers.CLOSE_STORAGE, BukkitAdapter.adapt(storageInventory.getInventory().getViewers().get(0).getLocation()),BukkitAdapter.adapt((Player)storageInventory.getInventory().getViewers().get(0)),null);

            }

        }
        catch (Exception e){
        }
    }

    public EntityMythicManager getEntityMythicManager() {
        return entityMythicManager;
    }

    public StorageTriggers getStorageTriggers() {
        return storageTriggers;
    }


}
