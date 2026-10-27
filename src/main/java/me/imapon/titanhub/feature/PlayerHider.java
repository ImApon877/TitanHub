package me.imapon.titanhub.feature;

import me.imapon.titanhub.TitanHub;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** The item that hides the other players. Only players hidden by TitanHub are shown again. */
public final class PlayerHider {
    private final TitanHub plugin;
    private final Set<UUID> hidingPlayers = new HashSet<UUID>();
    private final Map<UUID, Set<UUID>> hiddenByTitanHub = new HashMap<UUID, Set<UUID>>();
    private final Map<UUID, Long> cooldowns = new HashMap<UUID, Long>();

    public PlayerHider(TitanHub plugin) {
        this.plugin = plugin;
    }

    public boolean isHiding(Player player) {
        return hidingPlayers.contains(player.getUniqueId());
    }

    public void toggle(Player player) {
        long now = System.currentTimeMillis();
        Long previousUse = cooldowns.get(player.getUniqueId());
        long remaining = plugin.settings().playerHiderCooldownMillis - (now - (previousUse == null ? 0L : previousUse));
        if (remaining > 0L) {
            player.sendMessage(plugin.messages().get("player-hider-cooldown", player).replace("%seconds%", String.valueOf((remaining + 999L) / 1000L)));
            return;
        }
        cooldowns.put(player.getUniqueId(), now);
        boolean hide = !isHiding(player);
        if (hide) hide(player); else show(player);
        plugin.hubItems().give(player, false);
        player.sendMessage(plugin.messages().get(hide ? "players-hidden" : "players-shown", player));
    }

    /** Someone who joins is hidden from the players who are hiding everyone. */
    public void applyToJoiner(Player joiner) {
        for (UUID hiderId : hidingPlayers) {
            Player hider = Bukkit.getPlayer(hiderId);
            if (hider != null && hider.isOnline() && hider.canSee(joiner)) {
                Set<UUID> hidden = hiddenByTitanHub.get(hiderId);
                if (hidden == null) {
                    hidden = new HashSet<UUID>();
                    hiddenByTitanHub.put(hiderId, hidden);
                }
                hidden.add(joiner.getUniqueId());
                hider.hidePlayer(joiner);
            }
        }
    }

    /** Shows everyone again, used when the player hider gets disabled by a reload. */
    public void showAll() {
        for (UUID hiderId : new HashSet<UUID>(hidingPlayers)) {
            Player hider = Bukkit.getPlayer(hiderId);
            if (hider != null) show(hider);
        }
    }

    public void forget(Player player) {
        UUID uuid = player.getUniqueId();
        hidingPlayers.remove(uuid);
        hiddenByTitanHub.remove(uuid);
        for (Set<UUID> hidden : hiddenByTitanHub.values()) hidden.remove(uuid);
        cooldowns.remove(uuid);
    }

    private void hide(Player player) {
        Set<UUID> hidden = new HashSet<UUID>();
        hidingPlayers.add(player.getUniqueId());
        hiddenByTitanHub.put(player.getUniqueId(), hidden);
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(player) && player.canSee(other)) {
                hidden.add(other.getUniqueId());
                player.hidePlayer(other);
            }
        }
    }

    private void show(Player player) {
        hidingPlayers.remove(player.getUniqueId());
        Set<UUID> hidden = hiddenByTitanHub.remove(player.getUniqueId());
        if (hidden == null) return;
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (hidden.contains(other.getUniqueId())) player.showPlayer(other);
        }
    }
}
