package dev.wuason.storagemechanic.items.properties;

import dev.wuason.storagemechanic.StorageMechanic;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.List;

public class PlaceHolderItemProperties extends Properties {

    private boolean whitelistEnabled;
    private boolean blacklistEnabled;
    private List<String> whitelistItems;
    private List<String> blacklistItems;
    public final static NamespacedKey NAMESPACED_KEY = new NamespacedKey(StorageMechanic.getInstance(),"smplaceholderitem");
    public final static NamespacedKey NAMESPACED_KEY_INTERFACE = new NamespacedKey(StorageMechanic.getInstance(),"smplaceholderiteminterface");

    public PlaceHolderItemProperties(boolean whitelistEnabled, boolean blacklistEnabled, List<String> whitelistItems, List<String> blacklistItems) {
        this.whitelistEnabled = whitelistEnabled;
        this.blacklistEnabled = blacklistEnabled;
        this.whitelistItems = whitelistItems;
        this.blacklistItems = blacklistItems;
    }

    public boolean isWhitelistEnabled() {
        return whitelistEnabled;
    }

    public boolean isBlacklistEnabled() {
        return blacklistEnabled;
    }

    public List<String> getWhitelistItems() {
        return whitelistItems;
    }

    public List<String> getBlacklistItems() {
        return blacklistItems;
    }
}
