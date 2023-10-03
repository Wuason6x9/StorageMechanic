package dev.wuason.storagemechanic.actions.functions;

import dev.wuason.storagemechanic.actions.Action;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Function {

    private String id;
    private ArrayList<String> args;

    public Function(String id, ArrayList<String> args) {
        this.id = id;
        this.args = args;
    }


    public String getId() {
        return id;
    }
    public abstract void execute(Action action, Player player, Object... objects);

    public ArrayList<String> getArgs() {
        return args;
    }
}
