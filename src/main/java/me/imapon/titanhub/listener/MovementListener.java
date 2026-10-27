package me.imapon.titanhub.listener;

import me.imapon.titanhub.TitanHub;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public final class MovementListener implements Listener {
    private final TitanHub plugin;

    public MovementListener(TitanHub plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        plugin.movement().handleMove(event);
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        plugin.movement().handleToggleFlight(event);
    }
}
