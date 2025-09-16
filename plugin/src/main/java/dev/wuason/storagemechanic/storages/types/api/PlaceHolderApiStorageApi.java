package dev.wuason.storagemechanic.storages.types.api;

import dev.wuason.storagemechanic.StorageMechanic;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class PlaceHolderApiStorageApi extends PlaceholderExpansion {
    private final StorageMechanic plugin;

    public PlaceHolderApiStorageApi(StorageMechanic plugin) {
        this.plugin = plugin;
    }


    @Override
    public @NotNull String getIdentifier() {
        return "smAPI";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Wuason6x9";
    }

    @Override
    public @NotNull String getVersion() {
        return "0.1";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (params == null || params.isEmpty() || params.indexOf("_") == -1) return null;

        String param = params.substring(0, params.indexOf("_"));

        switch (param.toLowerCase(Locale.ENGLISH)) {
            case "exist" -> {
                String id = params.substring(params.indexOf("_") + 1);
                return plugin.getManagers().getStorageApiManager().existStorageApi(id) + "";
            }
        }

        return null;
    }
}
