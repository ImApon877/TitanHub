package me.imapon.titanhub.command;

import me.imapon.titanhub.TitanHub;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class HubCommand implements TabExecutor {
    private final TitanHub plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<UUID, Long>();

    public HubCommand(TitanHub plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.messages().get("player-only", null));
            return true;
        }
        Player player = (Player) sender;
        if (!plugin.settings().hubCommandEnabled) return true;
        if (plugin.settings().hubLocation() == null) {
            player.sendMessage(plugin.messages().get("hub-not-set", player));
            return true;
        }
        long now = System.currentTimeMillis();
        Long previousUse = cooldowns.get(player.getUniqueId());
        long remaining = plugin.settings().hubCooldownMillis - (now - (previousUse == null ? 0L : previousUse));
        if (remaining > 0L && !player.hasPermission(TitanHub.ADMIN_PERMISSION)) {
            player.sendMessage(plugin.messages().get("hub-cooldown", player).replace("%seconds%", String.valueOf((remaining + 999L) / 1000L)));
            return true;
        }
        cooldowns.put(player.getUniqueId(), now);
        if (plugin.teleporter().sendToHub(player, false)) player.sendMessage(plugin.messages().get("hub-teleport", player));
        else player.sendMessage(plugin.messages().get("hub-teleport-failed", player));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }

    public void forget(UUID uuid) {
        cooldowns.remove(uuid);
    }
}
