package dev.wuason.storagemechanic.actions.functions.functions.storage;

import dev.wuason.storagemechanic.actions.Action;
import dev.wuason.storagemechanic.actions.functions.Function;
import dev.wuason.storagemechanic.storages.Storage;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Locale;

public class ClearSlotPageStorage extends Function {

    public static ArrayList<String> ARGS = new ArrayList<>(){{
        add("slot".toUpperCase(Locale.ENGLISH).intern());
        add("page".toUpperCase(Locale.ENGLISH).intern());
    }};

    public ClearSlotPageStorage() {
        super("ClearSlotPageStorage".toUpperCase(Locale.ENGLISH).intern(), ARGS);
    }

    @Override
    public void execute(Action action, Player player, Object... objects) {
        Object slotStr = objects[0];
        Object pageStr = objects[1];
        if(slotStr == null) return;
        if(pageStr == null) pageStr = "" + ((Integer)action.getPlaceholders().get("$currentPage$".toUpperCase().intern()));
        Object pageObj = null;
        Object slotObj = null;

        if(action.getPlaceholders().containsKey(((String) slotStr).toUpperCase(Locale.ENGLISH).intern())){
            slotObj = action.getPlaceholders().get(((String) slotStr).toUpperCase(Locale.ENGLISH).intern());
        }
        if(action.getPlaceholders().containsKey(((String) pageStr).toUpperCase(Locale.ENGLISH).intern())){
            pageObj = action.getPlaceholders().get(((String) pageStr).toUpperCase(Locale.ENGLISH).intern());
        }

        if(slotObj == null){
            slotObj = Integer.parseInt(((String) pageStr).replace(" ", ""));
        }
        if(pageObj == null){
            pageObj = Integer.parseInt(((String) pageStr).replace(" ", ""));
        }
        Storage storage = (Storage) action.getPlaceholders().get("$STORAGE$");
        storage.clearSlotPage((Integer) pageObj, (Integer) slotObj);
    }
}
