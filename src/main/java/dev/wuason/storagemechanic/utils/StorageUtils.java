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
    public static int randomNumberString(String numbers){
        if(numbers == null) return -1;
        if(!numbers.contains("-")) return Integer.parseInt(numbers);
        String[] nString = numbers.split("-");
        if(nString.length < 2) return Integer.parseInt(nString[0]);
        if(nString[1] == null || nString[0] == null) return 64;
        int min = Integer.parseInt(nString[0]);
        int max = Integer.parseInt(nString[1]);
        return randomNumber(min,max);
    }
    public static int randomNumber(int min, int max){
        return (int) (min + Math.round(Math.random() * (max - min)));
    }
    public static boolean chance(float probability) {
        if (probability < 0.0f || probability > 100.0f) {
            throw new IllegalArgumentException("ERROR CHANCE!!!!!!!!!!");
        }
        float randomValue = new Random().nextFloat() * 100.0f;
        return randomValue < probability;
    }
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

    public static String getBlockStorageId(Location loc){
        return loc.getWorld().getUID() + "_" + loc.getBlockX() + "_" + loc.getY() + "_" + loc.getBlockZ();
    }
    public static Location getBlockStorageLocation(String id){
        String[] loc = id.split("_");
        return new Location(Bukkit.getWorld(UUID.fromString(loc[0])),Double.parseDouble(loc[1]),Double.parseDouble(loc[2]),Double.parseDouble(loc[3]));
    }
}
