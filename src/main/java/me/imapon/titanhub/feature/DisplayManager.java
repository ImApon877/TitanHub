package me.imapon.titanhub.feature;

import me.imapon.titanhub.TitanHub;
import me.imapon.titanhub.config.HubSettings;
import me.imapon.titanhub.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Sidebar scoreboard and tab header/footer, refreshed periodically. */
public final class DisplayManager {
    private static final String OBJECTIVE_NAME = "titanhub";
    private final TitanHub plugin;
    private final Map<UUID, Scoreboard> ownScoreboards = new HashMap<UUID, Scoreboard>();
    private BukkitTask refreshTask;

    public DisplayManager(TitanHub plugin) {
        this.plugin = plugin;
    }

    public void restartRefreshTask() {
        if (refreshTask != null) refreshTask.cancel();
        refreshTask = null;
        HubSettings settings = plugin.settings();
        if (settings.refreshSeconds <= 0L || (!settings.scoreboardEnabled && !settings.tabEnabled)) return;
        refreshTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!plugin.settings().isHubWorld(player.getWorld())) continue;
                updateScoreboard(player);
                updateTab(player);
            }
        }, settings.refreshSeconds * 20L, settings.refreshSeconds * 20L);
    }

    public void updateScoreboard(Player player) {
        HubSettings settings = plugin.settings();
        if (!settings.scoreboardEnabled) return;
        // Reuse the player's own board on refresh so it is not rebuilt from scratch every time.
        Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();
        Scoreboard board = player.getScoreboard();
        if (board == null || board == main) {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
            ownScoreboards.put(player.getUniqueId(), board);
        }
        // Boards created by TitanHub mirror the main scoreboard teams, so rank prefixes and nametags keep working.
        if (board == ownScoreboards.get(player.getUniqueId())) syncTeams(main, board);
        Objective previous = board.getObjective(OBJECTIVE_NAME);
        if (previous != null) previous.unregister();
        Objective objective = board.registerNewObjective(OBJECTIVE_NAME, "dummy");
        objective.setDisplayName(Text.truncateLegacy(Text.color(settings.scoreboardTitle), 32));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        int score = Math.min(15, settings.scoreboardLines.size());
        Set<String> entries = new HashSet<String>();
        int duplicate = 0;
        for (String line : settings.scoreboardLines) {
            if (score <= 0) break;
            String entry = uniqueEntry(plugin.messages().format(player, line.replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()))), entries, duplicate++);
            objective.getScore(entry).setScore(score--);
        }
        player.setScoreboard(board);
    }

    public void clearScoreboard(Player player) {
        Objective objective = player.getScoreboard().getObjective(OBJECTIVE_NAME);
        if (objective != null) objective.unregister();
    }

    public void updateTab(Player player) {
        HubSettings settings = plugin.settings();
        if (!settings.tabEnabled) return;
        // Not part of the 1.8 Bukkit API, hence the reflection.
        try {
            Method method = Player.class.getMethod("setPlayerListHeaderFooter", String.class, String.class);
            method.invoke(player, formatTab(settings.tabHeader, player), formatTab(settings.tabFooter, player));
        } catch (ReflectiveOperationException ignored) { }
    }

    public void forget(UUID uuid) {
        ownScoreboards.remove(uuid);
    }

    private void syncTeams(Scoreboard source, Scoreboard target) {
        for (Team copy : new ArrayList<Team>(target.getTeams())) {
            if (source.getTeam(copy.getName()) == null) copy.unregister();
        }
        for (Team team : source.getTeams()) {
            Team copy = target.getTeam(team.getName());
            if (copy == null) copy = target.registerNewTeam(team.getName());
            copy.setDisplayName(team.getDisplayName());
            copy.setPrefix(team.getPrefix());
            copy.setSuffix(team.getSuffix());
            copy.setAllowFriendlyFire(team.allowFriendlyFire());
            copy.setCanSeeFriendlyInvisibles(team.canSeeFriendlyInvisibles());
            Set<String> entries = team.getEntries();
            for (String entry : new ArrayList<String>(copy.getEntries())) {
                if (!entries.contains(entry)) copy.removeEntry(entry);
            }
            for (String entry : entries) {
                if (!copy.hasEntry(entry)) copy.addEntry(entry);
            }
        }
    }

    private String formatTab(List<String> lines, Player player) {
        StringBuilder text = new StringBuilder();
        for (String line : lines) {
            if (text.length() > 0) text.append('\n');
            text.append(plugin.messages().format(player, line.replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()))));
        }
        return text.toString();
    }

    // Scoreboard entries must be unique: identical lines get invisible color codes appended.
    private static String uniqueEntry(String entry, Set<String> entries, int duplicate) {
        String base = Text.truncateLegacy(entry, 36);
        String candidate = base;
        if (entries.contains(candidate)) candidate = Text.truncateLegacy(base, 36) + ChatColor.RESET + ChatColor.values()[duplicate % ChatColor.values().length];
        while (!entries.add(candidate)) candidate = Text.truncateLegacy(base, 34) + ChatColor.RESET + ChatColor.values()[(++duplicate) % ChatColor.values().length] + ChatColor.RESET;
        return Text.truncateLegacy(candidate, 40);
    }
}
