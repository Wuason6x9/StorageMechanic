package dev.wuason.storagemechanic.items.properties;

public class ActionItemProperties extends Properties{
    private String actionId;

    public ActionItemProperties(String actionId) {
        this.actionId = actionId;
    }

    public String getActionId() {
        return actionId;
    }
}
