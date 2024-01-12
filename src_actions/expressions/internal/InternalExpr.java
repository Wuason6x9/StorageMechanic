package dev.wuason.storagemechanic.actions.expressions.internal;

import dev.wuason.storagemechanic.actions.expressions.Expression;
import dev.wuason.storagemechanic.actions.expressions.internal.methods.RandomObject;
import dev.wuason.storagemechanic.actions.expressions.internal.methods.adapter.GetAdapterIdMethod;
import dev.wuason.storagemechanic.actions.expressions.internal.methods.adapter.GetItemStackByAdapter;
import dev.wuason.storagemechanic.actions.expressions.internal.methods.math.Chance;
import dev.wuason.storagemechanic.actions.expressions.internal.methods.math.RandomNumberExpr;
import dev.wuason.storagemechanic.actions.types.ArgType;

import java.util.ArrayList;
import java.util.HashMap;

public class InternalExpr extends Expression {
    public final static ArgType ARG_TYPE = ArgType.INTERNAL;
    private String method;
    private ArrayList<String> args;
    public InternalExpr(String method, ArrayList<String> args) {
        this.method = method;
        this.args = args;
    }
    public static HashMap<String,Class<?>> CLASSES_METHODS_HASHMAP = new HashMap<>(){{
        put("getAdapterId".toUpperCase().intern(), GetAdapterIdMethod.class);
        put("getItemStackByAdapter".toUpperCase().intern(), GetItemStackByAdapter.class);
        put("RandomNumber".toUpperCase().intern(), RandomNumberExpr.class);
        put("chance".toUpperCase().intern(), Chance.class);
        put("randomObject".toUpperCase().intern(), RandomObject.class);
    }}; // METHOD UPPERCASE & CLASS WITH METHOD
    public static HashMap<Class<?>,HashMap<String, InternalExpr>> CLASSES_HASHMAP = new HashMap<>(){{
        put(GetAdapterIdMethod.class, new HashMap<>(){{
            put("getAdapterId".toUpperCase().intern(), new GetAdapterIdMethod());
        }});
        put(GetItemStackByAdapter.class, new HashMap<>(){{
            put("getItemStackByAdapter".toUpperCase().intern(), new GetItemStackByAdapter());
        }});
        put(RandomNumberExpr.class, new HashMap<>(){{
            put("RandomNumber".toUpperCase().intern(), new RandomNumberExpr());
        }});
        put(Chance.class, new HashMap<>(){{
            put("chance".toUpperCase().intern(), new Chance());
        }});
        put(RandomObject.class, new HashMap<>(){{
            put("randomObject".toUpperCase().intern(), new RandomObject());
        }});
    }};

    public String getMethod() {
        return method;
    }

    public ArrayList<String> getArgs() {
        return args;
    }
}
