package dev.wuason.storagemechanic.actions.functions.functions.inventory;

import dev.wuason.mechanics.Mechanics;
import dev.wuason.storagemechanic.actions.Action;
import dev.wuason.storagemechanic.actions.functions.Function;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Locale;

public class ClearSlotInventory extends Function {

    public static ArrayList<String> ARGS = new ArrayList<>(){{
        add("slot".toUpperCase(Locale.ENGLISH).intern());
        add("inventory".toUpperCase(Locale.ENGLISH).intern());
    }};

    public ClearSlotInventory() {
        super("ClearSlotInventory".toUpperCase(Locale.ENGLISH).intern(), ARGS);
    }

    @Override
    public void execute(Action action, Player player, Object... objects) {
        Object slotStr = objects[0];
        Object inventoryStr = objects[1];
        if(slotStr == null) return;
        Object slotObj = null;
        Object inventoryObj = null;
        if(action.getPlaceholders().containsKey(((String) inventoryStr).toUpperCase(Locale.ENGLISH).intern())){
            inventoryObj = action.getPlaceholders().get(((String) inventoryStr).toUpperCase(Locale.ENGLISH).intern());
        }
        if(action.getPlaceholders().containsKey(((String) slotStr).toUpperCase(Locale.ENGLISH).intern())){
            slotObj = action.getPlaceholders().get(((String) slotStr).toUpperCase(Locale.ENGLISH).intern());
        }
        if(inventoryObj == null){
            inventoryObj = action.getPlaceholders().get("$currentBukkitInventoryStorage$".toUpperCase().intern());
        }
        if(slotObj == null){
            slotObj = Integer.parseInt(((String) slotStr).replace(" ", ""));
        }
        ((Inventory)inventoryObj).clear((Integer) slotObj);
    }
}
