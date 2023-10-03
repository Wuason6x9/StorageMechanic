package dev.wuason.storagemechanic.actions.args;

import dev.wuason.storagemechanic.actions.Action;
import dev.wuason.storagemechanic.actions.vars.GlobalVar;
import dev.wuason.storagemechanic.storages.Storage;

import java.util.Locale;

public class VarArg extends Arg{
    public VarArg(String line) {
        super(line);
    }

    @Override
    public Object getObject(Action action) {
        String var = getLine().replace(" ", "").toUpperCase(Locale.ENGLISH).intern();
        //GLOBAL VAR
        if(var.contains("{") && var.contains("}")) {
            String varString = getGlobalVarString(var);
            GlobalVar globalVar = action.getActionManager().getGlobalVar(varString,((Storage)action.getPlaceholders().get("$STORAGE$")).getId());
            if(var.contains(".OBJECT")) return globalVar.getData();
            if(var.contains(".ID")) return globalVar.getId();
            return globalVar;
        }
        //LOCAL VAR
        return action.getPlaceholders().getOrDefault(var,"INVALID");
    }

    @Override
    public void reload() {

    }

    private String getGlobalVarString(String string){
        int charFirst = string.indexOf("{");
        int charLast = string.lastIndexOf("}");
        return string.substring(charFirst,charLast + 1).intern();
    }
}
