package me.imapon.titanhub.listener;

import me.imapon.titanhub.TitanHub;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;

/** Join, quit, death and respawn. */
public final class PlayerListener implements Listener {
    private final TitanHub plugin;

    public PlayerListener(TitanHub plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.setJoinMessage(plugin.messages().get("join", player));
        if (plugin.settings().spawnOnJoin && plugin.settings().hubLocation() != null) {
            plugin.teleporter().sendToHub(player, true);
            plugin.playerHider().applyToJoiner(player);
            return;
        }
        // The player inventory is fully initialized on the next server tick.
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) return;
            plugin.hubItems().give(player, true);
            plugin.display().updateScoreboard(player);
            plugin.display().updateTab(player);
            plugin.movement().refreshDoubleJump(player);
            plugin.playerHider().applyToJoiner(player);
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage(plugin.messages().get("quit", player));
        plugin.playerHider().forget(player);
        plugin.movement().forget(player);
        plugin.hubCommand().forget(player.getUniqueId());
        plugin.pvp().forget(player.getUniqueId());
        plugin.display().forget(player.getUniqueId());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (plugin.pvp().handleDeath(event)) return;
        for (Iterator<ItemStack> iterator = event.getDrops().iterator(); iterator.hasNext();) {
            if (plugin.hubItems().isManaged(iterator.next())) iterator.remove();
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location hubLocation = plugin.settings().hubLocation();
        if (hubLocation != null) event.setRespawnLocation(hubLocation);
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                plugin.hubItems().give(player, false);
                plugin.display().updateScoreboard(player);
                plugin.display().updateTab(player);
            }
        });
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if (!plugin.settings().isHubWorld(event.getPlayer().getWorld())) plugin.movement().revokeDoubleJump(event.getPlayer());
    }
}
