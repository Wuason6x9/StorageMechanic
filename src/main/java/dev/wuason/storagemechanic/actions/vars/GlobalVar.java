package dev.wuason.storagemechanic.actions.vars;

import java.util.UUID;

public class GlobalVar {
    private String id;
    private Object data;

    public GlobalVar(Object data) {
        this.data = data;
        id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
