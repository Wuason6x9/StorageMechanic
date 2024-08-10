package dev.wuason.storagemechanic.storages.types.block.config;

import dev.wuason.storagemechanic.storages.types.block.mechanics.BlockMechanic;

public class BlockStorageMechanicConfig {
    private boolean enabled = false;
    private BlockMechanicProperties blockMechanicProperties;
    private BlockMechanic blockMechanic;

    public BlockStorageMechanicConfig(boolean enabled, BlockMechanicProperties blockMechanicProperties, BlockMechanic blockMechanic) {
        this.enabled = enabled;
        this.blockMechanicProperties = blockMechanicProperties;
        this.blockMechanic = blockMechanic;
    }

    public BlockMechanic getBlockMechanic() {
        return blockMechanic;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public BlockMechanicProperties getBlockMechanicProperties() {
        return blockMechanicProperties;
    }
}
