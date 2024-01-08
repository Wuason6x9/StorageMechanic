package dev.wuason.storagemechanic.actions.args;

import dev.wuason.libs.bsh.EvalError;
import dev.wuason.storagemechanic.actions.Action;

import java.lang.reflect.Method;

public class JavaArg extends Arg{


    public JavaArg(String line) {
        super(line);
    }

    @Override
    public Object getObject(Action action) {
        String code = getLine().trim();
        Object objReturn = null;
        try {
            objReturn = action.getInterpreter().eval(action.getActionConfig().getImportsLine() + code);
        } catch (EvalError e) {
            throw new RuntimeException(e);
        }
        return objReturn;
    }

    @Override
    public void reload() {

    }
}
