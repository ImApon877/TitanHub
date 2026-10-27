package me.imapon.titanhub.command;

import me.imapon.titanhub.TitanHub;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public final class TitanHubCommand implements TabExecutor {
    private final TitanHub plugin;

    public TitanHubCommand(TitanHub plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reload();
            sender.sendMessage(plugin.messages().get("reload", player));
            return true;
        }
        sender.sendMessage(plugin.messages().get("usage", player));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return args.length == 1 && sender.hasPermission(TitanHub.ADMIN_PERMISSION) && "reload".startsWith(args[0].toLowerCase())
                ? Collections.singletonList("reload") : Collections.<String>emptyList();
    }
}
