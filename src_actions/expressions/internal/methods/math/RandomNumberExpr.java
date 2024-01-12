package dev.wuason.storagemechanic.actions.expressions.internal.methods.math;

import dev.wuason.mechanics.utils.MathUtils;
import dev.wuason.storagemechanic.actions.expressions.internal.InternalExpr;

import java.util.ArrayList;

public class RandomNumberExpr extends InternalExpr {
    public static ArrayList<String> ARGS = new ArrayList<>(){{
        add("min".toUpperCase().intern());
        add("max".toUpperCase().intern());
    }};
    public RandomNumberExpr() {
        super("randomNumber", ARGS);
    }

    public Object randomNumber(Object... objs){
        return MathUtils.randomNumber((int)objs[0],(int)objs[1]);
    }
}
