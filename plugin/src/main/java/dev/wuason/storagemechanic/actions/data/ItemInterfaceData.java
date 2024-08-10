package dev.wuason.storagemechanic.actions.data;

public class ItemInterfaceData {
    private final String id;
    private final Object object;

    public ItemInterfaceData(String id, Object object) {
        this.id = id;
        this.object = object;
    }

    public String getId() {
        return id;
    }

    public Object getObject() {
        return object;
    }
}
