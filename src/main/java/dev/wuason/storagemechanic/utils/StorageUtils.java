package dev.wuason.storagemechanic.utils;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class StorageUtils {


    public static void addItemToInventoryOrDrop(Player player, ItemStack itemStack) {
        HashMap<Integer, ItemStack> remainingItems = player.getInventory().addItem(itemStack);
        if (!remainingItems.isEmpty()) {
            for (ItemStack remainingItem : remainingItems.values()) {
                Location dropLocation = player.getLocation();
                Item droppedItem = dropLocation.getWorld().dropItem(dropLocation, remainingItem);
                droppedItem.setPickupDelay(40);
            }
        }
    }
}
