package me.imapon.titanhub.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/** Identifies TitanHub menus by holder instead of title, so other plugins' inventories are never intercepted. */
public final class MenuHolder implements InventoryHolder {
    public enum Type { NAVIGATOR, PROFILE }

    private final Type type;

    MenuHolder(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
