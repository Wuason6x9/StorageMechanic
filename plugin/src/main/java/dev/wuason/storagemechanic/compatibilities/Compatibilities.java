package dev.wuason.storagemechanic.compatibilities;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class Compatibilities {
    private static boolean itemsAdderLoaded = false;
    private static boolean oraxenLoaded = false;
    private static boolean mythicCrucibleLoaded = false;
    private static boolean mythicMobsLoaded = false;
    private static boolean nexoLoaded = false;
    private static boolean craftEngineLoaded = false;


    public static boolean isMythicMobsLoaded() {
        if (!mythicMobsLoaded) mythicMobsLoaded = Bukkit.getPluginManager().getPlugin("MythicMobs") != null;
        return mythicMobsLoaded;
    }

    public static boolean isItemsAdderLoaded() {
        if (!itemsAdderLoaded) itemsAdderLoaded = Bukkit.getPluginManager().getPlugin("ItemsAdder") != null;
        return itemsAdderLoaded;
    }

    public static boolean isOraxenLoaded() {
        if (!oraxenLoaded) oraxenLoaded = Bukkit.getPluginManager().getPlugin("Oraxen") != null;
        return oraxenLoaded;
    }

    public static boolean isMythicCrucibleLoaded() {
        if (!mythicCrucibleLoaded) mythicCrucibleLoaded = Bukkit.getPluginManager().getPlugin("MythicCrucible") != null;
        return mythicCrucibleLoaded;
    }

    public static boolean isNexoLoaded() {
        if (!nexoLoaded) nexoLoaded = Bukkit.getPluginManager().getPlugin("Nexo") != null;
        return nexoLoaded;
    }

    public static boolean isCraftEngineLoaded() {
        if (!craftEngineLoaded) craftEngineLoaded = Bukkit.getPluginManager().getPlugin("CraftEngine") != null;
        return craftEngineLoaded;
    }


    public static Plugin getItemsAdder() {
        return Bukkit.getPluginManager().getPlugin("ItemsAdder");
    }

    public static Plugin getOraxen() {
        return Bukkit.getPluginManager().getPlugin("Oraxen");
    }

    public static Plugin getMythicCrucible() {
        return Bukkit.getPluginManager().getPlugin("MythicCrucible");
    }

    public static Plugin getMythicMobs() {
        return Bukkit.getPluginManager().getPlugin("MythicMobs");
    }

    public static boolean isOraxenNew() {
        return getOraxen().getDescription().getVersion().startsWith("2");
    }

    public static Plugin getNexo() {
        return Bukkit.getPluginManager().getPlugin("Nexo");
    }

    public static Plugin getCraftEngine() {
        return Bukkit.getPluginManager().getPlugin("CraftEngine");
    }
}