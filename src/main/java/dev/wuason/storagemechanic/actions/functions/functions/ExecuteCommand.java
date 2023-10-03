package dev.wuason.storagemechanic.actions.functions.functions;

import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.actions.Action;
import dev.wuason.storagemechanic.actions.functions.Function;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class ExecuteCommand extends Function {
    public static ArrayList<String> ARGS = new ArrayList<>(){{
        add("COMMAND"); //0
        add("ASOP"); //1
        add("ASCONSOLE"); //2
        add("DELAY");
    }};
    public ExecuteCommand() {
        super("EXECUTECOMMAND", ARGS);
    }

    @Override
    public void execute(Action action, Player player, Object... objects) {
        boolean asConsole = true; //DEF VALUE
        boolean asOp = false; //DEF VALUE
        long delay = 0;
        if(objects[3] != null) delay = Long.parseLong((String) objects[3]);
        String command = AdventureUtils.deserializeLegacy(((String) objects[0]),player);
        if(objects[1] != null) asOp = Boolean.parseBoolean((String) objects[1]);
        if(objects[2] != null) asConsole = Boolean.parseBoolean((String) objects[2]);
        if(player.isOp()) asOp = false;
        if(delay == 0) {
            executeCommand(asConsole, asOp, player, command);
            return;
        }
        boolean finalAsConsole = asConsole;
        boolean finalAsOp = asOp;
        Bukkit.getScheduler().runTaskLater(StorageMechanic.getInstance(),() -> {
            executeCommand(finalAsConsole, finalAsOp, player, command);
        },delay);
    }

    public void executeCommand(boolean asConsole, boolean asOp, Player player, String command){
        if(asConsole){
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),command);
            return;
        }
        if(asOp) player.setOp(true);
        Bukkit.dispatchCommand(player,command);
        if(asOp) player.setOp(false);
    }
}
