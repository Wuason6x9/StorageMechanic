package dev.wuason.storagemechanic.actions.config;

import java.util.HashMap;

public class InternalConfigContent {
    private String method;
    private HashMap<String, String> args;

    public InternalConfigContent(String method, HashMap<String, String> args) {
        this.method = method;
        this.args = args;
    }

    public String getMethod() {
        return method;
    }

    public HashMap<String, String> getArgs() {
        return args;
    }
}
