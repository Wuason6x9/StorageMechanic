package dev.wuason.storagemechanic.customblocks;

public class CustomBlockProperties {

    private boolean dropBlock = true;

    public CustomBlockProperties(boolean dropBlock) {
        this.dropBlock = dropBlock;
    }

    public boolean isDropBlock() {
        return dropBlock;
    }
}
