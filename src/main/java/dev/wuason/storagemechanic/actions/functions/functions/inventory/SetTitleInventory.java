package dev.wuason.storagemechanic.actions.functions.functions.inventory;

import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.mechanics.utils.Utils;
import dev.wuason.storagemechanic.actions.Action;
import dev.wuason.storagemechanic.actions.functions.Function;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class SetTitleInventory extends Function {
    //ARGUMENTS
    public static ArrayList<String> ARGS = new ArrayList<>(){{
        add("TITLE");
        add("toAll".toUpperCase(Locale.ENGLISH).intern());
    }};
    public SetTitleInventory() {
        super("SETTITLEINVENTORY", ARGS);
    }

    @Override
    public void execute(Action action, Player player, Object... objects){
        String title = ((String) objects[0]);
        boolean toAll = false;
        if(objects[1] != null && Utils.isBool(((String) objects[1]).trim())){
            toAll = Boolean.parseBoolean(((String) objects[1]).trim());
        }
        Inventory inventory = null;
        if(toAll == true){
            inventory = (Inventory) action.getPlaceholders().get("$currentBukkitInventoryStorage$".toUpperCase().intern());
            if(inventory != null){
                for(HumanEntity human : inventory.getViewers()){
                    Mechanics.getInstance().getServerNmsVersion().getVersionWrapper().updateCurrentInventoryTitle(AdventureUtils.deserializeJson(title, (Player) human),(Player) human);
                }
                return;
            }
        }
        Mechanics.getInstance().getServerNmsVersion().getVersionWrapper().updateCurrentInventoryTitle(AdventureUtils.deserializeJson(title,player),player);
    }
}
