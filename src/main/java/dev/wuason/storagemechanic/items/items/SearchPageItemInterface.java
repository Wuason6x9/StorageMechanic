package dev.wuason.storagemechanic.items.items;

import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.items.ItemInterface;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.StorageManager;
import dev.wuason.storagemechanic.storages.WaitingInputData;
import dev.wuason.storagemechanic.storages.config.StorageConfig;
import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class SearchPageItemInterface extends ItemInterface {

    private Map<UUID, WaitingInputData> waitingInput = new ConcurrentHashMap<>();
    private final StorageMechanic core;
    private final double maxDistance;

    public SearchPageItemInterface(String item, String displayName, List<String> lore, double maxDistance, String id) {
        super(item, displayName, lore, id, "SEARCH_PAGE");
        this.core = StorageMechanic.getInstance();
        this.maxDistance = maxDistance;
    }

    @Override
    public void onClick(Storage storage, StorageInventory storageInventory, InventoryClickEvent event, StorageConfig storageConfig, StorageManager storageManager) {
        Player player = (Player) event.getWhoClicked();
        player.closeInventory();
        player.sendMessage(AdventureUtils.deserializeLegacy(core.getManagers().getConfigManager().getLangDocumentYaml().getString("messages.storage.search_page_enter"),null));
        UUID playerId = player.getUniqueId();
        waitingInput.put(playerId, new WaitingInputData(storage, storageInventory.getPage(), player.getLocation(), this));
        AtomicLong timer = new AtomicLong(0);
        Bukkit.getScheduler().runTaskTimerAsynchronously(core, task -> {
            if (timer.incrementAndGet() >= 20 * 10 || !waitingInput.containsKey(playerId)) {
                waitingInput.remove(playerId);
                task.cancel();
            }
        }, 0L, 3L);
    }

    public double getMaxDistance() {
        return maxDistance;
    }
}
