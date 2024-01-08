package dev.wuason.storagemechanic.actions.functions.functions.inventory;

import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.compatibilities.adapter.Adapter;
import dev.wuason.storagemechanic.actions.Action;
import dev.wuason.storagemechanic.actions.functions.Function;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Locale;

public class SetItemInventory extends Function {

    public static ArrayList<String> ARGS = new ArrayList<>(){{
        add("slot".toUpperCase(Locale.ENGLISH).intern());
        add("item".toUpperCase(Locale.ENGLISH).intern());
        add("inventory".toUpperCase(Locale.ENGLISH).intern());
    }};

    public SetItemInventory() {
        super("SetItemInventory".toUpperCase(Locale.ENGLISH).intern(), ARGS);
    }

    @Override
    public void execute(Action action, Player player, Object... objects) {
        Object slotStr = objects[0];
        Object itemStr = objects[1];
        Object inventoryStr = objects[2];
        if(itemStr == null || slotStr == null) return;
        Object itemObj = null;
        Object slotObj = null;
        Object inventoryObj = null;
        if(inventoryStr != null && action.getPlaceholders().containsKey(((String) inventoryStr).toUpperCase(Locale.ENGLISH).intern())){
            inventoryObj = action.getPlaceholders().get(((String) inventoryStr).toUpperCase(Locale.ENGLISH).intern());
        }
        if(action.getPlaceholders().containsKey(((String) itemStr).toUpperCase(Locale.ENGLISH).intern())){
            itemObj = action.getPlaceholders().get(((String) itemStr).toUpperCase(Locale.ENGLISH).intern());
        }
        if(action.getPlaceholders().containsKey(((String) slotStr).toUpperCase(Locale.ENGLISH).intern())){
            slotObj = action.getPlaceholders().get(((String) slotStr).toUpperCase(Locale.ENGLISH).intern());
        }
        if(inventoryObj == null){
            inventoryObj = action.getPlaceholders().get("$currentBukkitInventoryStorage$".toUpperCase().intern());
        }
        if(itemObj == null){
            itemObj = Adapter.getInstance().getItemStack(((String) itemStr).intern());
        }
        if(slotObj == null){
            slotObj = Integer.parseInt(((String) slotStr).replace(" ", ""));
        }
        ((Inventory)inventoryObj).setItem((Integer) slotObj, (ItemStack) itemObj);
    }
}
