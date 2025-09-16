package dev.wuason.storagemechanic.storages.config;

import java.util.*;

public class StorageSoundConfig {
    private String id;
    private String sound;
    private Map<Integer, Set<Integer>> pagesToSlots;
    private Type type;
    private float pitch;
    private float volume;

    public StorageSoundConfig(String id, String sound, List<Integer> pages, Type type, List<Integer> slots, Double pitch, int volume) {
        this.id = id;
        this.sound = sound;
        this.type = type;
        this.pitch = pitch.floatValue();
        this.volume = volume / 100.0f;

        this.pagesToSlots = new HashMap<>();
        HashSet<Integer> hashSet = new HashSet<>(slots);

        for (Integer i : pages) {
            pagesToSlots.put(i, hashSet);
        }
    }

    public String getId() {
        return id;
    }

    public String getSound() {
        return sound;
    }

    public Map<Integer, Set<Integer>> getPagesToSlots() {
        return pagesToSlots;
    }

    public Type getType() {
        return type;
    }

    public float getPitch() {
        return pitch;
    }

    public float getVolume() {
        return volume;
    }

    public enum Type {
        OPEN,
        CLOSE,
        CLICK
    }
}
