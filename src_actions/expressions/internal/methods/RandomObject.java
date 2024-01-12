package dev.wuason.storagemechanic.actions.expressions.internal.methods;

import dev.wuason.mechanics.utils.MathUtils;
import dev.wuason.storagemechanic.actions.expressions.internal.InternalExpr;

import java.util.ArrayList;
import java.util.Random;

public class RandomObject extends InternalExpr {
    public static ArrayList<String> ARGS = new ArrayList<>(){{
        add("list".toUpperCase().intern());
    }};

    public RandomObject() {
        super("randomObject", ARGS);
    }

    public Object randomObject(Object... objs){
        ArrayList<?> list = (ArrayList<?>) objs[0];
        Random random = new Random();
        int randomIndex = random.nextInt( list.size() );
        return list.get(randomIndex);
    }
}
