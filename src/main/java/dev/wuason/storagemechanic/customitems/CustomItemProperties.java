package dev.wuason.storagemechanic.customitems;

public class CustomItemProperties {

    private boolean dropBlock = true;
    private boolean stackable = true;

    public CustomItemProperties(boolean dropBlock, boolean stackable) {
        this.stackable = stackable;
        this.dropBlock = dropBlock;
    }

    public boolean isDropBlock() {
        return dropBlock;
    }

    public boolean isStackable() {
        return stackable;
    }
}
