package dev.wuason.storagemechanic.actions;

import dev.wuason.mechanics.actions.events.EventAction;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.actions.events.EventsRegister;
import dev.wuason.storagemechanic.actions.executors.ExecutorsStorage;
import dev.wuason.storagemechanic.actions.functions.FunctionsStorage;

import java.util.concurrent.atomic.AtomicBoolean;

public class ActionManager extends dev.wuason.mechanics.actions.ActionManager {

    private final AtomicBoolean isEnabled = new AtomicBoolean(true);

    public ActionManager(StorageMechanic core) {
        super(core, true);
        EventsRegister.registerEvents();
        ExecutorsStorage.registerExecutors();
        FunctionsStorage.registerFunctions();
        registerAllEvents();
        addCallEventApiEventListener(event -> {
            if (!isEnabled()) {
                event.setCancelled(true);
            }
        });
    }

    public void setEnabled(boolean enabled) {
        isEnabled.set(enabled);
    }

    public boolean isEnabled() {
        return isEnabled.get();
    }

    @Override
    public void callEvent(EventAction eventAction, String namespace, Object... args) {
        super.callEvent(eventAction, namespace, args);
    }
}
