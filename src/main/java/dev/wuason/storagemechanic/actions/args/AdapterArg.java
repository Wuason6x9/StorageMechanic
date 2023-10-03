package dev.wuason.storagemechanic.actions.args;

import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.compatibilities.AdapterManager;
import dev.wuason.storagemechanic.actions.Action;
import dev.wuason.storagemechanic.actions.types.ArgType;

public class AdapterArg extends Arg{

    public AdapterArg(String line) {
        super(line);
    }

    @Override
    public Object getObject(Action action) {
        AdapterManager adapterManager = Mechanics.getInstance().getManager().getAdapterManager();
        return adapterManager.computeAdapterId(getLine().trim());
    }

    @Override
    public void reload() {

    }
}
