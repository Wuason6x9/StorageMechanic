package dev.wuason.storagemechanic.storages.types.entity;

import io.lumine.mythic.api.skills.SkillTrigger;

import java.util.HashSet;
import java.util.Set;

public class StorageTriggers {
    public static SkillTrigger OPEN_STORAGE;
    public static SkillTrigger CLOSE_STORAGE;
    private static Set<SkillTrigger> triggers = new HashSet<>();

    public StorageTriggers() {
        load();
    }

    public void load(){

        OPEN_STORAGE = SkillTrigger.create("OPENSTORAGE", new String[]{"OPENS", "OPEN_STORAGE","STORAGEOPEN", "STORAGE_OPEN"});
        OPEN_STORAGE.register();

        CLOSE_STORAGE = SkillTrigger.create("CLOSESTORAGE", new String[]{"CLOSES", "CLOSE_STORAGE","STORAGECLOSE", "STORAGE_CLOSE"});
        CLOSE_STORAGE.register();

        triggers.add(OPEN_STORAGE);
        triggers.add(CLOSE_STORAGE);
    }

    public static Set<SkillTrigger> getTriggers() {
        return triggers;
    }
}
