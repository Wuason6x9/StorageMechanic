package dev.wuason.storagemechanic.inventory;

import dev.wuason.fastinv.FastInv;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.function.Function;

public class InventoryMechanic extends FastInv {

    public InventoryMechanic(int size, String title) {
        super(size, title);
    }

    public InventoryMechanic(InventoryType type, String title) {
        super(type, title);
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {

    }

    @Override
    public void onClose(InventoryCloseEvent event) {

    }

    @Override
    public void onClick(InventoryClickEvent event) {

    }
}
