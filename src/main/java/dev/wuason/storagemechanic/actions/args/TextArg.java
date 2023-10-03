package dev.wuason.storagemechanic.actions.args;

import dev.wuason.storagemechanic.actions.Action;
import dev.wuason.storagemechanic.actions.types.ArgType;

public class TextArg extends Arg {
    public TextArg(String line) {
        super(line);
    }

    @Override
    public Object getObject(Action action) {
        return getLine().trim().intern();
    }

    @Override
    public void reload() {

    }
}
