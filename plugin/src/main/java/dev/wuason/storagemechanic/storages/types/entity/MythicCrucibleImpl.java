package dev.wuason.storagemechanic.storages.types.entity;

import io.lumine.mythic.api.skills.SkillTrigger;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythiccrucible.MythicCrucible;
import io.lumine.mythiccrucible.items.CrucibleItem;
import io.lumine.mythiccrucible.items.CrucibleItemType;
import io.lumine.mythiccrucible.items.furniture.Furniture;
import io.lumine.mythiccrucible.items.furniture.FurnitureItemContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Queue;
import java.util.UUID;

public class MythicCrucibleImpl {

    public void a(HashMap<String, HashMap<SkillTrigger, Queue<SkillMechanic>>> triggerSkills) {
        Collection<CrucibleItem> items = MythicCrucible.inst().getItemManager().getItems();
        for (CrucibleItem crucibleItem : items) {
            if (crucibleItem.getType().equals(CrucibleItemType.FURNITURE)) {
                FurnitureItemContext furnitureItemContext = crucibleItem.getFurnitureData();
                HashMap<SkillTrigger, Queue<SkillMechanic>> map = new HashMap<>();
                for (SkillTrigger trigger : StorageTriggers.getTriggers()) {
                    Queue<SkillMechanic> skillMechanics = furnitureItemContext.getSkills(trigger);
                    if (skillMechanics != null && !skillMechanics.isEmpty()) {
                        map.put(trigger, skillMechanics);
                    }
                }
                triggerSkills.put(crucibleItem.getInternalName(), map);
            }
        }
    }

    public Object[] b(UUID uuid) {
        Object[] objects = new Object[2];
        Furniture furniture = MythicCrucible.inst().getItemManager().getFurnitureManager().getFurniture(uuid).get();
        objects[0] = furniture;
        objects[1] = furniture.getFurnitureData().getItem().getInternalName();
        return objects;
    }

}
