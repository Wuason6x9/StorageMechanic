package dev.wuason.storagemechanic.storages.types.block.mechanics;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.storages.types.block.mechanics.integrated.hopper.HopperBlockMechanic;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class BlockMechanicManager {

    private Map<String, BlockMechanic> mechanics;
    private StorageMechanic core;
    public static HopperBlockMechanic HOPPER_BLOCK_MECHANIC;

    public BlockMechanicManager(StorageMechanic core) {
        this.mechanics = new HashMap<>();
        this.core = core;
        regDef();
    }

    public void registerMechanic(BlockMechanic mechanic) {
        if (mechanic instanceof Listener) {
            Bukkit.getPluginManager().registerEvents((Listener) mechanic, core);
        }
        this.mechanics.put(mechanic.getId(), mechanic);
    }

    public void regDef() {
        HOPPER_BLOCK_MECHANIC = new HopperBlockMechanic(core);

        registerMechanic(HOPPER_BLOCK_MECHANIC);
    }

    public void unregisterMechanic(String id) {
        this.mechanics.remove(id);
    }

    public BlockMechanic getMechanic(String id) {
        return this.mechanics.get(id);
    }

    public boolean mechanicExists(String id) {
        return this.mechanics.containsKey(id);
    }
}