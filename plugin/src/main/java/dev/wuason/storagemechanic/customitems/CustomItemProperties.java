package dev.wuason.storagemechanic.customitems;

public class CustomItemProperties {

    private final boolean dropBlock;
    private final boolean stackable;
    private final String skullTexture;

    public CustomItemProperties(boolean dropBlock, boolean stackable, String skullTexture) {
        this.stackable = stackable;
        this.dropBlock = dropBlock;
        this.skullTexture = skullTexture;
    }

    public boolean isDropBlock() {
        return dropBlock;
    }

    public boolean isStackable() {
        return stackable;
    }

    public String getSkullTexture() {
        return skullTexture;
    }
}
