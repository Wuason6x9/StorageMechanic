package dev.wuason.storagemechanic.actions.args;

import dev.wuason.storagemechanic.actions.Action;

public class FloatArg extends Arg{
    public FloatArg(String line) {
        super(line);
    }

    @Override
    public Object getObject(Action action) {
        return Float.parseFloat(getLine().replace(" ", ""));
    }

    @Override
    public void reload() {

    }
}
