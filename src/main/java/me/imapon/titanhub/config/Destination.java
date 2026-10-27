package me.imapon.titanhub.config;

import org.bukkit.inventory.ItemStack;

/** A navigator entry: either a proxy server to connect to or a command run by the player. */
public final class Destination {
    private final int slot;
    private final ItemStack icon;
    private final boolean server;
    private final String value;

    public Destination(int slot, ItemStack icon, boolean server, String value) {
        this.slot = slot;
        this.icon = icon;
        this.server = server;
        this.value = value;
    }

    public int getSlot() {
        return slot;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public boolean isServer() {
        return server;
    }

    public String getValue() {
        return value;
    }
}
