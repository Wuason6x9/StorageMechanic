package dev.wuason.storagemechanic;

import dev.wuason.boostedyaml.YamlDocument;
import dev.wuason.boostedyaml.dvs.versioning.BasicVersioning;
import dev.wuason.boostedyaml.settings.dumper.DumperSettings;
import dev.wuason.boostedyaml.settings.general.GeneralSettings;
import dev.wuason.boostedyaml.settings.loader.LoaderSettings;
import dev.wuason.boostedyaml.settings.updater.UpdaterSettings;
import dev.wuason.fastinv.FastInvManager;
import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.mechanics.utils.MechanicsUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public final class StorageMechanic extends JavaPlugin {

    private static StorageMechanic instance;
    private Managers managers;
    private Debug debug;
    private YamlDocument yamlDocument;
    private String a = "%%__USER__%%";

    public StorageMechanic(){

        instance = this;

    }

    @Override
    public void onLoad() {
        if(!Bukkit.getPluginManager().isPluginEnabled("Mechanics")){
            getLogger().severe("-------------------------------------------------------------------------");
            getLogger().severe("-------------------------------------------------------------------------");
            getLogger().severe(" ");
            getLogger().severe(" ");
            getLogger().severe("ERROR LOADING STORAGE MECHANIC!");
            getLogger().severe("You must not put the plugin in the following folder! put in plugins/Mechanics/mechanics");
            getLogger().severe(" ");
            getLogger().severe(" ");
            getLogger().severe("-------------------------------------------------------------------------");
            getLogger().severe("-------------------------------------------------------------------------");
            getPluginLoader().disablePlugin(this);
            return;
        }
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
        FastInvManager.register(this);

        AdventureUtils.sendMessagePluginConsole(this, "<green>Loading Managers...");
        debug = new Debug();
        Bukkit.getPluginManager().registerEvents(debug,this);
        managers = new Managers(this);
        managers.loadManagers();
        AdventureUtils.sendMessagePluginConsole(this, "<green>StorageMechanic is loaded!");


    }
    public void loadConfig(){
        try {
            File file = new File(Mechanics.getInstance().getManager().getMechanicsManager().getMechanic(this).getDirConfig() + "/config.yml");
            YamlDocument config = YamlDocument.create(file, getResource("config.yml"), GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build());
            yamlDocument = config;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public YamlDocument getConfigDocumentYaml() {
        return yamlDocument;
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

    public Debug getDebug() {
        return debug;
    }
}
