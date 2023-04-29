package dev.wuason.storagemechanic;

import dev.wuason.storagemechanic.blocks.CustomBlockManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

public class Managers {
    private StorageMechanic core;

    private CustomBlockManager customBlockManager;
    private ConfigManager configManager;

    public Managers(StorageMechanic core) {
        this.core = core;
        loadManagers();
    }

    public void loadManagers(){

        customBlockManager = new CustomBlockManager(core); //1
        configManager = new ConfigManager(core); //2



        PluginManager pm = Bukkit.getPluginManager();

        pm.registerEvents(customBlockManager, core);

    }

    public CustomBlockManager getCustomBlockManager() {
        return customBlockManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
