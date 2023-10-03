package dev.wuason.storagemechanic.actions.args;

import dev.wuason.storagemechanic.actions.Action;
import dev.wuason.storagemechanic.actions.types.ArgType;
import dev.wuason.storagemechanic.utils.StorageUtils;

import java.util.Arrays;
import java.util.Collections;

public class NumberArg extends Arg {

    public NumberArg(String line) {
        super(line);
    }
    @Override
    public Object getObject(Action action) {
        if(getLine().contains("-")) return StorageUtils.configFill(Collections.singletonList(getLine()));
        return Integer.parseInt(getLine().replace(" ", ""));
    }

    @Override
    public void reload() {

    }
}
