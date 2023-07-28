package dev.wuason.storagemechanic;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIConfig;
import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.mechanics.utils.MechanicsUtils;
import io.th0rgal.protectionlib.ProtectionLib;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public final class StorageMechanic extends JavaPlugin {

    private static StorageMechanic instance;
    private Managers managers;

    public StorageMechanic(){

        instance = this;

    }

    @Override
    public void onLoad() {
        AdventureUtils.sendMessagePluginConsole(this, "<green>Loading StorageMechanic...");
    }

    @Override
    public void onEnable() {

        if(!MechanicsUtils.isMechanicLoaded(this)){
            getLogger().severe("ERROR LOADING STORAGE MECHANIC!");
            getLogger().severe("You must not put the plugin in the following folder!");
            getPluginLoader().disablePlugin(this);
        }
        loadConfig();
        ProtectionLib.init(this);
        CommandAPI.onLoad(new CommandAPIConfig().silentLogs(true));
        CommandAPI.onEnable(this);


        AdventureUtils.sendMessagePluginConsole(this, "<green>Loading Managers...");
        managers = new Managers(this);
        managers.loadManagers();
        AdventureUtils.sendMessagePluginConsole(this, "<green>StorageMechanic is loaded!");


    }
    public void loadConfig(){
        File file = new File(Mechanics.getInstance().getManager().getMechanicsManager().getMechanic(this).getDirConfig() + "/config.yml");
        try {
            if(!file.exists()){
                InputStreamReader inputStreamReader = new InputStreamReader(getResource("config.yml"));
                getConfig().load(inputStreamReader);
                getConfig().save(file);
                return;
            }
            getConfig().load(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
        managers.stop();
        AdventureUtils.sendMessagePluginConsole(this,"<red> StorageMechanic Stopped!");
    }

    public static StorageMechanic getInstance() {
        return instance;
    }

    public Managers getManagers() {
        return managers;
    }
}
