package dev.wuason.storagemechanic.actions.functions.functions;

import dev.wuason.storagemechanic.actions.Action;
import dev.wuason.storagemechanic.actions.functions.Function;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Locale;

public class cloned extends Function {

    public static ArrayList<String> ARGS = new ArrayList<>(){{
        add("var".toUpperCase(Locale.ENGLISH).intern());
    }};

    public cloned() {
        super("cloned".toUpperCase(Locale.ENGLISH).intern(), ARGS);
    }

    @Override
    public void execute(Action action, Player player, Object... objects) {

    }
}
