package dev.wuason.storagemechanic.storages.types.entity;

import dev.wuason.storagemechanic.StorageMechanic;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillTrigger;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicPostReloadedEvent;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.core.skills.TriggeredSkill;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.function.Consumer;

public class MythicManager implements Listener {
    private EntityMythicManager entityMythicManager;
    private StorageTriggers storageTriggers;
    private StorageMechanic core;
    private HashMap<String, HashMap<SkillTrigger, Queue<SkillMechanic>>> triggerSkills = new HashMap<>();
    public MythicManager(StorageMechanic core) {
        this.core = core;
        load();
    }

    public void load(){
        storageTriggers = new StorageTriggers();
        entityMythicManager = new EntityMythicManager(core);
        Bukkit.getPluginManager().registerEvents(entityMythicManager,core);
    }

    @EventHandler
    public void onReloadConfig(MythicPostReloadedEvent event){
        triggerSkills = new HashMap<>();
        Collection<MythicMob> mythicMobs = MythicBukkit.inst().getMobManager().getMobTypes();

        for(MythicMob mythicMob : mythicMobs){
            HashMap<SkillTrigger, Queue<SkillMechanic>> map = new HashMap<>();
            for(SkillTrigger trigger : StorageTriggers.getTriggers()){
                Queue<SkillMechanic> skillMechanics = mythicMob.getSkills(trigger);
                if(skillMechanics.size()>0){
                    map.put(trigger,skillMechanics);
                }
            }
            triggerSkills.put(mythicMob.getInternalName(), map);
        }
    }
    public boolean runSkills(String internalName, SkillCaster caster, SkillTrigger cause, AbstractLocation origin, AbstractEntity trigger, Consumer<SkillMetadata> transformer) {
        if (!triggerSkills.containsKey(internalName) && !triggerSkills.get(internalName).containsKey(cause)) {
            return false;
        } else {
            TriggeredSkill ts = new TriggeredSkill(cause, caster, origin, trigger, (Collection)triggerSkills.get(internalName).get(cause), true, (meta) -> {
                if (transformer != null) {
                    transformer.accept(meta);
                }

            });
            return false;
        }
    }

    public EntityMythicManager getEntityMythicManager() {
        return entityMythicManager;
    }

    public StorageTriggers getStorageTriggers() {
        return storageTriggers;
    }


}
