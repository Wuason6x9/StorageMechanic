package dev.wuason.storagemechanic.actions.events;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.event.Event;

import java.util.HashMap;

public class SkillMythicExecutorAction extends EventAction {

    private SkillMetadata skillMetadata;
    private AbstractEntity abstractEntity;

    public SkillMythicExecutorAction(Event event, AbstractEntity abstractEntity, SkillMetadata skillMetadata) {
        super(SkillMythicExecutorAction.class.getSimpleName(), event);
        this.skillMetadata = skillMetadata;
        this.abstractEntity = abstractEntity;
    }

    @Override
    public void registerPlaceholders(HashMap<String, Object> currentPlaceholders) {
        currentPlaceholders.put("$caster$".toUpperCase().intern(), skillMetadata.getCaster());
        currentPlaceholders.put("$skill_metadata$".toUpperCase().intern(), skillMetadata);
        currentPlaceholders.put("$skill_abstract_entity$".toUpperCase().intern(), skillMetadata);
        currentPlaceholders.put("$activeMob_uuid$".toUpperCase().intern(), skillMetadata.getCaster().getEntity().getUniqueId());
    }
}
