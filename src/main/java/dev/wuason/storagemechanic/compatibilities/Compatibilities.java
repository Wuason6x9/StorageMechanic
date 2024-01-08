package dev.wuason.storagemechanic.compatibilities;

import org.bukkit.Bukkit;

public class Compatibilities {
    private static boolean itemsAdderLoaded = false;
    private static boolean oraxenLoaded = false;
    private static boolean mythicCrucibleLoaded = false;
    private static boolean mythicMobsLoaded = false;


    public static boolean isMythicMobsLoaded(){
        if(!mythicMobsLoaded) mythicMobsLoaded = Bukkit.getPluginManager().getPlugin("MythicMobs") != null;
        return mythicMobsLoaded;
    }

    public static boolean isItemsAdderLoaded(){
        if(!itemsAdderLoaded) itemsAdderLoaded = Bukkit.getPluginManager().getPlugin("ItemsAdder") != null;
        return itemsAdderLoaded;
    }
    public static boolean isOraxenLoaded(){
        if(!oraxenLoaded) oraxenLoaded = Bukkit.getPluginManager().getPlugin("Oraxen") != null;
        return oraxenLoaded;
    }
    public static boolean isMythicCrucibleLoaded(){
        if(!mythicCrucibleLoaded) mythicCrucibleLoaded = Bukkit.getPluginManager().getPlugin("MythicCrucible") != null;
        return mythicCrucibleLoaded;
    }
}
