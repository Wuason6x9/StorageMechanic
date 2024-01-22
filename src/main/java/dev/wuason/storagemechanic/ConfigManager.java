package dev.wuason.storagemechanic;

import dev.wuason.libs.boostedyaml.YamlDocument;
import dev.wuason.libs.boostedyaml.dvs.versioning.BasicVersioning;
import dev.wuason.libs.boostedyaml.settings.dumper.DumperSettings;
import dev.wuason.libs.boostedyaml.settings.general.GeneralSettings;
import dev.wuason.libs.boostedyaml.settings.loader.LoaderSettings;
import dev.wuason.libs.boostedyaml.settings.updater.UpdaterSettings;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.storagemechanic.compatibilities.ItemsAdderEvent;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class ConfigManager {

    private StorageMechanic core;
    private volatile boolean isConfigLoaded = false;
    private final Object lock = new Object();
    private YamlDocument langDocumentYaml;
    private YamlDocument mainConfig;

    public ConfigManager(StorageMechanic core) {
        this.core = core;
    }
    public void loadAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(core, () -> {
            isConfigLoaded = false;
            while (!waitDependencies()){
            }
            Plugin plugin = Bukkit.getPluginManager().getPlugin("ItemsAdder");
            if(plugin != null && plugin.isEnabled()){
                ItemsAdderEvent itemsAdderEvent = new ItemsAdderEvent();
                Bukkit.getPluginManager().registerEvents(itemsAdderEvent,core);
                return;
            }

            try {
                loadConfig();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    public void load() {
        try {
            loadMainConfig();
            loadLang();
            loadAsync();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMainConfig() throws IOException {
        File file = new File(core.getDataFolder().getPath() + "/config.yml");
        mainConfig = YamlDocument.create(file, core.getResource("config.yml"), GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build());
    }

    public void loadLang() throws IOException {
        File base = new File(core.getDataFolder().getPath() + "/lang/");
        base.mkdirs();
        String selectedLang = mainConfig.getString("config.lang", "en").toLowerCase(Locale.ENGLISH);
        File file = new File(base.getPath() + "/" + selectedLang + ".yml");
        if(core.getResource("lang/"+ selectedLang +".yml") == null && !file.exists()) selectedLang = "en";
        YamlDocument config = YamlDocument.create(file, core.getResource("lang/"+ selectedLang +".yml"), GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build());
        langDocumentYaml = config;
    }

    public boolean waitDependencies(){
        boolean a = true;
        ArrayList<Plugin> plugins = new ArrayList<>();
        PluginManager pluginManager = Bukkit.getPluginManager();

        for(String p : core.getDescription().getSoftDepend()){
            Plugin pl = pluginManager.getPlugin(p);
            if(pl != null){
                plugins.add(pl);
            }
        }

        for(Plugin plugin : plugins){
            if(!plugin.isEnabled()){
                a = false;
            }
        }

        return a;
    }

    public void loadConfig() throws IOException {
        AdventureUtils.sendMessagePluginConsole(core, "<green>Loading Config...");
        mainConfig.reload();
        core.getManagers().stopAndRemoveDataSaveTask();
        core.getManagers().runDataSaveTask();
        langDocumentYaml.reload();
        core.getManagers().getCustomItemsManager().loadCustomBlocks();
        core.getManagers().getActionManager().getActionConfigManager().loadActions(new File(core.getDataFolder() + "/actions/"));
        AdventureUtils.sendMessagePluginConsole(core, "<aqua> Actions loaded: <yellow>" + core.getManagers().getActionManager().getActionConfigs().size());
        core.getManagers().getItemInterfaceManager().loadItemsInterface();
        core.getManagers().getStorageConfigManager().loadStoragesConfig();
        core.getManagers().getBlockStorageConfigManager().loadBlockStorageConfigs();
        core.getManagers().getItemStorageConfigManager().loadItemStorageConfigs();
        core.getManagers().getFurnitureStorageConfigManager().loadFurnitureStorageConfigs();
        core.getManagers().getInventoryConfigManager().loadInventoriesConfig();
        core.getManagers().getInventoryConfigManager1().load();
        AdventureUtils.sendMessagePluginConsole(core, "<aqua> Inventories1 loaded: <yellow>" + core.getManagers().getInventoryConfigManager1().getInventories().size());
        core.getManagers().getRecipesManager().loadRecipes();
        setConfigLoaded(true);
    }

    public boolean isConfigLoaded() {
        synchronized (lock) {
            while (!isConfigLoaded) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    // manejar la excepción
                    Thread.currentThread().interrupt();
                }
            }
        }
        return isConfigLoaded;
    }

    public void setConfigLoaded(boolean isConfigLoaded) {
        synchronized (lock) {
            this.isConfigLoaded = isConfigLoaded;
            lock.notifyAll();
        }
    }

    public YamlDocument getLangDocumentYaml() {
        return langDocumentYaml;

    }

    public YamlDocument getMainConfig() {
        return mainConfig;
    }
}
