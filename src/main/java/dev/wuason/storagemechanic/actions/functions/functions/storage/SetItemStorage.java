package dev.wuason.storagemechanic.actions.functions.functions.storage;

import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.compatibilities.adapter.Adapter;
import dev.wuason.storagemechanic.actions.Action;
import dev.wuason.storagemechanic.actions.functions.Function;
import dev.wuason.storagemechanic.storages.Storage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Locale;

public class SetItemStorage extends Function {

    public static ArrayList<String> ARGS = new ArrayList<>(){{
        add("item".toUpperCase(Locale.ENGLISH).intern());
        add("slot".toUpperCase(Locale.ENGLISH).intern());
        add("page".toUpperCase(Locale.ENGLISH).intern());
    }};

    public SetItemStorage() {
        super("SetItemStorage".toUpperCase(Locale.ENGLISH).intern(), ARGS);
    }

    @Override
    public void execute(Action action, Player player, Object... objects) {
        Object itemStr = objects[0];
        Object slotStr = objects[1];
        Object pageStr = objects[2];
        if(itemStr == null || slotStr == null) return;
        if(pageStr == null) pageStr = "" + ((Integer)action.getPlaceholders().get("$currentPage$".toUpperCase().intern()));
        Object itemObj = null;
        Object slotObj = null;
        Object pageObj = null;
        if(action.getPlaceholders().containsKey(((String) itemStr).toUpperCase(Locale.ENGLISH).intern())){
            itemObj = action.getPlaceholders().get(((String) itemStr).toUpperCase(Locale.ENGLISH).intern());
        }
        if(action.getPlaceholders().containsKey(((String) slotStr).toUpperCase(Locale.ENGLISH).intern())){
            slotObj = action.getPlaceholders().get(((String) slotStr).toUpperCase(Locale.ENGLISH).intern());
        }
        if(action.getPlaceholders().containsKey(((String) pageStr).toUpperCase(Locale.ENGLISH).intern())){
            pageObj = action.getPlaceholders().get(((String) pageStr).toUpperCase(Locale.ENGLISH).intern());
        }
        if(itemObj == null){
            itemObj = Adapter.getInstance().getItemStack(((String) itemStr).intern());
        }
        if(slotObj == null){
            slotObj = Integer.parseInt(((String) slotStr).replace(" ", ""));
        }
        if(pageObj == null){
            pageObj = Integer.parseInt(((String) pageStr).replace(" ", ""));
        }
        Storage storage = (Storage) action.getPlaceholders().get("$STORAGE$");
        storage.setItemInSlotPage((Integer)pageObj, (Integer)slotObj, (ItemStack)itemObj);
    }
}
