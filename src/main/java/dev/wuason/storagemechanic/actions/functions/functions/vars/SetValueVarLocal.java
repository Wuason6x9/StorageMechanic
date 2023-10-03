package dev.wuason.storagemechanic.actions.functions.functions.vars;

import dev.wuason.storagemechanic.actions.Action;
import dev.wuason.storagemechanic.actions.functions.Function;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Locale;

public class SetValueVarLocal extends Function {
    public static ArrayList<String> ARGS = new ArrayList<>(){{
        add("var".toUpperCase(Locale.ENGLISH).intern());
        add("value".toUpperCase(Locale.ENGLISH).intern());
    }};
    public SetValueVarLocal() {
        super("setValueLocalVar".toUpperCase(Locale.ENGLISH).intern(), ARGS);
    }

    @Override
    public void execute(Action action, Player player, Object... objects) {
        String var = ((String) objects[0]).replace(" ", "").toUpperCase(Locale.ENGLISH).intern();
        String objString = ((String) objects[1]).trim();
        Object obj = null;
        if(action.getPlaceholders().containsKey(objString)) obj = action.getPlaceholders().get(objString);
        if(obj == null) obj = objString.intern();
        action.getPlaceholders().put(var, obj);
    }
}
