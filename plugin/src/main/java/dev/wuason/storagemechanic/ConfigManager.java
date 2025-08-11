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

    public void load() {
        try {
            loadMainConfig();
            loadLang();
            loadConfig();

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

    public void loadConfig() throws IOException {
        AdventureUtils.sendMessagePluginConsole(core, "<green>Loading Config...");
        mainConfig.reload();
        core.getManagers().stopAndRemoveDataSaveTask();
        core.getManagers().runDataSaveTask();
        langDocumentYaml.reload();
        core.getManagers().getCustomItemsManager().loadCustomBlocks();
        core.getManagers().getActionManager().getActionConfigManager().loadActions(new File(core.getDataFolder() + "/Actions/"));
        AdventureUtils.sendMessagePluginConsole(core, "<aqua> Actions loaded: <yellow>" + core.getManagers().getActionManager().getActionConfigs().size());
        core.getManagers().getItemInterfaceManager().loadItemsInterface();
        core.getManagers().getStorageConfigManager().loadStoragesConfig();
        core.getManagers().getBlockStorageConfigManager().loadBlockStorageConfigs();
        core.getManagers().getItemStorageConfigManager().loadItemStorageConfigs();
        core.getManagers().getFurnitureStorageConfigManager().loadFurnitureStorageConfigs();
        core.getManagers().getInventoryConfigManager().load();
        AdventureUtils.sendMessagePluginConsole(core, "<aqua> Inventories loaded: <yellow>" + core.getManagers().getInventoryConfigManager().getInventories().size());
        Bukkit.getScheduler().runTask(core, () -> core.getManagers().getRecipesManager().loadRecipes());
    }

    public YamlDocument getLangDocumentYaml() {
        return langDocumentYaml;
    }

    public YamlDocument getMainConfig() {
        return mainConfig;
    }
}
