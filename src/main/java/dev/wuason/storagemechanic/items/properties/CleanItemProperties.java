package dev.wuason.storagemechanic.items.properties;

import java.util.ArrayList;
import java.util.Set;

public class CleanItemProperties extends Properties {
    private ArrayList<Integer> pages;
    private ArrayList<Integer> slots;


    public CleanItemProperties(ArrayList<Integer> pages, ArrayList<Integer> slots) {
        this.pages = pages;
        this.slots = slots;
    }

    public ArrayList<Integer> getPages() {
        return pages;
    }

    public ArrayList<Integer> getSlots() {
        return slots;
    }
}
