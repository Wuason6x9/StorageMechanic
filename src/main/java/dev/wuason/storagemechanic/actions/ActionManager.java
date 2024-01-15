package dev.wuason.storagemechanic.actions;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.actions.events.EventsRegister;
import dev.wuason.storagemechanic.actions.executors.ExecutorsStorage;
import dev.wuason.storagemechanic.actions.functions.FunctionsStorage;

public class ActionManager extends dev.wuason.mechanics.actions.ActionManager {

    public ActionManager(StorageMechanic core) {
        super(core, true);
        setListenDefEvents(false);
        EventsRegister.registerEvents();
        ExecutorsStorage.registerExecutors();
        FunctionsStorage.registerFunctions();
        registerAllEvents();
    }




}
