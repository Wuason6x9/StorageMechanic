package dev.wuason.storagemechanic.actions.events;

public enum EventEnum {
    CLOSE_STORAGE_PAGE(CloseStoragePageAction.class),
    OPEN_STORAGE_PAGE(OpenStoragePageAction.class),
    CLICK_STORAGE_ITEM(ClickStorageItemAction.class)
    ;
    private Class<?> eventActionClass;

    private EventEnum(Class<?> eventActionClass) {
        this.eventActionClass = eventActionClass;
    }

    public Class<?> getEventActionClass() {
        return eventActionClass;
    }
}
