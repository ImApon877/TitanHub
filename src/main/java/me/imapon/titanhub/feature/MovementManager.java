package me.imapon.titanhub.feature;

import me.imapon.titanhub.TitanHub;
import me.imapon.titanhub.config.HubSettings;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Double jump, launch pads and the enderbutt. */
public final class MovementManager {
    private final TitanHub plugin;
    private final Set<UUID> doubleJumpFlightEnabled = new HashSet<UUID>();
    private final Set<UUID> launchPadCooldown = new HashSet<UUID>();
    private final Map<UUID, Long> enderbuttCooldowns = new HashMap<UUID, Long>();

    public MovementManager(TitanHub plugin) {
        this.plugin = plugin;
    }

    public void handleMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        HubSettings settings = plugin.settings();
        refreshDoubleJump(player);
        if (!settings.launchPadsEnabled || !settings.isHubWorld(player.getWorld()) || sameBlock(event)) return;
        // A pressure plate is in the block of the player's feet; a full block pad is the block below.
        Block feet = event.getTo().getBlock();
        if (feet.getType() != settings.launchPadMaterial && feet.getRelative(0, -1, 0).getType() != settings.launchPadMaterial) return;
        if (!launchPadCooldown.add(player.getUniqueId())) return;
        // The server does not know the player's walking speed (getVelocity() is ~0), so launch where they look.
        Vector velocity = player.getLocation().getDirection().setY(0);
        if (velocity.lengthSquared() > 0) velocity.normalize().multiply(settings.launchPadStrength);
        velocity.setY(settings.launchPadVerticalBoost);
        player.setVelocity(velocity);
        play(player, settings.launchPadSound);
        Bukkit.getScheduler().runTaskLater(plugin, () -> launchPadCooldown.remove(player.getUniqueId()), 10L);
    }

    /** The "fly" key is used as the second jump: cancel the flight and push the player forward. */
    public void handleToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        HubSettings settings = plugin.settings();
        if (!settings.doubleJumpEnabled || !settings.isHubWorld(player.getWorld()) || player.getGameMode() == GameMode.CREATIVE) return;
        // Only the attempt to start flying is the second jump; stopping a real flight is left alone.
        if (!event.isFlying() || !doubleJumpFlightEnabled.contains(player.getUniqueId())) return;
        event.setCancelled(true);
        player.setAllowFlight(false);
        doubleJumpFlightEnabled.remove(player.getUniqueId());
        Vector direction = player.getLocation().getDirection().normalize().multiply(settings.doubleJumpStrength);
        direction.setY(Math.max(0.45D, direction.getY()));
        player.setVelocity(direction);
        play(player, settings.doubleJumpSound);
    }

    public void refreshDoubleJump(Player player) {
        HubSettings settings = plugin.settings();
        // Flying for real means another plugin (e.g. /fly) took over flight: stop treating it as a double jump.
        if (player.isFlying()) doubleJumpFlightEnabled.remove(player.getUniqueId());
        if (!settings.doubleJumpEnabled || !settings.isHubWorld(player.getWorld()) || player.getGameMode() == GameMode.CREATIVE || player.getAllowFlight()) return;
        // isOnGround() comes from the client and can be spoofed for infinite double jumps: also check the block below.
        if (!player.isOnGround() || !isStandingOnSolidBlock(player)) return;
        player.setAllowFlight(true);
        doubleJumpFlightEnabled.add(player.getUniqueId());
    }

    public void revokeDoubleJump(Player player) {
        if (doubleJumpFlightEnabled.remove(player.getUniqueId())) player.setAllowFlight(false);
    }

    public void useEnderbutt(Player player) {
        HubSettings settings = plugin.settings();
        long now = System.currentTimeMillis();
        Long previousUse = enderbuttCooldowns.get(player.getUniqueId());
        if (previousUse != null && now - previousUse < settings.enderbuttCooldownMillis) return;
        enderbuttCooldowns.put(player.getUniqueId(), now);
        Vector velocity = player.getLocation().getDirection().normalize().multiply(settings.enderbuttStrength);
        velocity.setY(velocity.getY() + settings.enderbuttVerticalBoost);
        player.setVelocity(velocity);
        play(player, settings.enderbuttSound);
    }

    public void forget(Player player) {
        revokeDoubleJump(player);
        enderbuttCooldowns.remove(player.getUniqueId());
    }

    private boolean isStandingOnSolidBlock(Player player) {
        Location feet = player.getLocation();
        double[] offsets = {-0.3D, 0.3D};
        for (double x : offsets) {
            for (double z : offsets) {
                if (feet.clone().add(x, -0.1D, z).getBlock().getType().isSolid()) return true;
            }
        }
        return false;
    }

    private static boolean sameBlock(PlayerMoveEvent event) {
        return event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ();
    }

    private static void play(Player player, Sound sound) {
        if (sound != null) player.playSound(player.getLocation(), sound, 1F, 1F);
    }
}
