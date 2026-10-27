package me.imapon.titanhub.command;

import me.imapon.titanhub.TitanHub;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public final class SetHubCommand implements TabExecutor {
    private final TitanHub plugin;

    public SetHubCommand(TitanHub plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.messages().get("player-only", null));
            return true;
        }
        Player player = (Player) sender;
        Location location = player.getLocation();
        plugin.getConfig().set("hub-location.world", location.getWorld().getName());
        plugin.getConfig().set("hub-location.x", location.getX());
        plugin.getConfig().set("hub-location.y", location.getY());
        plugin.getConfig().set("hub-location.z", location.getZ());
        plugin.getConfig().set("hub-location.yaw", location.getYaw());
        plugin.getConfig().set("hub-location.pitch", location.getPitch());
        plugin.saveConfig();
        plugin.reloadSettings();
        player.sendMessage(plugin.messages().get("hub-set", player).replace("%world%", location.getWorld().getName()));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
