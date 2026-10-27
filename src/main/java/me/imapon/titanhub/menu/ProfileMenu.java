package me.imapon.titanhub.menu;

import me.imapon.titanhub.TitanHub;
import me.imapon.titanhub.util.Items;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class ProfileMenu {
    private final TitanHub plugin;

    public ProfileMenu(TitanHub plugin) {
        this.plugin = plugin;
    }

    public void openNextTick(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) open(player);
        });
    }

    private void open(Player player) {
        Inventory menu = Bukkit.createInventory(new MenuHolder(MenuHolder.Type.PROFILE), plugin.settings().profileSize, formatMenuText(player, plugin.settings().profileTitleTemplate));
        placeItem(menu, "profile-menu.playtime", player, 10, "CLOCK", "&bTempo di gioco", "&7%playtime%");
        placeItem(menu, "profile-menu.first-join", player, 12, "PLAYER_HEAD", "&aPrimo accesso", "&7%first-join%");
        placeItem(menu, "profile-menu.last-join", player, 14, "BOOK", "&eUltimo accesso", "&7%last-join%");
        placeItem(menu, "profile-menu.online", player, 16, "NETHER_STAR", "&dOnline", "&7Player connessi: &f%online%");
        player.openInventory(menu);
    }

    private void placeItem(Inventory menu, String path, Player player, int defaultSlot, String defaultMaterial, String defaultName, String... defaultLore) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(path);
        if (section == null) {
            String[] lore = new String[defaultLore.length];
            for (int index = 0; index < defaultLore.length; index++) lore[index] = formatMenuText(player, defaultLore[index]);
            menu.setItem(defaultSlot, Items.simpleItem(Items.materialByName(defaultMaterial), formatMenuText(player, defaultName), lore));
            return;
        }
        int slot = section.getInt("slot", defaultSlot);
        if (slot < 0 || slot >= menu.getSize()) return;
        List<String> lore = new ArrayList<String>();
        for (String line : section.getStringList("lore")) lore.add(formatMenuText(player, line));
        menu.setItem(slot, Items.simpleItem(Items.material(plugin.getConfig(), path + ".material"), formatMenuText(player, section.getString("name", "")), lore.toArray(new String[lore.size()])));
    }

    private String formatMenuText(Player player, String value) {
        return plugin.messages().format(player, value)
                .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%playtime%", formatPlaytime(player.getStatistic(playtimeStatistic())))
                .replace("%first-join%", formatDate(player.getFirstPlayed()))
                .replace("%last-join%", formatDate(player.getLastPlayed()));
    }

    // Renamed from PLAY_ONE_TICK to PLAY_ONE_MINUTE in 1.13 (it still counts ticks).
    private static Statistic playtimeStatistic() {
        try {
            return Statistic.valueOf("PLAY_ONE_MINUTE");
        } catch (IllegalArgumentException ignored) {
            return Statistic.valueOf("PLAY_ONE_TICK");
        }
    }

    private static String formatPlaytime(int ticks) {
        long totalMinutes = ticks / 1200L;
        long hours = totalMinutes / 60L;
        long minutes = totalMinutes % 60L;
        return hours + "h " + minutes + "m";
    }

    private static String formatDate(long time) {
        return time <= 0L ? "-" : new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(time));
    }
}
