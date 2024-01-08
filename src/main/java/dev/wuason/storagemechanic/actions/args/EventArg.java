package dev.wuason.storagemechanic.actions.args;


import dev.wuason.libs.bsh.EvalError;
import dev.wuason.storagemechanic.actions.Action;

import dev.wuason.storagemechanic.actions.config.InternalConfigContent;
import dev.wuason.storagemechanic.actions.expressions.internal.InternalExpr;
import dev.wuason.storagemechanic.actions.types.ArgType;
import dev.wuason.storagemechanic.utils.ActionConfigUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EventArg extends Arg{

    public EventArg(String line) {
        super(line);
    }

    @Override
    public Object getObject(Action action) {
        String code = getLine().trim();
        try {
            return action.getInterpreter().eval("$event$.".toUpperCase() + code);
        } catch (EvalError e) {
            throw new RuntimeException(e);
        }
    }





    @Override
    public void reload() {
    }


}
