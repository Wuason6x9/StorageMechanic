package dev.wuason.storagemechanic.utils;

import dev.wuason.bsh.EvalError;
import dev.wuason.storagemechanic.actions.Action;
import dev.wuason.storagemechanic.actions.args.Arg;
import dev.wuason.storagemechanic.actions.types.ArgType;

public class ArgUtils {
    public static Object runArg(Action action, ArgType argType, String value){
        Arg arg = ActionConfigUtils.getArg(argType, value.trim());
        Object object = arg.getObject(action);
        return object;
    }
    public static Object runArg(Action action, String argType, String value){
        ArgType argTypeEnum = ArgType.valueOf(argType.toUpperCase());
        try {
            value = (String) ActionsUtils.processArgSearchArg(argTypeEnum,value,action);
        } catch (EvalError e) {
            throw new RuntimeException(e);
        }
        Arg arg = ActionConfigUtils.getArg(argTypeEnum, value.trim());
        Object object = arg.getObject(action);

        return object;
    }
}
