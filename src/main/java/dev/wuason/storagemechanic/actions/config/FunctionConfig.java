package dev.wuason.storagemechanic.actions.config;

import java.util.ArrayList;
import java.util.HashMap;

public class FunctionConfig {
    private String function;
    private HashMap<String,String> args;

    public FunctionConfig(String function, HashMap<String, String> args) {
        this.function = function;
        this.args = args;
    }

    public String getFunction() {
        return function;
    }

    public HashMap<String, String> getArgs() {
        return args;
    }
}
