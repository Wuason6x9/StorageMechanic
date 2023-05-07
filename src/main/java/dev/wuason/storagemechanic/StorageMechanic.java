package dev.wuason.storagemechanic;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIConfig;
import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.mechanics.utils.MechanicsUtils;
import org.bukkit.plugin.java.JavaPlugin;

public final class StorageMechanic extends JavaPlugin {

    private static StorageMechanic instance;
    private Managers managers;

    public StorageMechanic(){

        instance = this;

    }

    @Override
    public void onLoad() {
        AdventureUtils.sendMessagePluginConsole(this, "<green>Loading StorageMechanic...");
        CommandAPI.onLoad(new CommandAPIConfig().silentLogs(true));
    }

    @Override
    public void onEnable() {

        if(!MechanicsUtils.isMechanicLoaded(this)){
            getLogger().severe("ERROR LOADING STORAGE MECHANIC! ");
            getLogger().severe("You must not put the plugin in the following folder!");
            getPluginLoader().disablePlugin(this);
        }
        CommandAPI.onEnable(this);


        AdventureUtils.sendMessagePluginConsole(this, "<green>Loading Managers...");
        managers = new Managers(this);
        managers.loadManagers();
        AdventureUtils.sendMessagePluginConsole(this, "<green>StorageMechanic is loaded!");


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static StorageMechanic getInstance() {
        return instance;
    }

    public Managers getManagers() {
        return managers;
    }
}
