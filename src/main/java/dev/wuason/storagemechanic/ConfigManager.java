package dev.wuason.storagemechanic;

public class ConfigManager {

    private StorageMechanic core;

    public ConfigManager(StorageMechanic core) {
        this.core = core;
        loadConfig();
    }

    public void loadConfig(){

        core.getManagers().getCustomBlockManager().loadCustomBlocks();
        core.getManagers().getItemInterfaceManager().loadItemsInterface();

    }


}
