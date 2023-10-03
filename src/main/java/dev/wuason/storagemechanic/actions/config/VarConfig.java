package dev.wuason.storagemechanic.actions.config;

import dev.wuason.storagemechanic.actions.args.Arg;
import dev.wuason.storagemechanic.actions.types.ArgType;
import dev.wuason.storagemechanic.utils.ActionConfigUtils;

public class VarConfig {
    private String var;
    private String content;
    private Arg arg;
    private ArgType argType;

    public VarConfig(String var, String content, ArgType argType) {
        this.var = var;
        this.content = content;
        this.argType = argType;
        arg = ActionConfigUtils.getArg(argType,content);
    }

    public String getVar() {
        return var;
    }

    public String getContent() {
        return content;
    }

    public ArgType getArgType() {
        return argType;
    }

    public Arg getArg() {
        return arg;
    }
}
