package dev.wuason.storagemechanic.actions.args;

import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.compatibilities.adapter.Adapter;
import dev.wuason.storagemechanic.actions.Action;

public class AdapterArg extends Arg{

    public AdapterArg(String line) {
        super(line);
    }

    @Override
    public Object getObject(Action action) {
        Adapter adapterManager = Adapter.getInstance();
        return adapterManager.computeAdapterId(getLine().trim());
    }

    @Override
    public void reload() {

    }
}
