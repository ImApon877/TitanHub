package me.imapon.titanhub.feature;

import me.imapon.titanhub.TitanHub;
import me.imapon.titanhub.config.HubSettings;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/** The hotbar items TitanHub hands out (navigator, player hider, profile, enderbutt, PvP). */
public final class HubItems {
    private final TitanHub plugin;

    public HubItems(TitanHub plugin) {
        this.plugin = plugin;
    }

    public void give(Player player, boolean clearInventory) {
        HubSettings settings = plugin.settings();
        if (clearInventory && settings.clearInventoryOnJoin) player.getInventory().clear();
        remove(player);
        giveItem(player, settings.navigatorEnabled, settings.navigatorSlot, settings.navigatorItem);
        giveItem(player, settings.playerHiderEnabled, settings.playerHiderSlot, plugin.playerHider().isHiding(player) ? settings.playerHiderHiddenItem : settings.playerHiderVisibleItem);
        giveItem(player, settings.profileEnabled, settings.profileSlot, settings.profileItem);
        giveItem(player, settings.enderbuttEnabled, settings.enderbuttSlot, settings.enderbuttItem);
        giveItem(player, settings.pvpEnabled && !plugin.pvp().isInPvp(player), settings.pvpSlot, settings.pvpItem);
    }

    public void remove(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int slot = 0; slot < contents.length; slot++) {
            if (isManaged(contents[slot])) player.getInventory().setItem(slot, null);
        }
    }

    public boolean isManaged(ItemStack item) {
        if (item == null) return false;
        HubSettings settings = plugin.settings();
        return matches(item, settings.navigatorItem) || matches(item, settings.playerHiderVisibleItem)
                || matches(item, settings.playerHiderHiddenItem) || matches(item, settings.profileItem)
                || matches(item, settings.enderbuttItem) || matches(item, settings.pvpItem);
    }

    /** Hub items are recognised by material and display name. */
    public static boolean matches(ItemStack item, ItemStack expected) {
        if (item == null || expected == null || item.getType() != expected.getType() || !item.hasItemMeta() || !expected.hasItemMeta()) return false;
        ItemMeta actualMeta = item.getItemMeta();
        ItemMeta expectedMeta = expected.getItemMeta();
        return actualMeta.hasDisplayName() && expectedMeta.hasDisplayName() && actualMeta.getDisplayName().equals(expectedMeta.getDisplayName());
    }

    private void giveItem(Player player, boolean enabled, int slot, ItemStack item) {
        if (enabled && item != null && slot >= 0 && slot < player.getInventory().getSize()) player.getInventory().setItem(slot, item.clone());
    }
}
