package me.imapon.titanhub.config;

import me.imapon.titanhub.util.Items;
import me.imapon.titanhub.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A snapshot of config.yml; a new one is built on every reload. */
public final class HubSettings {
    public final boolean spawnOnJoin;
    public final long refreshSeconds;
    public final boolean clearInventoryOnJoin;

    public final boolean hubCommandEnabled;
    public final long hubCooldownMillis;

    public final boolean preventDamage;
    public final boolean preventHungerLoss;
    public final boolean preventItemDrop;
    public final boolean preventBlockBreak;
    public final boolean preventBlockPlace;
    public final boolean preventInteractions;
    public final boolean preventWeather;

    public final boolean launchPadsEnabled;
    public final Material launchPadMaterial;
    public final double launchPadStrength;
    public final double launchPadVerticalBoost;
    public final Sound launchPadSound;
    public final boolean doubleJumpEnabled;
    public final double doubleJumpStrength;
    public final Sound doubleJumpSound;
    public final boolean enderbuttEnabled;
    public final long enderbuttCooldownMillis;
    public final double enderbuttStrength;
    public final double enderbuttVerticalBoost;
    public final Sound enderbuttSound;

    public final boolean navigatorEnabled;
    public final int navigatorSlot;
    public final ItemStack navigatorItem;
    public final boolean playerHiderEnabled;
    public final int playerHiderSlot;
    public final long playerHiderCooldownMillis;
    public final ItemStack playerHiderVisibleItem;
    public final ItemStack playerHiderHiddenItem;
    public final boolean profileEnabled;
    public final int profileSlot;
    public final ItemStack profileItem;
    public final int enderbuttSlot;
    public final ItemStack enderbuttItem;
    public final boolean pvpEnabled;
    public final int pvpSlot;
    public final ItemStack pvpItem;

    public final String navigatorTitle;
    public final int navigatorSize;
    public final boolean navigatorDecorationEnabled;
    public final ItemStack navigatorDecorationItem;
    public final List<Integer> navigatorDecorationSlots;
    public final Map<Integer, Destination> destinations;
    public final String profileTitleTemplate;
    public final int profileSize;

    public final boolean scoreboardEnabled;
    public final String scoreboardTitle;
    public final List<String> scoreboardLines;
    public final boolean tabEnabled;
    public final List<String> tabHeader;
    public final List<String> tabFooter;

    // Only the world name is stored: the world may be loaded later by Multiverse or similar plugins.
    private final String hubWorldName;
    private final Location hubLocationTemplate;

    public HubSettings(FileConfiguration config) {
        spawnOnJoin = config.getBoolean("settings.spawn-on-join");
        ConfigurationSection hubSection = config.getConfigurationSection("hub-location");
        if (hubSection != null && hubSection.isString("world")) {
            hubWorldName = hubSection.getString("world");
            hubLocationTemplate = new Location(null, hubSection.getDouble("x"), hubSection.getDouble("y"), hubSection.getDouble("z"), (float) hubSection.getDouble("yaw"), (float) hubSection.getDouble("pitch"));
        } else {
            hubWorldName = null;
            hubLocationTemplate = null;
        }
        refreshSeconds = Math.max(0L, config.getLong("settings.refresh-seconds", 5L));
        clearInventoryOnJoin = config.getBoolean("settings.clear-inventory-on-join");

        hubCommandEnabled = config.getBoolean("settings.hub-command.enabled", true);
        hubCooldownMillis = Math.max(0L, config.getLong("settings.hub-command.cooldown-seconds", 3L)) * 1000L;

        preventDamage = config.getBoolean("settings.prevent-damage");
        preventHungerLoss = config.getBoolean("settings.prevent-hunger-loss");
        preventItemDrop = config.getBoolean("settings.prevent-item-drop");
        preventBlockBreak = config.getBoolean("settings.prevent-block-break");
        preventBlockPlace = config.getBoolean("settings.prevent-block-place");
        preventInteractions = config.getBoolean("settings.prevent-interactions", true);
        preventWeather = config.getBoolean("settings.prevent-weather");

        launchPadsEnabled = config.getBoolean("launch-pads.enabled");
        launchPadMaterial = Items.material(config, "launch-pads.material");
        launchPadStrength = config.getDouble("launch-pads.strength");
        launchPadVerticalBoost = config.getDouble("launch-pads.vertical-boost");
        launchPadSound = Items.sound(config, "launch-pads.sound");
        doubleJumpEnabled = config.getBoolean("double-jump.enabled");
        doubleJumpStrength = config.getDouble("double-jump.strength");
        doubleJumpSound = Items.sound(config, "double-jump.sound");
        enderbuttEnabled = config.getBoolean("enderbutt.enabled") && config.getBoolean("items.enderbutt.enabled");
        enderbuttCooldownMillis = Math.max(0L, config.getLong("enderbutt.cooldown-millis", 350L));
        enderbuttStrength = config.getDouble("enderbutt.strength", 1.35D);
        enderbuttVerticalBoost = config.getDouble("enderbutt.vertical-boost", 0.55D);
        enderbuttSound = Items.sound(config, "enderbutt.sound");

        navigatorEnabled = config.getBoolean("items.navigator.enabled");
        navigatorSlot = config.getInt("items.navigator.slot");
        navigatorItem = Items.configuredItem(config, "items.navigator", "material");
        playerHiderEnabled = config.getBoolean("items.player-hider.enabled");
        playerHiderSlot = config.getInt("items.player-hider.slot");
        playerHiderCooldownMillis = Math.max(0L, config.getLong("items.player-hider.cooldown-seconds", 5L)) * 1000L;
        playerHiderVisibleItem = Items.configuredItem(config, "items.player-hider", "visible-material");
        playerHiderHiddenItem = Items.configuredItem(config, "items.player-hider", "hidden-material");
        profileEnabled = config.getBoolean("items.profile.enabled");
        profileSlot = config.getInt("items.profile.slot");
        profileItem = Items.configuredItem(config, "items.profile", "material");
        enderbuttSlot = config.getInt("items.enderbutt.slot", 8);
        enderbuttItem = Items.configuredItem(config, "items.enderbutt", "material");
        pvpEnabled = config.getBoolean("items.pvp.enabled", true);
        pvpSlot = config.getInt("items.pvp.slot", 1);
        pvpItem = Items.configuredItem(config, "items.pvp", "material");

        navigatorTitle = Text.color(config.getString("navigator.title"));
        int size = Math.max(9, Math.min(54, config.getInt("navigator.size", 27)));
        navigatorSize = size - size % 9;
        ConfigurationSection decoration = config.getConfigurationSection("navigator.decoration");
        navigatorDecorationEnabled = decoration == null || decoration.getBoolean("enabled", true);
        if (decoration == null) {
            navigatorDecorationItem = Items.simpleItem(Items.materialByName("CYAN_STAINED_GLASS_PANE"), "&r ");
            navigatorDecorationItem.setDurability((short) 9);
            navigatorDecorationSlots = defaultDecorationSlots(navigatorSize);
        } else {
            navigatorDecorationItem = Items.item(config, "navigator.decoration.material", config.getString("navigator.decoration.name", "&r "), config.getStringList("navigator.decoration.lore"));
            navigatorDecorationSlots = new ArrayList<Integer>(config.getIntegerList("navigator.decoration.slots"));
        }
        destinations = Collections.unmodifiableMap(loadDestinations(config, navigatorSize));
        profileTitleTemplate = config.getString("profile-menu.title", "&8Profilo: %player%");
        int profileMenuSize = Math.max(9, Math.min(54, config.getInt("profile-menu.size", 27)));
        profileSize = profileMenuSize - profileMenuSize % 9;

        scoreboardEnabled = config.getBoolean("settings.scoreboard.enabled");
        scoreboardTitle = config.getString("settings.scoreboard.title", "");
        scoreboardLines = new ArrayList<String>(config.getStringList("settings.scoreboard.lines"));
        tabEnabled = config.getBoolean("settings.tab.enabled", true);
        tabHeader = new ArrayList<String>(config.getStringList("settings.tab.header"));
        tabFooter = new ArrayList<String>(config.getStringList("settings.tab.footer"));
    }

    public boolean isHubWorld(World world) {
        return hubWorldName != null && world != null && world.getName().equals(hubWorldName);
    }

    /** The hub spawn, or null while it is not set or its world is not loaded. */
    public Location hubLocation() {
        if (hubWorldName == null || hubLocationTemplate == null) return null;
        World world = Bukkit.getWorld(hubWorldName);
        if (world == null) return null;
        Location location = hubLocationTemplate.clone();
        location.setWorld(world);
        return location;
    }

    private static Map<Integer, Destination> loadDestinations(FileConfiguration config, int navigatorSize) {
        Map<Integer, Destination> destinations = new HashMap<Integer, Destination>();
        ConfigurationSection section = config.getConfigurationSection("navigator.destinations");
        if (section == null) return destinations;
        for (String key : section.getKeys(false)) {
            String path = "navigator.destinations." + key;
            if (key.equalsIgnoreCase("spawn")) continue;
            Destination destination = new Destination(config.getInt(path + ".slot"), Items.configuredItem(config, path, "material"), config.getString(path + ".action", "COMMAND").equalsIgnoreCase("SERVER"), config.getString(path + ".value", ""));
            if (destination.getSlot() >= 0 && destination.getSlot() < navigatorSize) destinations.put(destination.getSlot(), destination);
        }
        return destinations;
    }

    private static List<Integer> defaultDecorationSlots(int navigatorSize) {
        List<Integer> slots = new ArrayList<Integer>();
        for (int slot = 0; slot < navigatorSize; slot++) {
            int row = slot / 9;
            int column = slot % 9;
            if (row == 0 || row == navigatorSize / 9 - 1 || column == 0 || column == 8) slots.add(slot);
        }
        return slots;
    }
}
