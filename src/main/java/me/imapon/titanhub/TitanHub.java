package me.imapon.titanhub;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class TitanHub extends JavaPlugin implements Listener {
    private static final long PVP_COMBAT_MILLIS = 10000L;
    private static final long PVP_REENTRY_MILLIS = 3000L;
    private final Set<UUID> hiddenPlayers = new HashSet<UUID>();
    private final Map<UUID, Set<UUID>> hiddenByTitanHub = new HashMap<UUID, Set<UUID>>();
    private final Set<UUID> doubleJumpFlightEnabled = new HashSet<UUID>();
    private final Set<UUID> pvpCountdown = new HashSet<UUID>();
    private final Set<UUID> pvpPlayers = new HashSet<UUID>();
    private final Set<UUID> pvpExitCountdown = new HashSet<UUID>();
    private final Map<UUID, Long> pvpCombatUntil = new HashMap<UUID, Long>();
    private final Map<UUID, Long> pvpReentryCooldown = new HashMap<UUID, Long>();
    private final Set<UUID> restoreHubItemsAfterDeath = new HashSet<UUID>();
    private final Set<UUID> launchPadCooldown = new HashSet<UUID>();
    private final Map<Integer, Destination> destinations = new HashMap<Integer, Destination>();
    private final Map<UUID, Long> hubCooldowns = new HashMap<UUID, Long>();
    private final Map<UUID, Long> enderbuttCooldowns = new HashMap<UUID, Long>();
    private final Map<UUID, Long> playerHiderCooldowns = new HashMap<UUID, Long>();
    private final Map<UUID, Scoreboard> ownScoreboards = new HashMap<UUID, Scoreboard>();
    private FileConfiguration messages;
    private Method placeholderMethod;
    private boolean hubCommandEnabled;
    private boolean enderbuttEnabled;
    private long hubCooldownMillis;
    private long enderbuttCooldownMillis;
    private long playerHiderCooldownMillis;
    private double enderbuttStrength;
    private double enderbuttVerticalBoost;
    private Sound enderbuttSound;
    private boolean launchPadsEnabled;
    private boolean doubleJumpEnabled;
    private boolean preventDamage;
    private boolean preventHungerLoss;
    private boolean preventItemDrop;
    private boolean preventBlockBreak;
    private boolean preventBlockPlace;
    private boolean preventInteractions;
    private boolean preventWeather;
    private Material launchPadMaterial;
    private double launchPadStrength;
    private double launchPadVerticalBoost;
    private double doubleJumpStrength;
    private Sound launchPadSound;
    private Sound doubleJumpSound;
    private String navigatorTitle;
    private boolean navigatorDecorationEnabled;
    private ItemStack navigatorDecorationItem;
    private List<Integer> navigatorDecorationSlots;
    private String profileTitleTemplate;
    private int profileSize;
    private boolean clearInventoryOnJoin;
    private boolean navigatorEnabled;
    private boolean playerHiderEnabled;
    private boolean profileEnabled;
    private int navigatorSlot;
    private int playerHiderSlot;
    private int profileSlot;
    private int enderbuttSlot;
    private boolean pvpEnabled;
    private int pvpSlot;
    private int navigatorSize;
    private ItemStack navigatorItem;
    private ItemStack playerHiderVisibleItem;
    private ItemStack playerHiderHiddenItem;
    private ItemStack profileItem;
    private ItemStack enderbuttItem;
    private ItemStack pvpItem;
    private boolean scoreboardEnabled;
    private String scoreboardTitle;
    private List<String> scoreboardLines;
    private boolean tabEnabled;
    private List<String> tabHeader;
    private List<String> tabFooter;
    private boolean spawnOnJoin;
    // Only the world name is stored: the world may be loaded later by Multiverse or similar plugins.
    private Location hubLocationTemplate;
    private String hubWorldName;
    private long refreshSeconds;
    private BukkitTask refreshTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        updateConfigDefaults();
        loadMessages();
        loadCache();
        restartRefreshTask();
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getCommand("titanhub").setExecutor((sender, command, label, args) -> {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                // Remove items matching the old names before they change, otherwise they stay in inventories.
                for (Player online : Bukkit.getOnlinePlayers()) removeHubItems(online);
                reloadConfig();
                updateConfigDefaults();
                loadMessages();
                loadCache();
                restartRefreshTask();
                refreshOnlinePlayers();
                sender.sendMessage(message("reload", sender instanceof Player ? (Player) sender : null));
                return true;
            }
            sender.sendMessage(message("usage", sender instanceof Player ? (Player) sender : null));
            return true;
        });
        getCommand("titanhub").setTabCompleter((sender, command, label, args) ->
                args.length == 1 && sender.hasPermission("titanhub.admin") && "reload".startsWith(args[0].toLowerCase())
                        ? Collections.singletonList("reload") : Collections.<String>emptyList());
        getCommand("hub").setTabCompleter((sender, command, label, args) -> Collections.<String>emptyList());
        getCommand("sethub").setTabCompleter((sender, command, label, args) -> Collections.<String>emptyList());
        getCommand("hub").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage(message("player-only", null));
                return true;
            }
            Player player = (Player) sender;
            if (!hubCommandEnabled) return true;
            if (hubLocation() == null) {
                player.sendMessage(message("hub-not-set", player));
                return true;
            }
            long now = System.currentTimeMillis();
            long remaining = hubCooldownMillis - (now - (hubCooldowns.containsKey(player.getUniqueId()) ? hubCooldowns.get(player.getUniqueId()) : 0L));
            if (remaining > 0L && !player.hasPermission("titanhub.admin")) {
                player.sendMessage(message("hub-cooldown", player).replace("%seconds%", String.valueOf((remaining + 999L) / 1000L)));
                return true;
            }
            hubCooldowns.put(player.getUniqueId(), now);
            if (sendToHub(player, false)) player.sendMessage(message("hub-teleport", player));
            else player.sendMessage(message("hub-teleport-failed", player));
            return true;
        });
        getCommand("sethub").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage(message("player-only", null));
                return true;
            }
            Player player = (Player) sender;
            Location location = player.getLocation();
            getConfig().set("hub-location.world", location.getWorld().getName());
            getConfig().set("hub-location.x", location.getX());
            getConfig().set("hub-location.y", location.getY());
            getConfig().set("hub-location.z", location.getZ());
            getConfig().set("hub-location.yaw", location.getYaw());
            getConfig().set("hub-location.pitch", location.getPitch());
            saveConfig();
            loadCache();
            player.sendMessage(message("hub-set", player).replace("%world%", location.getWorld().getName()));
            return true;
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.setJoinMessage(message("join", player));
        if (spawnOnJoin && hubLocation() != null) {
            sendToHub(player, true);
            applyHiddenPlayersToJoiner(player);
            return;
        }
        // The player inventory is fully initialized on the next server tick.
        Bukkit.getScheduler().runTask(this, () -> {
            if (!player.isOnline()) return;
            giveHubItems(player, true);
            updateScoreboard(player);
            updateTab(player);
            refreshDoubleJump(player);
            applyHiddenPlayersToJoiner(player);
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(message("quit", event.getPlayer()));
        hiddenPlayers.remove(event.getPlayer().getUniqueId());
        hiddenByTitanHub.remove(event.getPlayer().getUniqueId());
        for (Set<UUID> hidden : hiddenByTitanHub.values()) hidden.remove(event.getPlayer().getUniqueId());
        revokeDoubleJumpFlight(event.getPlayer());
        hubCooldowns.remove(event.getPlayer().getUniqueId());
        enderbuttCooldowns.remove(event.getPlayer().getUniqueId());
        playerHiderCooldowns.remove(event.getPlayer().getUniqueId());
        restoreHubItemsAfterDeath.remove(event.getPlayer().getUniqueId());
        clearPvpState(event.getPlayer().getUniqueId());
        pvpReentryCooldown.remove(event.getPlayer().getUniqueId());
        ownScoreboards.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (!isHubWorld(player.getWorld())) return;
        if (navigatorEnabled && isHubItem(item, navigatorItem, "navigator")) {
            event.setCancelled(true);
            openNavigator(player);
        } else if (playerHiderEnabled && isHubItem(item, hiddenPlayers.contains(player.getUniqueId()) ? playerHiderHiddenItem : playerHiderVisibleItem, "player-hider")) {
            event.setCancelled(true);
            togglePlayers(player);
        } else if (profileEnabled && isHubItem(item, profileItem, "profile")) {
            event.setCancelled(true);
            openProfileNextTick(player);
        } else if (enderbuttEnabled && isHubItem(item, enderbuttItem, "enderbutt")) {
            event.setCancelled(true);
            useEnderbutt(player);
        } else if (pvpEnabled && isHubItem(item, pvpItem, "pvp")) {
            event.setCancelled(true);
            startPvpCountdown(player);
        } else if (preventInteractions && event.getAction() == Action.RIGHT_CLICK_BLOCK && !player.hasPermission("titanhub.admin")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (isManagedHubItem(event.getCurrentItem()) || isManagedHubItem(event.getCursor())
                || (event.getHotbarButton() >= 0 && isManagedHubItem(player.getInventory().getItem(event.getHotbarButton())))) {
            event.setCancelled(true);
            return;
        }
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (!(holder instanceof MenuHolder)) return;
        event.setCancelled(true);
        if (((MenuHolder) holder).type != MenuType.NAVIGATOR) return;
        if (event.getRawSlot() < 0 || event.getRawSlot() >= event.getView().getTopInventory().getSize()) return;
        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;
        Destination destination = destinations.get(event.getRawSlot());
        if (destination != null) {
            player.closeInventory();
            runDestination(player, destination);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (isManagedHubItem(event.getOldCursor()) || event.getView().getTopInventory().getHolder() instanceof MenuHolder) event.setCancelled(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        refreshDoubleJump(player);
        if (!launchPadsEnabled || !isHubWorld(player.getWorld()) || sameBlock(event)) return;
        if (event.getTo().getBlock().getRelative(0, -1, 0).getType() != launchPadMaterial || !launchPadCooldown.add(player.getUniqueId())) return;
        player.setVelocity(new Vector(player.getVelocity().getX() * launchPadStrength, launchPadVerticalBoost, player.getVelocity().getZ() * launchPadStrength));
        play(player, launchPadSound);
        Bukkit.getScheduler().runTaskLater(this, () -> launchPadCooldown.remove(player.getUniqueId()), 10L);
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (!doubleJumpEnabled || !isHubWorld(player.getWorld()) || player.getGameMode() == GameMode.CREATIVE) return;
        if (!doubleJumpFlightEnabled.contains(player.getUniqueId())) return;
        event.setCancelled(true);
        player.setAllowFlight(false);
        doubleJumpFlightEnabled.remove(player.getUniqueId());
        Vector direction = player.getLocation().getDirection().normalize().multiply(doubleJumpStrength);
        direction.setY(Math.max(0.45D, direction.getY()));
        player.setVelocity(direction);
        play(player, doubleJumpSound);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if (!isHubWorld(event.getPlayer().getWorld())) revokeDoubleJumpFlight(event.getPlayer());
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (preventDamage && event.getEntity() instanceof Player && isHubWorld(event.getEntity().getWorld()) && !isMutualPvpDamage(event)) event.setCancelled(true);
    }

    @EventHandler
    public void onPvpDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !isHubWorld(event.getEntity().getWorld())) return;
        Player attacker = (Player) event.getDamager();
        if (!pvpPlayers.contains(attacker.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            if (!pvpPlayers.contains(victim.getUniqueId())) {
                event.setCancelled(true);
                return;
            }
            long until = System.currentTimeMillis() + PVP_COMBAT_MILLIS;
            pvpCombatUntil.put(attacker.getUniqueId(), until);
            pvpCombatUntil.put(victim.getUniqueId(), until);
        }
    }

    @EventHandler
    public void onFood(FoodLevelChangeEvent event) {
        if (preventHungerLoss && event.getEntity() instanceof Player && isHubWorld(event.getEntity().getWorld())) event.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (pvpPlayers.contains(event.getPlayer().getUniqueId()) && event.getPlayer().getInventory().getHeldItemSlot() == pvpSlot) {
            event.setCancelled(true);
            startPvpExit(event.getPlayer());
            return;
        }
        if (isManagedHubItem(event.getItemDrop().getItemStack()) || (preventItemDrop && isHubWorld(event.getPlayer().getWorld()) && !event.getPlayer().hasPermission("titanhub.admin"))) event.setCancelled(true);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        if (pvpPlayers.contains(victim.getUniqueId())) {
            // PvP gear must never be dropped in the hub; the player gets the hub items back on respawn.
            event.getDrops().clear();
            event.setDroppedExp(0);
            if (event.getKeepInventory()) victim.getInventory().setArmorContents(null);
            clearPvpState(victim.getUniqueId());
            pvpReentryCooldown.put(victim.getUniqueId(), System.currentTimeMillis() + PVP_REENTRY_MILLIS);
            restoreHubItemsAfterDeath.add(victim.getUniqueId());
            return;
        }
        boolean removedHubItem = false;
        for (java.util.Iterator<ItemStack> iterator = event.getDrops().iterator(); iterator.hasNext();) {
            if (isManagedHubItem(iterator.next())) {
                iterator.remove();
                removedHubItem = true;
            }
        }
        if (removedHubItem) restoreHubItemsAfterDeath.add(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location hubLocation = hubLocation();
        if (hubLocation != null) event.setRespawnLocation(hubLocation);
        restoreHubItemsAfterDeath.remove(player.getUniqueId());
        Bukkit.getScheduler().runTask(this, () -> {
            if (player.isOnline()) {
                giveHubItems(player, false);
                updateScoreboard(player);
                updateTab(player);
            }
        });
    }

    @EventHandler
    public void onHeldItemChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (!pvpEnabled || !isHubWorld(player.getWorld())) return;
        if (pvpPlayers.contains(player.getUniqueId()) && event.getNewSlot() == pvpSlot) {
            pvpExitCountdown.remove(player.getUniqueId());
        } else if (!pvpPlayers.contains(player.getUniqueId()) && event.getNewSlot() == pvpSlot) Bukkit.getScheduler().runTask(this, () -> startPvpCountdown(player));
        else if (pvpCountdown.remove(player.getUniqueId())) player.sendMessage(message("pvp-cancelled", player));
        else if (pvpPlayers.contains(player.getUniqueId()) && event.getNewSlot() != pvpSlot) startPvpExit(player);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (preventBlockBreak && isHubWorld(event.getPlayer().getWorld()) && !event.getPlayer().hasPermission("titanhub.admin")) event.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (preventBlockPlace && isHubWorld(event.getPlayer().getWorld()) && !event.getPlayer().hasPermission("titanhub.admin")) event.setCancelled(true);
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (preventInteractions && isHubWorld(event.getPlayer().getWorld()) && !event.getPlayer().hasPermission("titanhub.admin")) event.setCancelled(true);
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (preventInteractions && isHubWorld(event.getPlayer().getWorld()) && !event.getPlayer().hasPermission("titanhub.admin")) event.setCancelled(true);
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (preventInteractions && isHubWorld(event.getPlayer().getWorld()) && !event.getPlayer().hasPermission("titanhub.admin")) event.setCancelled(true);
    }

    @EventHandler
    public void onIgnite(BlockIgniteEvent event) {
        if (preventBlockPlace && isHubWorld(event.getBlock().getWorld())) event.setCancelled(true);
    }

    @EventHandler
    public void onBurn(BlockBurnEvent event) {
        if (preventBlockPlace && isHubWorld(event.getBlock().getWorld())) event.setCancelled(true);
    }

    @EventHandler
    public void onSpread(BlockSpreadEvent event) {
        if (preventBlockPlace && isHubWorld(event.getBlock().getWorld())) event.setCancelled(true);
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        if (preventBlockBreak && isHubWorld(event.getLocation().getWorld())) event.setCancelled(true);
    }

    @EventHandler
    public void onWeather(WeatherChangeEvent event) {
        if (preventWeather && isHubWorld(event.getWorld()) && event.toWeatherState()) event.setCancelled(true);
    }

    private void giveHubItems(Player player, boolean clearInventory) {
        if (clearInventory && clearInventoryOnJoin) player.getInventory().clear();
        removeHubItems(player);
        giveCachedItem(player, navigatorEnabled, navigatorSlot, navigatorItem);
        giveCachedItem(player, playerHiderEnabled, playerHiderSlot, hiddenPlayers.contains(player.getUniqueId()) ? playerHiderHiddenItem : playerHiderVisibleItem);
        giveCachedItem(player, profileEnabled, profileSlot, profileItem);
        giveCachedItem(player, enderbuttEnabled, enderbuttSlot, enderbuttItem);
        giveCachedItem(player, pvpEnabled && !pvpPlayers.contains(player.getUniqueId()), pvpSlot, pvpItem);
    }

    private boolean sendToHub(Player player, boolean clearInventory) {
        Location hubLocation = hubLocation();
        if (hubLocation == null || !player.teleport(hubLocation)) return false;
        refreshDoubleJump(player);
        giveHubItems(player, clearInventory);
        updateScoreboard(player);
        updateTab(player);
        return true;
    }

    private void giveCachedItem(Player player, boolean enabled, int slot, ItemStack item) {
        if (enabled && item != null && slot >= 0 && slot < player.getInventory().getSize()) player.getInventory().setItem(slot, item.clone());
    }

    private void openNavigator(Player player) {
        Inventory menu = Bukkit.createInventory(new MenuHolder(MenuType.NAVIGATOR), navigatorSize, navigatorTitle);
        if (navigatorDecorationEnabled && navigatorDecorationItem != null) {
            for (Integer slot : navigatorDecorationSlots) {
                if (slot != null && slot >= 0 && slot < navigatorSize) menu.setItem(slot, navigatorDecorationItem.clone());
            }
        }
        for (Destination destination : destinations.values()) {
            if (destination.slot >= 0 && destination.slot < navigatorSize) menu.setItem(destination.slot, destination.icon.clone());
        }
        player.openInventory(menu);
    }

    private void openProfile(Player player) {
        Inventory menu = Bukkit.createInventory(new MenuHolder(MenuType.PROFILE), profileSize, profileTitle(player));
        placeProfileItem(menu, "profile-menu.playtime", player, 10, "CLOCK", "&bTempo di gioco", "&7%playtime%");
        placeProfileItem(menu, "profile-menu.first-join", player, 12, "PLAYER_HEAD", "&aPrimo accesso", "&7%first-join%");
        placeProfileItem(menu, "profile-menu.last-join", player, 14, "BOOK", "&eUltimo accesso", "&7%last-join%");
        placeProfileItem(menu, "profile-menu.online", player, 16, "NETHER_STAR", "&dOnline", "&7Player connessi: &f%online%");
        player.openInventory(menu);
    }

    private void openProfileNextTick(Player player) {
        Bukkit.getScheduler().runTask(this, () -> {
            if (player.isOnline()) openProfile(player);
        });
    }

    private void runDestination(Player player, Destination destination) {
        if (destination.server) connect(player, destination.value);
        else Bukkit.dispatchCommand(player, destination.value.replace("%player%", player.getName()));
    }

    private void connect(Player player, String server) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);
            out.writeUTF("Connect");
            out.writeUTF(server);
            player.sendPluginMessage(this, "BungeeCord", bytes.toByteArray());
        } catch (IOException ignored) {
            player.sendMessage(message("proxy-connect-failed", player));
        }
    }

    private void togglePlayers(Player player) {
        long now = System.currentTimeMillis();
        Long previousUse = playerHiderCooldowns.get(player.getUniqueId());
        long remaining = playerHiderCooldownMillis - (now - (previousUse == null ? 0L : previousUse));
        if (remaining > 0L) {
            player.sendMessage(message("player-hider-cooldown", player).replace("%seconds%", String.valueOf((remaining + 999L) / 1000L)));
            return;
        }
        playerHiderCooldowns.put(player.getUniqueId(), now);
        boolean hidden = !hiddenPlayers.contains(player.getUniqueId());
        if (hidden) hidePlayers(player); else showPlayers(player);
        giveHubItems(player, false);
        player.sendMessage(message(hidden ? "players-hidden" : "players-shown", player));
    }

    private void useEnderbutt(Player player) {
        long now = System.currentTimeMillis();
        Long previousUse = enderbuttCooldowns.get(player.getUniqueId());
        if (previousUse != null && now - previousUse < enderbuttCooldownMillis) return;
        enderbuttCooldowns.put(player.getUniqueId(), now);
        Vector velocity = player.getLocation().getDirection().normalize().multiply(enderbuttStrength);
        velocity.setY(velocity.getY() + enderbuttVerticalBoost);
        player.setVelocity(velocity);
        play(player, enderbuttSound);
    }

    private void updateScoreboard(Player player) {
        if (!scoreboardEnabled) return;
        // Reuse the player's own board on refresh so it is not rebuilt from scratch every time.
        Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();
        Scoreboard board = player.getScoreboard();
        if (board == null || board == main) {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
            ownScoreboards.put(player.getUniqueId(), board);
        }
        // Boards created by TitanHub mirror the main scoreboard teams, so rank prefixes and nametags keep working.
        if (board == ownScoreboards.get(player.getUniqueId())) syncTeams(main, board);
        Objective previous = board.getObjective("titanhub");
        if (previous != null) previous.unregister();
        Objective objective = board.registerNewObjective("titanhub", "dummy");
        objective.setDisplayName(truncateLegacy(color(scoreboardTitle), 32));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        int score = Math.min(15, scoreboardLines.size());
        Set<String> entries = new HashSet<String>();
        int duplicate = 0;
        for (String line : scoreboardLines) {
            if (score <= 0) break;
            String entry = uniqueScoreboardEntry(format(player, line.replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()))), entries, duplicate++);
            objective.getScore(entry).setScore(score--);
        }
        player.setScoreboard(board);
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

    private ItemStack item(String materialPath, String name, List<String> lore) {
        ItemStack stack = new ItemStack(material(materialPath));
        int separator = materialPath.lastIndexOf('.');
        if (separator > 0) stack.setDurability((short) getConfig().getInt(materialPath.substring(0, separator) + ".data", 0));
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(color(name));
        List<String> coloredLore = new ArrayList<String>();
        for (String line : lore) coloredLore.add(color(line));
        meta.setLore(coloredLore);
        stack.setItemMeta(meta);
        return stack;
    }

    private ItemStack simpleItem(Material material, String name, String... lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(color(name));
        List<String> coloredLore = new ArrayList<String>();
        for (String line : lore) coloredLore.add(color(line));
        meta.setLore(coloredLore);
        stack.setItemMeta(meta);
        return stack;
    }

    private String formatPlaytime(int ticks) {
        long totalMinutes = ticks / 1200L;
        long hours = totalMinutes / 60L;
        long minutes = totalMinutes % 60L;
        return hours + "h " + minutes + "m";
    }

    private Material material(String path) {
        return materialByName(getConfig().getString(path, "STONE"));
    }

    private Material materialByName(String name) {
        String normalized = name == null ? "STONE" : name.toUpperCase();
        String modern = null;
        if (normalized.equals("SKULL_ITEM") || normalized.equals("PLAYER_HEAD")) modern = "PLAYER_HEAD";
        else if (normalized.equals("WATCH") || normalized.equals("CLOCK")) modern = "CLOCK";
        else if (normalized.equals("GRASS") || normalized.equals("GRASS_BLOCK")) modern = "GRASS_BLOCK";
        else if (normalized.equals("GOLD_PLATE") || normalized.equals("LIGHT_WEIGHTED_PRESSURE_PLATE")) modern = "LIGHT_WEIGHTED_PRESSURE_PLATE";
        else if (normalized.equals("INK_SACK") || normalized.equals("INK_SAC")) modern = "INK_SAC";
        else if (normalized.equals("WOOD_SWORD") || normalized.equals("WOODEN_SWORD")) modern = "WOODEN_SWORD";
        else if (normalized.equals("STAINED_GLASS_PANE") || normalized.equals("CYAN_STAINED_GLASS_PANE")) modern = "CYAN_STAINED_GLASS_PANE";
        Material material = modern == null ? null : Material.matchMaterial(modern);
        if (material == null) material = Material.matchMaterial(normalized);
        if (material == null && normalized.equals("PLAYER_HEAD")) material = Material.matchMaterial("SKULL_ITEM");
        if (material == null && normalized.equals("CLOCK")) material = Material.matchMaterial("WATCH");
        if (material == null && normalized.equals("GRASS_BLOCK")) material = Material.matchMaterial("GRASS");
        if (material == null && normalized.equals("LIGHT_WEIGHTED_PRESSURE_PLATE")) material = Material.matchMaterial("GOLD_PLATE");
        if (material == null && normalized.equals("INK_SAC")) material = Material.matchMaterial("INK_SACK");
        if (material == null && normalized.equals("WOODEN_SWORD")) material = Material.matchMaterial("WOOD_SWORD");
        if (material == null && normalized.equals("CYAN_STAINED_GLASS_PANE")) material = Material.matchMaterial("STAINED_GLASS_PANE");
        return material == null ? Material.STONE : material;
    }

    private void play(Player player, Sound sound) {
        if (sound != null) player.playSound(player.getLocation(), sound, 1F, 1F);
    }

    private String message(String path, Player player) {
        return format(player, messages.getString(path, ""));
    }

    private String color(String value) {
        return ChatColor.translateAlternateColorCodes('&', value == null ? "" : value);
    }

    private String format(Player player, String value) {
        String formatted = value == null ? "" : value.replace("%player%", player == null ? "" : player.getName());
        if (placeholderMethod != null && player != null) {
            try {
                formatted = (String) placeholderMethod.invoke(null, player, formatted);
            } catch (ReflectiveOperationException ignored) { }
        }
        return color(formatted);
    }

    private void loadMessages() {
        File file = new File(getDataFolder(), "messages.yml");
        if (!file.exists()) saveResource("messages.yml", false);
        messages = YamlConfiguration.loadConfiguration(file);
        try (InputStream resource = getResource("messages.yml")) {
            if (resource != null) {
                messages.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(resource, StandardCharsets.UTF_8)));
                messages.options().copyDefaults(true);
                try {
                    messages.save(file);
                } catch (IOException ignored) { }
            }
        } catch (IOException ignored) { }
        placeholderMethod = null;
        if (!getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) return;
        try {
            Class<?> api = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            placeholderMethod = api.getMethod("setPlaceholders", Player.class, String.class);
        } catch (ReflectiveOperationException ignored) { }
    }

    private void updateConfigDefaults() {
        try (InputStream resource = getResource("config.yml")) {
            if (resource != null) {
                YamlConfiguration defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(resource, StandardCharsets.UTF_8));
                // Destinations are user-owned: once the file has its own list, never re-add the example ones.
                YamlConfiguration onDisk = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
                if (onDisk.isConfigurationSection("navigator.destinations")) defaults.set("navigator.destinations", null);
                getConfig().setDefaults(defaults);
                getConfig().options().copyDefaults(true);
                saveConfig();
            }
        } catch (IOException ignored) { }
    }

    private void loadCache() {
        spawnOnJoin = getConfig().getBoolean("settings.spawn-on-join");
        ConfigurationSection hubSection = getConfig().getConfigurationSection("hub-location");
        if (hubSection != null && hubSection.isString("world")) {
            hubWorldName = hubSection.getString("world");
            hubLocationTemplate = new Location(null, hubSection.getDouble("x"), hubSection.getDouble("y"), hubSection.getDouble("z"), (float) hubSection.getDouble("yaw"), (float) hubSection.getDouble("pitch"));
        } else {
            hubWorldName = null;
            hubLocationTemplate = null;
        }
        refreshSeconds = Math.max(0L, getConfig().getLong("settings.refresh-seconds", 5L));
        launchPadsEnabled = getConfig().getBoolean("launch-pads.enabled");
        doubleJumpEnabled = getConfig().getBoolean("double-jump.enabled");
        hubCommandEnabled = getConfig().getBoolean("settings.hub-command.enabled", true);
        hubCooldownMillis = Math.max(0L, getConfig().getLong("settings.hub-command.cooldown-seconds", 3L)) * 1000L;
        enderbuttEnabled = getConfig().getBoolean("enderbutt.enabled") && getConfig().getBoolean("items.enderbutt.enabled");
        enderbuttCooldownMillis = Math.max(0L, getConfig().getLong("enderbutt.cooldown-millis", 350L));
        playerHiderCooldownMillis = Math.max(0L, getConfig().getLong("items.player-hider.cooldown-seconds", 5L)) * 1000L;
        enderbuttStrength = getConfig().getDouble("enderbutt.strength", 1.35D);
        enderbuttVerticalBoost = getConfig().getDouble("enderbutt.vertical-boost", 0.55D);
        enderbuttSound = sound("enderbutt.sound");
        clearInventoryOnJoin = getConfig().getBoolean("settings.clear-inventory-on-join");
        preventDamage = getConfig().getBoolean("settings.prevent-damage");
        preventHungerLoss = getConfig().getBoolean("settings.prevent-hunger-loss");
        preventItemDrop = getConfig().getBoolean("settings.prevent-item-drop");
        preventBlockBreak = getConfig().getBoolean("settings.prevent-block-break");
        preventBlockPlace = getConfig().getBoolean("settings.prevent-block-place");
        preventInteractions = getConfig().getBoolean("settings.prevent-interactions", true);
        preventWeather = getConfig().getBoolean("settings.prevent-weather");
        launchPadMaterial = material("launch-pads.material");
        launchPadStrength = getConfig().getDouble("launch-pads.strength");
        launchPadVerticalBoost = getConfig().getDouble("launch-pads.vertical-boost");
        doubleJumpStrength = getConfig().getDouble("double-jump.strength");
        launchPadSound = sound("launch-pads.sound");
        doubleJumpSound = sound("double-jump.sound");
        navigatorTitle = color(getConfig().getString("navigator.title"));
        navigatorSize = Math.max(9, Math.min(54, getConfig().getInt("navigator.size", 27)));
        navigatorSize -= navigatorSize % 9;
        ConfigurationSection decoration = getConfig().getConfigurationSection("navigator.decoration");
        navigatorDecorationEnabled = decoration == null || decoration.getBoolean("enabled", true);
        navigatorDecorationItem = decoration == null ? simpleItem(materialByName("CYAN_STAINED_GLASS_PANE"), "&r ") : item("navigator.decoration.material", getConfig().getString("navigator.decoration.name", "&r "), getConfig().getStringList("navigator.decoration.lore"));
        if (decoration == null) navigatorDecorationItem.setDurability((short) 9);
        navigatorDecorationSlots = decoration == null ? defaultNavigatorDecorationSlots() : new ArrayList<Integer>(getConfig().getIntegerList("navigator.decoration.slots"));
        profileTitleTemplate = getConfig().getString("profile-menu.title", "&8Profilo: %player%");
        profileSize = Math.max(9, Math.min(54, getConfig().getInt("profile-menu.size", 27)));
        profileSize -= profileSize % 9;
        navigatorEnabled = getConfig().getBoolean("items.navigator.enabled");
        playerHiderEnabled = getConfig().getBoolean("items.player-hider.enabled");
        profileEnabled = getConfig().getBoolean("items.profile.enabled");
        navigatorSlot = getConfig().getInt("items.navigator.slot");
        playerHiderSlot = getConfig().getInt("items.player-hider.slot");
        profileSlot = getConfig().getInt("items.profile.slot");
        enderbuttSlot = getConfig().getInt("items.enderbutt.slot", 8);
        pvpEnabled = getConfig().getBoolean("items.pvp.enabled", true);
        pvpSlot = getConfig().getInt("items.pvp.slot", 1);
        navigatorItem = configuredItem("items.navigator", "material", "navigator");
        playerHiderVisibleItem = configuredItem("items.player-hider", "visible-material", "player-hider");
        playerHiderHiddenItem = configuredItem("items.player-hider", "hidden-material", "player-hider");
        profileItem = configuredItem("items.profile", "material", "profile");
        enderbuttItem = configuredItem("items.enderbutt", "material", "enderbutt");
        pvpItem = configuredItem("items.pvp", "material", "pvp");
        scoreboardEnabled = getConfig().getBoolean("settings.scoreboard.enabled");
        scoreboardTitle = getConfig().getString("settings.scoreboard.title", "");
        scoreboardLines = new ArrayList<String>(getConfig().getStringList("settings.scoreboard.lines"));
        tabEnabled = getConfig().getBoolean("settings.tab.enabled", true);
        tabHeader = new ArrayList<String>(getConfig().getStringList("settings.tab.header"));
        tabFooter = new ArrayList<String>(getConfig().getStringList("settings.tab.footer"));

        destinations.clear();
        ConfigurationSection section = getConfig().getConfigurationSection("navigator.destinations");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            String path = "navigator.destinations." + key;
            if (key.equalsIgnoreCase("spawn")) continue;
            Destination destination = new Destination(getConfig().getInt(path + ".slot"), destinationItem(path), getConfig().getString(path + ".action", "COMMAND").equalsIgnoreCase("SERVER"), getConfig().getString(path + ".value", ""));
            if (destination.slot >= 0 && destination.slot < navigatorSize) destinations.put(destination.slot, destination);
        }
    }

    private boolean sameBlock(PlayerMoveEvent event) {
        return event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ();
    }

    private boolean isHubWorld(World world) {
        return hubWorldName != null && world != null && world.getName().equals(hubWorldName);
    }

    private Location hubLocation() {
        if (hubWorldName == null || hubLocationTemplate == null) return null;
        World world = Bukkit.getWorld(hubWorldName);
        if (world == null) return null;
        Location location = hubLocationTemplate.clone();
        location.setWorld(world);
        return location;
    }

    private void restartRefreshTask() {
        if (refreshTask != null) refreshTask.cancel();
        refreshTask = null;
        if (refreshSeconds <= 0L || (!scoreboardEnabled && !tabEnabled)) return;
        refreshTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!isHubWorld(player.getWorld())) continue;
                updateScoreboard(player);
                updateTab(player);
            }
        }, refreshSeconds * 20L, refreshSeconds * 20L);
    }

    private ItemStack configuredItem(String path, String materialKey, String identifier) {
        ItemStack stack = item(path + "." + materialKey, getConfig().getString(path + ".name"), getConfig().getStringList(path + ".lore"));
        applyHeadOwner(stack, getConfig().getString(path + ".head-owner", ""));
        return stack;
    }

    private ItemStack destinationItem(String path) {
        ItemStack stack = item(path + ".material", getConfig().getString(path + ".name"), getConfig().getStringList(path + ".lore"));
        applyHeadOwner(stack, getConfig().getString(path + ".head-owner", ""));
        return stack;
    }

    private void applyHeadOwner(ItemStack stack, String owner) {
        if (owner == null || owner.isEmpty() || !(stack.getItemMeta() instanceof SkullMeta)) return;
        stack.setDurability((short) 3);
        SkullMeta meta = (SkullMeta) stack.getItemMeta();
        meta.setOwner(owner);
        stack.setItemMeta(meta);
    }

    private String profileTitle(Player player) {
        return formatMenuText(player, profileTitleTemplate);
    }

    private void placeProfileItem(Inventory menu, String path, Player player, int defaultSlot, String defaultMaterial, String defaultName, String... defaultLore) {
        ConfigurationSection section = getConfig().getConfigurationSection(path);
        if (section == null) {
            String[] lore = new String[defaultLore.length];
            for (int index = 0; index < defaultLore.length; index++) lore[index] = formatMenuText(player, defaultLore[index]);
            menu.setItem(defaultSlot, simpleItem(materialByName(defaultMaterial), formatMenuText(player, defaultName), lore));
            return;
        }
        int slot = section.getInt("slot", defaultSlot);
        if (slot < 0 || slot >= menu.getSize()) return;
        List<String> lore = new ArrayList<String>();
        for (String line : section.getStringList("lore")) lore.add(formatMenuText(player, line));
        menu.setItem(slot, simpleItem(material(path + ".material"), formatMenuText(player, section.getString("name", "")), lore.toArray(new String[lore.size()])));
    }

    private String formatMenuText(Player player, String value) {
        return format(player, value)
                .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%playtime%", formatPlaytime(player.getStatistic(playtimeStatistic())))
                .replace("%first-join%", formatDate(player.getFirstPlayed()))
                .replace("%last-join%", formatDate(player.getLastPlayed()));
    }

    // Renamed from PLAY_ONE_TICK to PLAY_ONE_MINUTE in 1.13 (it still counts ticks).
    private Statistic playtimeStatistic() {
        try {
            return Statistic.valueOf("PLAY_ONE_MINUTE");
        } catch (IllegalArgumentException ignored) {
            return Statistic.valueOf("PLAY_ONE_TICK");
        }
    }

    private String formatDate(long time) {
        return time <= 0L ? "-" : new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(time));
    }

    private List<Integer> defaultNavigatorDecorationSlots() {
        List<Integer> slots = new ArrayList<Integer>();
        for (int slot = 0; slot < navigatorSize; slot++) {
            int row = slot / 9;
            int column = slot % 9;
            if (row == 0 || row == navigatorSize / 9 - 1 || column == 0 || column == 8) slots.add(slot);
        }
        return slots;
    }

    private boolean isHubItem(ItemStack item, ItemStack expected, String identifier) {
        if (item == null || expected == null || item.getType() != expected.getType() || !item.hasItemMeta() || !expected.hasItemMeta()) return false;
        ItemMeta actualMeta = item.getItemMeta();
        ItemMeta expectedMeta = expected.getItemMeta();
        return actualMeta.hasDisplayName() && expectedMeta.hasDisplayName() && actualMeta.getDisplayName().equals(expectedMeta.getDisplayName());
    }

    private boolean isManagedHubItem(ItemStack item) {
        if (item == null) return false;
        return isHubItem(item, navigatorItem, "navigator") || isHubItem(item, playerHiderVisibleItem, "player-hider")
                || isHubItem(item, playerHiderHiddenItem, "player-hider") || isHubItem(item, profileItem, "profile")
                || isHubItem(item, enderbuttItem, "enderbutt") || isHubItem(item, pvpItem, "pvp");
    }

    private void startPvpCountdown(Player player) {
        Long cooldown = pvpReentryCooldown.get(player.getUniqueId());
        if (cooldown != null && cooldown > System.currentTimeMillis()) {
            player.sendMessage(message("pvp-reentry-cooldown", player));
            return;
        }
        if (!isHoldingPvpItem(player) || !pvpCountdown.add(player.getUniqueId())) return;
        player.sendMessage(message("pvp-countdown", player).replace("%seconds%", "3"));
        Bukkit.getScheduler().runTaskLater(this, () -> player.sendMessage(message("pvp-countdown", player).replace("%seconds%", "2")), 20L);
        Bukkit.getScheduler().runTaskLater(this, () -> player.sendMessage(message("pvp-countdown", player).replace("%seconds%", "1")), 40L);
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (!pvpCountdown.remove(player.getUniqueId()) || !isHoldingPvpItem(player)) return;
            pvpPlayers.add(player.getUniqueId());
            player.getInventory().setHelmet(pvpGear("LEATHER_HELMET", false));
            player.getInventory().setChestplate(pvpGear("LEATHER_CHESTPLATE", false));
            player.getInventory().setLeggings(pvpGear("LEATHER_LEGGINGS", true));
            player.getInventory().setBoots(pvpGear("LEATHER_BOOTS", true));
            player.getInventory().setItem(pvpSlot, pvpGear("IRON_SWORD", false));
            player.sendMessage(message("pvp-enabled", player));
        }, 60L);
    }

    private void disablePvpIfSafe(Player player) {
        if (pvpCombatUntil.containsKey(player.getUniqueId()) && pvpCombatUntil.get(player.getUniqueId()) > System.currentTimeMillis()) {
            player.sendMessage(message("pvp-in-combat", player));
            return;
        }
        clearPvpState(player.getUniqueId());
        pvpReentryCooldown.put(player.getUniqueId(), System.currentTimeMillis() + PVP_REENTRY_MILLIS);
        player.getInventory().setArmorContents(null);
        giveHubItems(player, false);
        player.sendMessage(message("pvp-disabled", player));
    }

    private void clearPvpState(UUID uuid) {
        pvpPlayers.remove(uuid);
        pvpCountdown.remove(uuid);
        pvpExitCountdown.remove(uuid);
        pvpCombatUntil.remove(uuid);
    }

    private void startPvpExit(Player player) {
        if (pvpCombatUntil.containsKey(player.getUniqueId()) && pvpCombatUntil.get(player.getUniqueId()) > System.currentTimeMillis()) {
            player.sendMessage(message("pvp-in-combat", player));
            return;
        }
        if (!pvpExitCountdown.add(player.getUniqueId())) return;
        sendPvpExitCountdown(player, 3);
        Bukkit.getScheduler().runTaskLater(this, () -> sendPvpExitCountdown(player, 2), 20L);
        Bukkit.getScheduler().runTaskLater(this, () -> sendPvpExitCountdown(player, 1), 40L);
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (!pvpExitCountdown.remove(player.getUniqueId()) || player.getInventory().getHeldItemSlot() == pvpSlot) return;
            disablePvpIfSafe(player);
        }, 60L);
    }

    private void sendPvpExitCountdown(Player player, int seconds) {
        if (pvpExitCountdown.contains(player.getUniqueId())) player.sendMessage(message("pvp-exit-countdown", player).replace("%seconds%", String.valueOf(seconds)));
    }

    private boolean isHoldingPvpItem(Player player) {
        return player.isOnline() && player.getInventory().getHeldItemSlot() == pvpSlot && isHubItem(player.getItemInHand(), pvpItem, "pvp");
    }

    private ItemStack pvpGear(String material, boolean protection) {
        ItemStack item = new ItemStack(materialByName(material));
        if (protection) item.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        try {
            ItemMeta meta = item.getItemMeta();
            ItemMeta.class.getMethod("setUnbreakable", boolean.class).invoke(meta, true);
            item.setItemMeta(meta);
        } catch (ReflectiveOperationException ignored) { }
        return item;
    }

    private boolean isMutualPvpDamage(EntityDamageEvent event) {
        if (!(event instanceof EntityDamageByEntityEvent)) return false;
        EntityDamageByEntityEvent damage = (EntityDamageByEntityEvent) event;
        return damage.getDamager() instanceof Player && damage.getEntity() instanceof Player && pvpPlayers.contains(damage.getDamager().getUniqueId()) && pvpPlayers.contains(damage.getEntity().getUniqueId());
    }

    private void hidePlayers(Player player) {
        Set<UUID> hidden = new HashSet<UUID>();
        hiddenPlayers.add(player.getUniqueId());
        hiddenByTitanHub.put(player.getUniqueId(), hidden);
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(player) && player.canSee(other)) {
                hidden.add(other.getUniqueId());
                player.hidePlayer(other);
            }
        }
    }

    private void showPlayers(Player player) {
        hiddenPlayers.remove(player.getUniqueId());
        Set<UUID> hidden = hiddenByTitanHub.remove(player.getUniqueId());
        if (hidden == null) return;
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (hidden.contains(other.getUniqueId())) player.showPlayer(other);
        }
    }

    private void applyHiddenPlayersToJoiner(Player joiner) {
        for (UUID hiderId : hiddenPlayers) {
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

    private void refreshDoubleJump(Player player) {
        if (!doubleJumpEnabled || !isHubWorld(player.getWorld()) || player.getGameMode() == GameMode.CREATIVE || player.getAllowFlight()) return;
        // isOnGround() comes from the client and can be spoofed for infinite double jumps: also check the block below.
        if (!player.isOnGround() || !isStandingOnSolidBlock(player)) return;
        player.setAllowFlight(true);
        doubleJumpFlightEnabled.add(player.getUniqueId());
    }

    private boolean isStandingOnSolidBlock(Player player) {
        Location feet = player.getLocation();
        double[] offsets = {-0.3D, 0.3D};
        for (double x : offsets) {
            for (double z : offsets) {
                if (feet.clone().add(x, -0.1D, z).getBlock().getType().isSolid()) return true;
            }
        }
        return false;
    }

    private void revokeDoubleJumpFlight(Player player) {
        if (doubleJumpFlightEnabled.remove(player.getUniqueId())) player.setAllowFlight(false);
    }

    private void refreshOnlinePlayers() {
        if (!playerHiderEnabled) {
            for (UUID hiderId : new HashSet<UUID>(hiddenPlayers)) {
                Player hider = Bukkit.getPlayer(hiderId);
                if (hider != null) showPlayers(hider);
            }
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            revokeDoubleJumpFlight(player);
            removeHubItems(player);
            giveHubItems(player, false);
            if (scoreboardEnabled) updateScoreboard(player);
            else clearTitanHubScoreboard(player);
            updateTab(player);
            refreshDoubleJump(player);
        }
    }

    private void clearTitanHubScoreboard(Player player) {
        Objective objective = player.getScoreboard().getObjective("titanhub");
        if (objective != null) objective.unregister();
    }

    private void updateTab(Player player) {
        if (!tabEnabled) return;
        try {
            Method method = Player.class.getMethod("setPlayerListHeaderFooter", String.class, String.class);
            method.invoke(player, formatTab(tabHeader, player), formatTab(tabFooter, player));
        } catch (ReflectiveOperationException ignored) { }
    }

    private String formatTab(List<String> lines, Player player) {
        StringBuilder text = new StringBuilder();
        for (String line : lines) {
            if (text.length() > 0) text.append('\n');
            text.append(format(player, line.replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()))));
        }
        return text.toString();
    }

    private void removeHubItems(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int slot = 0; slot < contents.length; slot++) {
            if (isManagedHubItem(contents[slot])) player.getInventory().setItem(slot, null);
        }
    }

    private String uniqueScoreboardEntry(String entry, Set<String> entries, int duplicate) {
        String base = truncateLegacy(entry, 36);
        String candidate = base;
        if (entries.contains(candidate)) candidate = truncateLegacy(base, 36) + ChatColor.RESET + ChatColor.values()[duplicate % ChatColor.values().length];
        while (!entries.add(candidate)) candidate = truncateLegacy(base, 34) + ChatColor.RESET + ChatColor.values()[(++duplicate) % ChatColor.values().length] + ChatColor.RESET;
        return truncateLegacy(candidate, 40);
    }

    private String truncateLegacy(String value, int maxLength) {
        if (value.length() <= maxLength) return value;
        String truncated = value.substring(0, maxLength);
        return truncated.charAt(truncated.length() - 1) == ChatColor.COLOR_CHAR ? truncated.substring(0, truncated.length() - 1) : truncated;
    }

    private Sound sound(String path) {
        String configured = getConfig().getString(path, "").toUpperCase();
        String modern = configured.equals("BAT_TAKEOFF") ? "ENTITY_BAT_TAKEOFF" : configured.equals("ENDERMAN_TELEPORT") ? "ENTITY_ENDERMAN_TELEPORT" : configured;
        try {
            return Sound.valueOf(modern);
        } catch (IllegalArgumentException ignored) {
            try {
                return Sound.valueOf(configured);
            } catch (IllegalArgumentException ignoredAgain) {
                return null;
            }
        }
    }

    private enum MenuType { NAVIGATOR, PROFILE }

    /** Identifies TitanHub menus by holder instead of title, so other plugins' inventories are never intercepted. */
    private static final class MenuHolder implements InventoryHolder {
        private final MenuType type;

        private MenuHolder(MenuType type) {
            this.type = type;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private static final class Destination {
        private final int slot;
        private final ItemStack icon;
        private final boolean server;
        private final String value;

        private Destination(int slot, ItemStack icon, boolean server, String value) {
            this.slot = slot;
            this.icon = icon;
            this.server = server;
            this.value = value;
        }
    }
}
