package dev.wuason.storagemechanic.items.items;

import dev.wuason.mechanics.utils.Utils;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.items.ItemInterface;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.storages.config.StorageConfig;
import dev.wuason.storagemechanic.storages.inventory.StorageInventory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.Map;

public class CommandItemInterface extends ItemInterface {

    private final String command;
    private final boolean asConsole;
    private final boolean asOp;
    private final long delay;

    public CommandItemInterface(String item, String displayName, List<String> lore, String id, String command, boolean asConsole, boolean asOp, long delay) {
        super(item, displayName, lore, id, "COMMAND_ITEM");
        this.command = command;
        this.asConsole = asConsole;
        this.asOp = asOp;
        this.delay = delay;
    }

    @Override
    public void onClick(Storage storage, StorageInventory storageInventory, InventoryClickEvent event, StorageConfig storageConfig) {
        if (event.getWhoClicked() instanceof Player player) {
            if (delay > 0) {
                Bukkit.getScheduler().runTaskLater(StorageMechanic.getInstance(), () -> executeCommand(player), delay);
            } else {
                executeCommand(player);
            }
        }
    }

    public String getCommand() {
        return command;
    }

    public boolean isAsConsole() {
        return asConsole;
    }

    public boolean isAsOp() {
        return asOp;
    }

    public long getDelay() {
        return delay;
    }

    private void executeCommand(Player player) {
        String command = Utils.replaceVariablesInsensitive(this.command, Map.of(
                "%PLAYER_NAME%", player.getName(),
                "%PLAYER_UUID%", player.getUniqueId().toString()
        ));
        boolean isOp = player.isOp();
        if (asOp) {
            player.setOp(true);
        }
        if (asConsole) {
            player.getServer().dispatchCommand(player.getServer().getConsoleSender(), command);
        } else {
            player.performCommand(command);
        }
        if (asOp && !isOp) {
            player.setOp(false);
        }
    }
}
