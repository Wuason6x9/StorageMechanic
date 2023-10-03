package dev.wuason.storagemechanic.actions.args;

import dev.wuason.storagemechanic.actions.Action;

public class BooleanArg extends Arg {

    public BooleanArg(String line) {
        super(line);
    }

    @Override
    public Object getObject(Action action) {
        return Boolean.parseBoolean(getLine().replace(" ", ""));
    }

    @Override
    public void reload() {

    }
}
