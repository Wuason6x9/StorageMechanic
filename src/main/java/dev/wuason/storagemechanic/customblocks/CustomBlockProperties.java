package dev.wuason.storagemechanic.customblocks;

public class CustomBlockProperties {

    private boolean dropBlock = true;
    private boolean stackable = true;

    public CustomBlockProperties(boolean dropBlock, boolean stackable) {
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
