package dev.wuason.storagemechanic.actions.functions.functions.vars;

import dev.wuason.storagemechanic.actions.Action;
import dev.wuason.storagemechanic.actions.functions.Function;
import dev.wuason.storagemechanic.storages.Storage;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Locale;

public class SetValueGlobalVar extends Function {
    public static ArrayList<String> ARGS = new ArrayList<>(){{
        add("var".toUpperCase(Locale.ENGLISH).intern());
        add("value".toUpperCase(Locale.ENGLISH).intern());
    }};
    public SetValueGlobalVar() {
        super("setValueGlobalVar".toUpperCase(Locale.ENGLISH).intern(), ARGS);
    }

    @Override
    public void execute(Action action, Player player, Object... objects) {
        String var = ((String) objects[0]).replace(" ", "").toUpperCase(Locale.ENGLISH).intern();
        String objString = ((String) objects[1]).trim().intern();
        Object obj = null;
        if(action.getPlaceholders().containsKey(objString)) obj = action.getPlaceholders().get(objString);
        if(obj == null) obj = objString.intern();
        action.getActionManager().setValueGlobalVar(((Storage)action.getPlaceholders().get("$STORAGE$")).getId(),var,obj);
    }
}
