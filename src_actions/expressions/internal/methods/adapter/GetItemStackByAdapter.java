package dev.wuason.storagemechanic.actions.expressions.internal.methods.adapter;

import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.items.ItemBuilderMechanic;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.storagemechanic.actions.expressions.internal.InternalExpr;

import java.util.ArrayList;
import java.util.Arrays;

public class GetItemStackByAdapter extends InternalExpr {
    public static ArrayList<String> ARGS = new ArrayList<>(){{
        add("item".toUpperCase().intern());
        add("amount".toUpperCase().intern());
        add("name".toUpperCase().intern());
    }};
    public GetItemStackByAdapter() {
        super("getItemStackByAdapter", ARGS);
    }

    public Object getItemStackByAdapter(Object... objs){
        Object amount = objs[1];
        Object name = objs[2];
        if(amount == null) amount = "1";
        ItemBuilderMechanic itemBuilderMechanic = new ItemBuilderMechanic((String) objs[0], Integer.parseInt((String) amount));
        if(name != null) itemBuilderMechanic.setName(AdventureUtils.deserializeLegacy((String) name, null));
        return itemBuilderMechanic.build();
    }
}
