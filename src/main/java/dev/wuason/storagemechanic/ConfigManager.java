package dev.wuason.storagemechanic;

import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.storagemechanic.compatibilities.ItemsAdderEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ConfigManager {

    private StorageMechanic core;
    private File configFile;
    private YamlConfiguration config;
    private volatile boolean isConfigLoaded = false;
    private final Object lock = new Object();

    public ConfigManager(StorageMechanic core) {
        this.core = core;
    }
    public void load(){
        isConfigLoaded = false;
        while (!waitDependencies()){
        }
        Plugin plugin = Bukkit.getPluginManager().getPlugin("ItemsAdder");
        if(plugin != null && plugin.isEnabled()){
            ItemsAdderEvent itemsAdderEvent = new ItemsAdderEvent();
            Bukkit.getPluginManager().registerEvents(itemsAdderEvent,core);
            return;
        }
        loadConfig();
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

    public void loadConfig(){
        AdventureUtils.sendMessagePluginConsole(core, "<green>Loading Config...");
        try {
            core.getConfig().load(Mechanics.getInstance().getManager().getMechanicsManager().getMechanic(core).getDirConfig() + "/config.yml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
        core.getManagers().getCustomBlockManager().loadCustomBlocks();
        core.getManagers().getActionConfigManager().loadActions();
        core.getManagers().getItemInterfaceManager().loadItemsInterface();
        core.getManagers().getStorageConfigManager().loadStoragesConfig();
        core.getManagers().getBlockStorageConfigManager().loadBlockStorageConfigs();
        core.getManagers().getItemStorageConfigManager().loadItemStorageConfigs();
        core.getManagers().getFurnitureStorageConfigManager().loadFurnitureStorageConfigs();
        core.getManagers().getInventoryConfigManager().loadInventoriesConfig();

        configFile = new File(Mechanics.getInstance().getManager().getMechanicsManager().getMechanic(core).getDirConfig().getPath() + "/config.yml");
        if(!configFile.exists()){
            configFile.getParentFile().mkdirs();
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                YamlConfiguration.loadConfiguration(new InputStreamReader(core.getResource("config.yml"))).save(configFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        config = YamlConfiguration.loadConfiguration(configFile);
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

    public File getConfigFile() {
        return configFile;
    }

    public YamlConfiguration getConfig() {
        return config;
    }
}
