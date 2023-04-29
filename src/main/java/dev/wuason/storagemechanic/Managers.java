package dev.wuason.storagemechanic;

import dev.wuason.storagemechanic.customblocks.CustomBlockManager;
import dev.wuason.storagemechanic.items.ItemInterface;
import dev.wuason.storagemechanic.items.ItemInterfaceManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

public class Managers {
    private StorageMechanic core;

    private CustomBlockManager customBlockManager;
    private ItemInterfaceManager itemInterfaceManager;
    private ConfigManager configManager;

    public Managers(StorageMechanic core) {
        this.core = core;
        loadManagers();
    }

    public void loadManagers(){

        customBlockManager = new CustomBlockManager(core); //1
        itemInterfaceManager = new ItemInterfaceManager(core);//2
        configManager = new ConfigManager(core); //



        PluginManager pm = Bukkit.getPluginManager();

        pm.registerEvents(customBlockManager, core);

    }

    public CustomBlockManager getCustomBlockManager() {
        return customBlockManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ItemInterfaceManager getItemInterfaceManager() {
        return itemInterfaceManager;
    }
}
