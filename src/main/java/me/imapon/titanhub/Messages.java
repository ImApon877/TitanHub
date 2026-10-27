package me.imapon.titanhub;

import me.imapon.titanhub.util.Text;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

/** messages.yml plus placeholder handling (%player% and PlaceholderAPI when installed). */
public final class Messages {
    private final JavaPlugin plugin;
    private FileConfiguration messages;
    private Method placeholderMethod;

    public Messages(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) plugin.saveResource("messages.yml", false);
        messages = YamlConfiguration.loadConfiguration(file);
        try (InputStream resource = plugin.getResource("messages.yml")) {
            if (resource != null) {
                messages.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(resource, StandardCharsets.UTF_8)));
                messages.options().copyDefaults(true);
                try {
                    messages.save(file);
                } catch (IOException ignored) { }
            }
        } catch (IOException ignored) { }
        placeholderMethod = null;
        if (!plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) return;
        try {
            Class<?> api = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            placeholderMethod = api.getMethod("setPlaceholders", Player.class, String.class);
        } catch (ReflectiveOperationException ignored) { }
    }

    /** The message at {@code path}, formatted for {@code player} (which may be null for the console). */
    public String get(String path, Player player) {
        return format(player, messages.getString(path, ""));
    }

    public String format(Player player, String value) {
        String formatted = value == null ? "" : value.replace("%player%", player == null ? "" : player.getName());
        if (placeholderMethod != null && player != null) {
            try {
                formatted = (String) placeholderMethod.invoke(null, player, formatted);
            } catch (ReflectiveOperationException ignored) { }
        }
        return Text.color(formatted);
    }
}
