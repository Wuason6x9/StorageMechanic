package dev.wuason.storagemechanic.storages.types.block.config;

import org.bukkit.event.block.Action;

public enum BlockStorageClickType {

    LEFT(Action.LEFT_CLICK_BLOCK),
    RIGHT(Action.RIGHT_CLICK_BLOCK);

    private Action action;

    private BlockStorageClickType(Action action) {
        this.action = action;
    }

    public Action getAction() {
        return action;
    }
}
