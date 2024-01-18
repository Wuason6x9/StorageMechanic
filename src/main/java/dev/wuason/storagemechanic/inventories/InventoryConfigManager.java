package dev.wuason.storagemechanic.inventories;

import dev.wuason.mechanics.mechanics.MechanicAddon;
import dev.wuason.storagemechanic.StorageMechanic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class InventoryConfigManager extends dev.wuason.mechanics.configuration.inventories.InventoryConfigManager {
    public InventoryConfigManager(@NotNull MechanicAddon core) {
        super(core, new File(((StorageMechanic) core).getDataFolder() + "/inventories/"));
    }
}
