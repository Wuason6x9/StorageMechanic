package dev.wuason.storagemechanic.actions.functions.functions.vars;

import dev.wuason.storagemechanic.actions.Action;
import dev.wuason.storagemechanic.actions.functions.Function;
import dev.wuason.storagemechanic.storages.Storage;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Locale;

public class RemoveGlobalVar extends Function {
    public static ArrayList<String> ARGS = new ArrayList<>(){{
        add("var".toUpperCase(Locale.ENGLISH).intern());
    }};
    public RemoveGlobalVar() {
        super("removeGlobalVar".toUpperCase(Locale.ENGLISH).intern(), ARGS);
    }

    @Override
    public void execute(Action action, Player player, Object... objects) {
        String var = ((String) objects[0]).replace(" ", "").toUpperCase(Locale.ENGLISH).intern();
        action.getActionManager().removeGlobalVar(((Storage)action.getPlaceholders().get("$STORAGE$")).getId(),var);
    }
}
