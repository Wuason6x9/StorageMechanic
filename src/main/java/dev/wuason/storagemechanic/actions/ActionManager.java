package dev.wuason.storagemechanic.actions;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.actions.config.ActionConfigManager;

public class ActionManager extends dev.wuason.mechanics.actions.ActionManager {

    private ActionConfigManager actionConfigManager;
    private StorageMechanic core;

    public ActionManager(StorageMechanic core) {
        super(core);
        this.core = core;
        setListenDefEvents(true);
        actionConfigManager = new ActionConfigManager(core,this);
    }

    public ActionConfigManager getActionConfigManager() {
        return actionConfigManager;
    }
}
