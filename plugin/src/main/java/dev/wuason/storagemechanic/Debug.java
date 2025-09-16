package dev.wuason.storagemechanic;

import dev.wuason.mechanics.utils.AdventureUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashSet;
import java.util.Set;

public class Debug implements Listener {
    private static Set<Player> players = new HashSet<>();


    public void enableDebugMode(Player player) {
        if (players.contains(player)) return;
        players.add(player);
    }

    public void disableDebugMode(Player player) {
        if (!players.contains(player)) return;
        players.remove(player);
    }

    public static void debug(String string) {
        if (players.size() > 0) {
            System.out.println("[SM][DEBUG] ->" + string);
        }
    }

    public static void debugToPlayers(String s) {
        players.forEach(player -> {
            AdventureUtils.playerMessage(s, player);
        });
    }

    public static void debugToPlayer(String s, Player player) {
        if (players.contains(player)) {
            AdventureUtils.playerMessage(s, player);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        Player player = (Player) e.getWhoClicked();
        if (!players.contains(player)) return;
        debugToPlayer("InventoryType: " + e.getClickedInventory().getType(), player);
        debugToPlayer("Slot: " + e.getSlot(), player);
        debugToPlayer("SlotType: " + e.getSlotType(), player);
        debugToPlayer("SlotRaw: " + e.getRawSlot(), player);
        debugToPlayer("Holder: " + e.getInventory().getHolder(), player);
    }
}
