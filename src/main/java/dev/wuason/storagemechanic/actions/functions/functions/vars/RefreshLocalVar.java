package dev.wuason.storagemechanic.actions.functions.functions.vars;

import dev.wuason.libs.bsh.EvalError;
import dev.wuason.storagemechanic.actions.Action;
import dev.wuason.storagemechanic.actions.functions.Function;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Locale;

public class RefreshLocalVar extends Function {

    public static ArrayList<String> ARGS = new ArrayList<>(){{
        add("var".toUpperCase(Locale.ENGLISH).intern());
    }};

    public RefreshLocalVar() {
        super("RefreshLocalVar".toUpperCase(Locale.ENGLISH).intern(), ARGS);
    }

    @Override
    public void execute(Action action, Player player, Object... objects) {
        String var = (String) objects[0];
        Object obj = null;
        try {
            obj = action.getInterpreter().get(var);
        } catch (EvalError e) {
            throw new RuntimeException(e);
        }
        action.getPlaceholders().put(var, obj);
    }
}
