package me.imapon.titanhub;

import me.imapon.titanhub.command.HubCommand;
import me.imapon.titanhub.command.SetHubCommand;
import me.imapon.titanhub.command.TitanHubCommand;
import me.imapon.titanhub.config.HubSettings;
import me.imapon.titanhub.feature.DisplayManager;
import me.imapon.titanhub.feature.HubItems;
import me.imapon.titanhub.feature.HubTeleporter;
import me.imapon.titanhub.feature.MovementManager;
import me.imapon.titanhub.feature.PlayerHider;
import me.imapon.titanhub.feature.PvpManager;
import me.imapon.titanhub.listener.HubItemListener;
import me.imapon.titanhub.listener.MovementListener;
import me.imapon.titanhub.listener.PlayerListener;
import me.imapon.titanhub.listener.ProtectionListener;
import me.imapon.titanhub.menu.NavigatorMenu;
import me.imapon.titanhub.menu.ProfileMenu;
import me.imapon.titanhub.util.Utf8Yaml;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public final class TitanHub extends JavaPlugin {
    public static final String ADMIN_PERMISSION = "titanhub.admin";
    private HubSettings settings;
    private Messages messages;
    private HubItems hubItems;
    private PlayerHider playerHider;
    private PvpManager pvp;
    private MovementManager movement;
    private DisplayManager display;
    private HubTeleporter teleporter;
    private NavigatorMenu navigator;
    private ProfileMenu profile;
    private HubCommand hubCommand;
    private YamlConfiguration config;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        updateConfigDefaults();
        messages = new Messages(this);
        messages.load();
        settings = new HubSettings(getConfig());

        hubItems = new HubItems(this);
        playerHider = new PlayerHider(this);
        pvp = new PvpManager(this);
        movement = new MovementManager(this);
        display = new DisplayManager(this);
        teleporter = new HubTeleporter(this);
        navigator = new NavigatorMenu(this);
        profile = new ProfileMenu(this);
        display.restartRefreshTask();

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerListener(this), this);
        pluginManager.registerEvents(new HubItemListener(this), this);
        pluginManager.registerEvents(new MovementListener(this), this);
        pluginManager.registerEvents(new ProtectionListener(this), this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, HubTeleporter.PROXY_CHANNEL);

        hubCommand = new HubCommand(this);
        register("titanhub", new TitanHubCommand(this));
        register("hub", hubCommand);
        register("sethub", new SetHubCommand(this));
    }

    /** /titanhub reload: config, messages and everything handed out to online players. */
    public void reload() {
        // Remove items matching the old names before they change, otherwise they stay in inventories.
        for (Player online : Bukkit.getOnlinePlayers()) hubItems.remove(online);
        reloadConfig();
        updateConfigDefaults();
        messages.load();
        settings = new HubSettings(getConfig());
        display.restartRefreshTask();
        if (!settings.playerHiderEnabled) playerHider.showAll();
        for (Player player : Bukkit.getOnlinePlayers()) {
            movement.revokeDoubleJump(player);
            hubItems.give(player, false);
            if (settings.scoreboardEnabled) display.updateScoreboard(player);
            else display.clearScoreboard(player);
            display.updateTab(player);
            movement.refreshDoubleJump(player);
        }
    }

    /** Re-reads config.yml into a new settings snapshot, e.g. after /sethub. */
    public void reloadSettings() {
        settings = new HubSettings(getConfig());
    }

    public HubSettings settings() {
        return settings;
    }

    public Messages messages() {
        return messages;
    }

    public HubItems hubItems() {
        return hubItems;
    }

    public PlayerHider playerHider() {
        return playerHider;
    }

    public PvpManager pvp() {
        return pvp;
    }

    public MovementManager movement() {
        return movement;
    }

    public DisplayManager display() {
        return display;
    }

    public HubTeleporter teleporter() {
        return teleporter;
    }

    public NavigatorMenu navigator() {
        return navigator;
    }

    public ProfileMenu profile() {
        return profile;
    }

    public HubCommand hubCommand() {
        return hubCommand;
    }

    // config.yml is read and written as UTF-8 on every version (see Utf8Yaml).
    @Override
    public FileConfiguration getConfig() {
        if (config == null) reloadConfig();
        return config;
    }

    @Override
    public void reloadConfig() {
        config = Utf8Yaml.load(new File(getDataFolder(), "config.yml"));
        try (InputStream defaults = getResource("config.yml")) {
            if (defaults != null) config.setDefaults(Utf8Yaml.load(defaults));
        } catch (IOException exception) {
            getLogger().warning("Cannot read the default config.yml: " + exception.getMessage());
        }
    }

    @Override
    public void saveConfig() {
        try {
            Utf8Yaml.save((YamlConfiguration) getConfig(), new File(getDataFolder(), "config.yml"));
        } catch (IOException exception) {
            getLogger().severe("Cannot save config.yml: " + exception.getMessage());
        }
    }

    private void register(String name, TabExecutor executor) {
        PluginCommand command = getCommand(name);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    private void updateConfigDefaults() {
        try (InputStream resource = getResource("config.yml")) {
            if (resource != null) {
                YamlConfiguration defaults = Utf8Yaml.load(resource);
                // Destinations are user-owned: once the file has its own list, never re-add the example ones.
                YamlConfiguration onDisk = Utf8Yaml.load(new File(getDataFolder(), "config.yml"));
                if (onDisk.isConfigurationSection("navigator.destinations")) defaults.set("navigator.destinations", null);
                getConfig().setDefaults(defaults);
                getConfig().options().copyDefaults(true);
                saveConfig();
            }
        } catch (IOException ignored) { }
    }
}
