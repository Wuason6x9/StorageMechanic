package dev.wuason.storagemechanic.utils;

import dev.wuason.mechanics.utils.Utils;
import dev.wuason.storagemechanic.StorageMechanic;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

public class StorageUtils {

    public static boolean isValidConfigId(String id){
        if(!id.contains("_")) return true;
        return false;
    }

    public static String serializeObject(Object ob) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1);
        BukkitObjectOutputStream bukkitObjectOutputStream = new BukkitObjectOutputStream(byteArrayOutputStream);
        bukkitObjectOutputStream.writeObject(ob);
        bukkitObjectOutputStream.flush();
        bukkitObjectOutputStream.close();
        return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
    }
    public static Object deserializeObject(String data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
        BukkitObjectInputStream objectInputStream = new BukkitObjectInputStream(byteArrayInputStream);
        return objectInputStream.readObject();
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
        return loc.getWorld().getUID() + "_" + loc.getBlockX() + "_" + loc.getBlockX() + "_" + loc.getBlockZ();
    }
    public static Location getBlockStorageLocation(String id){
        String[] loc = id.split("_");
        return new Location(Bukkit.getWorld(loc[0]),Double.parseDouble(loc[1]),Double.parseDouble(loc[2]),Double.parseDouble(loc[3]));
    }
}
