package dev.wuason.storagemechanic.compatibilities;

import org.bukkit.Bukkit;

public class MythicMobs {
    public static boolean isMythicEnabled(){
        return Bukkit.getPluginManager().isPluginEnabled("MythicMobs");
    }
    public static boolean isExistMythic(){
        return Bukkit.getPluginManager().getPlugin("MythicMobs") != null;
    }
}
