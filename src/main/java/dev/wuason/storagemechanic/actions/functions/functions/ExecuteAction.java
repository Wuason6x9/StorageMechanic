package dev.wuason.storagemechanic.actions.functions.functions;

import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.actions.Action;
import dev.wuason.storagemechanic.actions.functions.Function;
import dev.wuason.storagemechanic.storages.Storage;
import dev.wuason.storagemechanic.utils.ActionConfigUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class ExecuteAction extends Function {
    public static ArrayList<String> ARGS = new ArrayList<>(){{
        add("action".toUpperCase(Locale.ENGLISH).intern());
        add("vars".toUpperCase(Locale.ENGLISH).intern());
    }};

    public ExecuteAction() {
        super("executeAction".toUpperCase(Locale.ENGLISH).intern(), ARGS);
    }

    @Override
    public void execute(Action action, Player player, Object... objects) {
        ArrayList<String> listStringVars = new ArrayList<>();
        String actionId = (String) objects[0];
        if(objects[1] != null){
            listStringVars.addAll(ActionConfigUtils.getArrayListFromArg((String)objects[1]));
        }
        HashMap<String,Object> placeholders = new HashMap<>();
        for(String v : listStringVars){
            placeholders.put(v.trim().intern(),action.getPlaceholders().get(v.trim().intern()));
        }
        Action actionExe = StorageMechanic.getInstance().getManagers().getActionManager().createAction((Storage)action.getPlaceholders().get("$STORAGE$"),actionId,player,action.getEventAction(), placeholders);
        actionExe.execute();
    }
}
