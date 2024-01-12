package dev.wuason.storagemechanic.actions.events;

import org.bukkit.event.Event;

import java.util.HashMap;

public abstract class EventAction {
    private String id;
    private Event event;

    public EventAction(String id, Event event) {
        this.id = id;
        this.event = event;
    }

    public abstract void registerPlaceholders(HashMap<String, Object> currentPlaceholders);

    public Event getEvent() {
        return event;
    }

    public String getId() {
        return id;
    }
}
