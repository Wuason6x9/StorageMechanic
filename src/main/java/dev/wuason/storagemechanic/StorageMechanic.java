package dev.wuason.storagemechanic;

import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.utils.MechanicsUtils;
import org.bukkit.plugin.java.JavaPlugin;

public final class StorageMechanic extends JavaPlugin {

    @Override
    public void onEnable() {

        if(!MechanicsUtils.isMechanicLoaded(this)){

            getLogger().severe("ERROR LOADING STORAGE MECHANIC! ");
            getLogger().severe("You must not put the plugin in the following folder!");
            getLogger().severe(Mechanics.getInstance().getDataFolder().getPath() + "/mechanics");

            getPluginLoader().disablePlugin(this);


        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
