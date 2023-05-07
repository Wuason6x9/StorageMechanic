package dev.wuason.storagemechanic;

import dev.wuason.mechanics.utils.AdventureUtils;

public class ConfigManager {

    private StorageMechanic core;

    public ConfigManager(StorageMechanic core) {
        this.core = core;
        loadConfig();
    }

    public void loadConfig(){
        AdventureUtils.sendMessagePluginConsole(core, "<green>Loading Config...");
        core.getManagers().getCustomBlockManager().loadCustomBlocks();
        core.getManagers().getItemInterfaceManager().loadItemsInterface();
        core.getManagers().getStorageConfigManager().loadStoragesConfig();

    }


}
