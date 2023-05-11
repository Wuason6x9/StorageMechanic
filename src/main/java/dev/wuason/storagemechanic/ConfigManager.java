package dev.wuason.storagemechanic;

import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.utils.AdventureUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConfigManager {

    private StorageMechanic core;
    private File configFile;
    private YamlConfiguration config;

    public ConfigManager(StorageMechanic core) {
        this.core = core;
        loadConfig();
    }

    public void loadConfig(){
        AdventureUtils.sendMessagePluginConsole(core, "<green>Loading Config...");
        core.getManagers().getCustomBlockManager().loadCustomBlocks();
        core.getManagers().getItemInterfaceManager().loadItemsInterface();
        core.getManagers().getStorageConfigManager().loadStoragesConfig();

        configFile = new File(Mechanics.getInstance().getMechanicsManager().getMechanic(core).getDirConfig().getPath() + "/config.yml");
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

    }

    public File getConfigFile() {
        return configFile;
    }

    public YamlConfiguration getConfig() {
        return config;
    }
}
