package dev.wuason.storagemechanic;

import dev.wuason.mechanics.mechanics.MechanicAddon;
import dev.wuason.mechanics.utils.AdventureUtils;
import org.bukkit.Bukkit;

public final class StorageMechanic extends MechanicAddon {

    private static StorageMechanic instance;
    private Managers managers;
    private Debug debug;

    public StorageMechanic() {
        super(23027);
        instance = this;
    }

    @Override
    public void onEnable() {
        AdventureUtils.sendMessagePluginConsole(this, "<gray>-----------------------------------------------------------");
        AdventureUtils.sendMessagePluginConsole(this, "<gray>-----------------------------------------------------------");
        AdventureUtils.sendMessagePluginConsole(this, "<gold>                            StorageMechanic");
        AdventureUtils.sendMessagePluginConsole(this, "<gold>Version: <aqua>" + getDescription().getVersion());
        AdventureUtils.sendMessagePluginConsole(this, "<gold>Author: <aqua>" + getDescription().getAuthors());
        AdventureUtils.sendMessagePluginConsole(this, "<gold>Buyer id: <aqua>" + Buyer.BUYER);
        AdventureUtils.sendMessagePluginConsole(this, "<gold>Resource id: <aqua>" + Buyer.RESOURCE_ID);
        AdventureUtils.sendMessagePluginConsole(this, "<green>Loading Managers...");
        loadDebug();
        loadManager();
        AdventureUtils.sendMessagePluginConsole(this, "<green>StorageMechanic is loaded!");
        AdventureUtils.sendMessagePluginConsole(this, "<gray>-----------------------------------------------------------");
        AdventureUtils.sendMessagePluginConsole(this, "<gray>-----------------------------------------------------------");
    }

    public void loadDebug() {
        debug = new Debug();
        Bukkit.getPluginManager().registerEvents(debug, this);
    }

    public void loadManager() {
        managers = new Managers(this);
        managers.loadManagers();
    }

    @Override
    public void onDisable() {
        managers.stop();
        AdventureUtils.sendMessagePluginConsole(this, "<red> StorageMechanic Stopped!");
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
