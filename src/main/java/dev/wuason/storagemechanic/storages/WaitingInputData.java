package dev.wuason.storagemechanic.storages;

import dev.wuason.storagemechanic.items.ItemInterface;
import org.bukkit.Location;

public class WaitingInputData {
    private final Storage storage;
    private final int currentPage;
    private final Location location;
    private final ItemInterface itemInterface;

    public WaitingInputData(Storage storage, int currentPage, Location location, ItemInterface itemInterface) {
        this.storage = storage;
        this.currentPage = currentPage;
        this.location = location;
        this.itemInterface = itemInterface;
    }

    public Storage getStorage() {
        return storage;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public Location getLocation() {
        return location;
    }

    public ItemInterface getItemInterface() {
        return itemInterface;
    }
}
