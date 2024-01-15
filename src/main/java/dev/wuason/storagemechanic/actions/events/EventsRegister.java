package dev.wuason.storagemechanic.actions.events;

import dev.wuason.mechanics.actions.events.Events;
import dev.wuason.storagemechanic.actions.events.def.*;

import java.util.Locale;

public class EventsRegister {
    public static void registerEvents() {

        Events.EVENTS.put("close_storage_page".toUpperCase(Locale.ENGLISH), CloseStoragePageActionEvent.class);
        Events.EVENTS.put("click_storage_page".toUpperCase(Locale.ENGLISH), ClickStoragePageActionEvent.class);
        Events.EVENTS.put("open_storage_page".toUpperCase(Locale.ENGLISH), OpenStoragePageActionEvent.class);
        Events.EVENTS.put("click_item_interface".toUpperCase(Locale.ENGLISH), ClickItemInterfaceActionEvent.class);
        Events.EVENTS.put("skill_mythic".toUpperCase(Locale.ENGLISH), SkillMythicActionEvent.class);
    }
}
