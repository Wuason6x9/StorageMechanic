package dev.wuason.storagemechanic.api;

import dev.wuason.storagemechanic.StorageMechanic;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class StorageMechanicAPI {

    private final StorageMechanic storageMechanic;
    private static final List<Plugin> PLUGINS_REGISTERED = new ArrayList<>();

    public StorageMechanicAPI(Plugin plugin){
        if(plugin != null){
            if(PLUGINS_REGISTERED.contains(plugin)) throw new RuntimeException("Plugin already registered");
            PLUGINS_REGISTERED.add(plugin);
        }
        this.storageMechanic = StorageMechanic.getInstance();
    }

    public StorageMechanicAPI() {
        this(null);
    }


    public StorageMechanic getStorageMechanicInstance() {
        return storageMechanic;
    }

    public static List<Plugin> getPluginsRegistered() {
        return PLUGINS_REGISTERED;
    }

}
