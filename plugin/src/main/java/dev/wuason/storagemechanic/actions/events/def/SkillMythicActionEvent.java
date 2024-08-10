package dev.wuason.storagemechanic.actions.events.def;

import dev.wuason.mechanics.actions.Action;
import dev.wuason.mechanics.actions.events.EventAction;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.SkillMetadata;

import java.util.Locale;

public class SkillMythicActionEvent implements EventAction {

    private final SkillMetadata skillMetadata;
    private final AbstractEntity abstractEntity;

    public SkillMythicActionEvent(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        this.skillMetadata = skillMetadata;
        this.abstractEntity = abstractEntity;
    }

    @Override
    public void registerPlaceholders(Action action) {

        action.registerPlaceholder("$skillMetadata$", skillMetadata);
        action.registerPlaceholder("$abstractEntity$", abstractEntity);
        action.registerPlaceholder("$caster$", skillMetadata.getCaster());
        action.registerPlaceholder("$cause$", skillMetadata.getCause());
        action.registerPlaceholder("$trigger$", skillMetadata.getTrigger());
        action.registerPlaceholder("$casterEntityUUID$", skillMetadata.getCaster().getEntity().getUniqueId());
        action.registerPlaceholder("$casterEntity$", skillMetadata.getCaster().getEntity());
        action.registerPlaceholder("$casterLocation$", skillMetadata.getCaster().getLocation());
        action.registerPlaceholder("$abstractEntityUUID$", abstractEntity.getUniqueId());

    }

    @Override
    public String getId() {
        return "skill_mythic".toUpperCase(Locale.ENGLISH);
    }
}
