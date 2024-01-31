package dev.wuason.storagemechanic.utils;

import dev.wuason.mechanics.utils.Utils;
import org.apache.commons.lang3.Range;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


import java.util.*;

public class StorageUtils {

    public static boolean isValidConfigId(String id){
        if(!id.contains("_")) return true;
        return false;
    }
    public static boolean canNotBuild(Player player, Block block) {
        if (player == null) return false;
        final Location bLoc = (new Location(block.getLocation().getWorld(), block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ())).clone().add(0.5, 0.5, 0.5);
        final Location pLoc = player.getLocation();
        return Range.between(0.5, 1.5).contains(bLoc.getY() - pLoc.getY()) && Range.between(-0.80, 0.80).contains(bLoc.getX() - pLoc.getX()) && Range.between(-0.80, 0.80).contains(bLoc.getZ() - pLoc.getZ());
    }

    @Deprecated
    public static ArrayList<Integer> configFill(List<String> arrayList){

        ArrayList<Integer> arrayListNumbers = new ArrayList<>();

        for(String number : arrayList){

            if(!number.contains("-")) {
                if (Utils.isNumber(number)) {
                    arrayListNumbers.add(Integer.parseInt(number));
                }
                continue;
            }

            String numbers[] = number.split("-");

            if(numbers.length>0){
                for(String n : numbers){
                    if(!Utils.isNumber(n)) continue;
                }
                for(int i=Integer.parseInt(numbers[0]);i<Integer.parseInt(numbers[1])+1;i++){
                    arrayListNumbers.add(i);
                }
            }
        }

        return arrayListNumbers;

    }


    /**
     * Adds an item to the player's inventory. If the inventory is full, the item will be dropped
     * at the player's location.
     *
     * @param player     The player whose inventory the item will be added to.
     * @param itemStack  The item to be added to the inventory.
     *
     * @deprecated This method is deprecated and will be removed in a future release. Use {@link dev.wuason.mechanics.utils.StorageUtils#addItemToInventoryOrDrop(Player, ItemStack)}
     * methods directly instead.
     */
    @Deprecated
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

    public static String getStoragePhysicalId(Location loc){
        return loc.getWorld().getUID() + "_" + loc.getBlockX() + "_" + loc.getY() + "_" + loc.getBlockZ();
    }
    public static Location getStoragePhysicalLocation(String id){
        String[] loc = id.split("_");
        return new Location(Bukkit.getWorld(UUID.fromString(loc[0])),Double.parseDouble(loc[1]),Double.parseDouble(loc[2]),Double.parseDouble(loc[3]));
    }
}
