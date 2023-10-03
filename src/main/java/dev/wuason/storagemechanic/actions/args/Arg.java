package dev.wuason.storagemechanic.actions.args;

import dev.wuason.storagemechanic.actions.Action;
import dev.wuason.storagemechanic.actions.types.ArgType;
import org.bukkit.inventory.ItemStack;

public abstract class Arg implements Cloneable {

    private String line;

    public Arg(String line) {
        this.line = line;
    }

    public abstract Object getObject(Action action);

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public abstract void reload();

    @Override
    public Arg clone() {
        try {
            return (Arg) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }
}
