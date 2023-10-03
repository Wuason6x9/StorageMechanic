package dev.wuason.storagemechanic.actions.expressions.internal.methods.adapter;

import dev.wuason.mechanics.Mechanics;
import dev.wuason.storagemechanic.actions.expressions.internal.InternalExpr;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class GetAdapterIdMethod extends InternalExpr {

    public static ArrayList<String> ARGS = new ArrayList<>(){{
        add("item".toUpperCase().intern());
    }};
    public GetAdapterIdMethod() {
        super("getAdapterId", ARGS);
    }

    public Object getAdapterId(Object... objs){
        if(objs[0] instanceof ItemStack){
            return Mechanics.getInstance().getManager().getAdapterManager().getAdapterID((ItemStack) objs[0]);
        }
        else if (objs[0] instanceof Block) {
            return Mechanics.getInstance().getManager().getAdapterManager().getAdapterID((Block) objs[0]);
        }
        return null;
    }
}
