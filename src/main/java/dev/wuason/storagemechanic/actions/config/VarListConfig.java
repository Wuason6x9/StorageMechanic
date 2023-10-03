package dev.wuason.storagemechanic.actions.config;

import dev.wuason.storagemechanic.actions.args.Arg;
import dev.wuason.storagemechanic.actions.types.ArgType;
import dev.wuason.storagemechanic.utils.ActionConfigUtils;

import java.util.ArrayList;
import java.util.List;

public class VarListConfig {
    private String id;
    private ArgType argType;
    private String var;
    private List<Arg> list = new ArrayList<>();

    public VarListConfig(String id, ArgType argType, String var, List<String> list) {
        this.id = id;
        this.argType = argType;
        this.var = var;
        for(String a : list){
            this.list.add(ActionConfigUtils.getArg(argType,a));
        }
    }

    public String getId() {
        return id;
    }

    public ArgType getArgType() {
        return argType;
    }

    public String getVar() {
        return var;
    }

    public List<Arg> getArrayList() {
        return list;
    }
}
