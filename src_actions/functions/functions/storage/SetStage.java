package dev.wuason.storagemechanic.actions.functions.functions.storage;

import dev.wuason.mechanics.Mechanics;
import dev.wuason.storagemechanic.actions.Action;
import dev.wuason.storagemechanic.actions.functions.Function;
import dev.wuason.storagemechanic.storages.Storage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Locale;

public class SetStage extends Function {

    public static ArrayList<String> ARGS = new ArrayList<>(){{
        add("stage".toUpperCase(Locale.ENGLISH).intern());
        add("page".toUpperCase(Locale.ENGLISH).intern());
    }};

    public SetStage() {
        super("SetStage".toUpperCase(Locale.ENGLISH).intern(), ARGS);
    }

    @Override
    public void execute(Action action, Player player, Object... objects) {
        Object stageStr = objects[0];
        Object pageStr = objects[1];
        if(stageStr == null) return;
        if(pageStr == null) pageStr = "" + ((Integer)action.getPlaceholders().get("$currentPage$".toUpperCase().intern()));
        Object stageObj = null;
        Object pageObj = null;
        if(action.getPlaceholders().containsKey(((String) stageStr).toUpperCase(Locale.ENGLISH).intern())){
            stageObj = action.getPlaceholders().get(((String) stageStr).toUpperCase(Locale.ENGLISH).intern());
        }
        if(action.getPlaceholders().containsKey(((String) pageStr).toUpperCase(Locale.ENGLISH).intern())){
            pageObj = action.getPlaceholders().get(((String) pageStr).toUpperCase(Locale.ENGLISH).intern());
        }
        if(stageObj == null){
            stageObj = ((String) stageStr).intern();
        }
        if(pageObj == null){
            pageObj = Integer.parseInt(((String) pageStr).replace(" ", ""));
        }
        Storage storage = (Storage) action.getPlaceholders().get("$STORAGE$");
        storage.setStage((String) stageObj, (Integer) pageObj);
    }
}
