package dev.wuason.storagemechanic.actions.expressions.internal.methods.math;

import dev.wuason.mechanics.utils.MathUtils;
import dev.wuason.storagemechanic.actions.expressions.internal.InternalExpr;

import java.util.ArrayList;

public class Chance extends InternalExpr {
    public static ArrayList<String> ARGS = new ArrayList<>(){{
        add("chance".toUpperCase().intern());
    }};
    public Chance() {
        super("chance", ARGS);
    }

    public Object chance(Object... objs){
        return MathUtils.chance((Float) objs[0]);
    }
}
