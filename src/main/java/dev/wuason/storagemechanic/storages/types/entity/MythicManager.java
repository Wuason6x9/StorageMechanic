package dev.wuason.storagemechanic.storages.types.entity;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.storages.types.furnitures.FurnitureStorageManager;
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
import io.lumine.mythiccrucible.MythicCrucible;
import io.lumine.mythiccrucible.items.CrucibleItem;
import io.lumine.mythiccrucible.items.CrucibleItemType;
import io.lumine.mythiccrucible.items.furniture.FurnitureItemContext;
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
                if(!skillMechanics.isEmpty()){
                    map.put(trigger,skillMechanics);
                }
            }
            triggerSkills.put(mythicMob.toString(), map);
        }
        if(FurnitureStorageManager.isMythicCrucibleLoaded()){
            Collection<CrucibleItem> items = MythicCrucible.inst().getItemManager().getItems();
            for(CrucibleItem crucibleItem : items){
                if(crucibleItem.getType().equals(CrucibleItemType.FURNITURE)){
                    FurnitureItemContext furnitureItemContext = crucibleItem.getFurnitureData();
                    HashMap<SkillTrigger, Queue<SkillMechanic>> map = new HashMap<>();
                    for(SkillTrigger trigger : StorageTriggers.getTriggers()){
                        Queue<SkillMechanic> skillMechanics = furnitureItemContext.getSkills(trigger);
                        if(skillMechanics != null && !skillMechanics.isEmpty()){
                            map.put(trigger,skillMechanics);
                        }
                    }
                    triggerSkills.put(crucibleItem.getInternalName(), map);
                }
            }
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

    public EntityMythicManager getEntityMythicManager() {
        return entityMythicManager;
    }

    public StorageTriggers getStorageTriggers() {
        return storageTriggers;
    }


}
