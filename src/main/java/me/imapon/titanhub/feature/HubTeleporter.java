package me.imapon.titanhub.feature;

import me.imapon.titanhub.TitanHub;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/** Sends players to the hub spawn, or to another server of the proxy. */
public final class HubTeleporter {
    public static final String PROXY_CHANNEL = "BungeeCord";
    private final TitanHub plugin;

    public HubTeleporter(TitanHub plugin) {
        this.plugin = plugin;
    }

    public boolean sendToHub(Player player, boolean clearInventory) {
        Location hubLocation = plugin.settings().hubLocation();
        if (hubLocation == null || !player.teleport(hubLocation)) return false;
        plugin.movement().refreshDoubleJump(player);
        plugin.hubItems().give(player, clearInventory);
        plugin.display().updateScoreboard(player);
        plugin.display().updateTab(player);
        return true;
    }

    public void connect(Player player, String server) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);
            out.writeUTF("Connect");
            out.writeUTF(server);
            player.sendPluginMessage(plugin, PROXY_CHANNEL, bytes.toByteArray());
        } catch (IOException ignored) {
            player.sendMessage(plugin.messages().get("proxy-connect-failed", player));
        }
    }
}
