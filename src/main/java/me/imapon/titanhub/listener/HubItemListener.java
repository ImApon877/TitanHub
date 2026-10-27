package me.imapon.titanhub.listener;

import me.imapon.titanhub.TitanHub;
import me.imapon.titanhub.config.HubSettings;
import me.imapon.titanhub.feature.HubItems;
import me.imapon.titanhub.menu.MenuHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/** Using the hub items, keeping them in place, and the TitanHub menus. */
public final class HubItemListener implements Listener {
    private final TitanHub plugin;

    public HubItemListener(TitanHub plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        HubSettings settings = plugin.settings();
        if (!settings.isHubWorld(player.getWorld())) return;
        if (settings.navigatorEnabled && HubItems.matches(item, settings.navigatorItem)) {
            event.setCancelled(true);
            plugin.navigator().open(player);
        } else if (settings.playerHiderEnabled && HubItems.matches(item, plugin.playerHider().isHiding(player) ? settings.playerHiderHiddenItem : settings.playerHiderVisibleItem)) {
            event.setCancelled(true);
            plugin.playerHider().toggle(player);
        } else if (settings.profileEnabled && HubItems.matches(item, settings.profileItem)) {
            event.setCancelled(true);
            plugin.profile().openNextTick(player);
        } else if (settings.enderbuttEnabled && HubItems.matches(item, settings.enderbuttItem)) {
            event.setCancelled(true);
            plugin.movement().useEnderbutt(player);
        } else if (settings.pvpEnabled && HubItems.matches(item, settings.pvpItem)) {
            event.setCancelled(true);
            plugin.pvp().startCountdown(player);
        } else if (settings.preventInteractions && event.getAction() == Action.RIGHT_CLICK_BLOCK && !player.hasPermission(TitanHub.ADMIN_PERMISSION)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        HubItems items = plugin.hubItems();
        if (items.isManaged(event.getCurrentItem()) || items.isManaged(event.getCursor())
                || (event.getHotbarButton() >= 0 && items.isManaged(player.getInventory().getItem(event.getHotbarButton())))) {
            event.setCancelled(true);
            return;
        }
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (!(holder instanceof MenuHolder)) return;
        event.setCancelled(true);
        if (((MenuHolder) holder).getType() != MenuHolder.Type.NAVIGATOR) return;
        if (event.getRawSlot() < 0 || event.getRawSlot() >= event.getView().getTopInventory().getSize()) return;
        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;
        plugin.navigator().handleClick(player, event.getRawSlot());
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (plugin.hubItems().isManaged(event.getOldCursor()) || event.getView().getTopInventory().getHolder() instanceof MenuHolder) event.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        HubSettings settings = plugin.settings();
        // Dropping while holding the PvP sword is how players leave PvP.
        if (plugin.pvp().isInPvp(player) && player.getInventory().getHeldItemSlot() == settings.pvpSlot) {
            event.setCancelled(true);
            plugin.pvp().startExit(player);
            return;
        }
        if (plugin.hubItems().isManaged(event.getItemDrop().getItemStack())
                || (settings.preventItemDrop && settings.isHubWorld(player.getWorld()) && !player.hasPermission(TitanHub.ADMIN_PERMISSION))) event.setCancelled(true);
    }

    @EventHandler
    public void onHeldItemChange(PlayerItemHeldEvent event) {
        plugin.pvp().handleHeldItemChange(event.getPlayer(), event.getNewSlot());
    }
}
