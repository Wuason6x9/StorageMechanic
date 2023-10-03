package dev.wuason.storagemechanic.actions.args;

import dev.wuason.bsh.EvalError;
import dev.wuason.storagemechanic.actions.Action;

public class InventoryArg extends Arg{
    public InventoryArg(String line) {
        super(line);
    }

    @Override
    public Object getObject(Action action) {
        String code = getLine().trim();
        try {
            return action.getInterpreter().eval("$currentBukkitInventoryStorage$.".toUpperCase() + code);
        } catch (EvalError e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reload() {

    }
}
