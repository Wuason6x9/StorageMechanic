package dev.wuason.storagemechanic.actions.executors;

import dev.wuason.mechanics.actions.executators.Executors;

public class ExecutorsStorage {
    public static void registerExecutors() {
        Executors.EXECUTORS.put("STORAGE", new StorageExecutor());
    }
}
