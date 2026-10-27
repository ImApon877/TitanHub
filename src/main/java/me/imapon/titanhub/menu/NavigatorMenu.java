package me.imapon.titanhub.menu;

import me.imapon.titanhub.TitanHub;
import me.imapon.titanhub.config.Destination;
import me.imapon.titanhub.config.HubSettings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class NavigatorMenu {
    private final TitanHub plugin;

    public NavigatorMenu(TitanHub plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        HubSettings settings = plugin.settings();
        Inventory menu = Bukkit.createInventory(new MenuHolder(MenuHolder.Type.NAVIGATOR), settings.navigatorSize, settings.navigatorTitle);
        if (settings.navigatorDecorationEnabled && settings.navigatorDecorationItem != null) {
            for (Integer slot : settings.navigatorDecorationSlots) {
                if (slot != null && slot >= 0 && slot < settings.navigatorSize) menu.setItem(slot, settings.navigatorDecorationItem.clone());
            }
        }
        for (Destination destination : settings.destinations.values()) {
            menu.setItem(destination.getSlot(), destination.getIcon().clone());
        }
        player.openInventory(menu);
    }

    public void handleClick(Player player, int slot) {
        Destination destination = plugin.settings().destinations.get(slot);
        if (destination == null) return;
        player.closeInventory();
        if (destination.isServer()) plugin.teleporter().connect(player, destination.getValue());
        else Bukkit.dispatchCommand(player, destination.getValue().replace("%player%", player.getName()));
    }
}
