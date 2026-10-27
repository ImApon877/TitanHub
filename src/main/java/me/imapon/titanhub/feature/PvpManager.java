package me.imapon.titanhub.feature;

import me.imapon.titanhub.TitanHub;
import me.imapon.titanhub.util.Items;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Hub PvP: hold the PvP item for 3 seconds to get gear, switch away for 3 seconds to leave. */
public final class PvpManager {
    private static final long COMBAT_MILLIS = 10000L;
    private static final long REENTRY_MILLIS = 3000L;
    private final TitanHub plugin;
    private final Set<UUID> pvpPlayers = new HashSet<UUID>();
    private final Set<UUID> enterCountdown = new HashSet<UUID>();
    private final Set<UUID> exitCountdown = new HashSet<UUID>();
    private final Map<UUID, Long> combatUntil = new HashMap<UUID, Long>();
    private final Map<UUID, Long> reentryCooldown = new HashMap<UUID, Long>();

    public PvpManager(TitanHub plugin) {
        this.plugin = plugin;
    }

    public boolean isInPvp(Player player) {
        return pvpPlayers.contains(player.getUniqueId());
    }

    public void startCountdown(Player player) {
        Long cooldown = reentryCooldown.get(player.getUniqueId());
        if (cooldown != null && cooldown > System.currentTimeMillis()) {
            player.sendMessage(message("pvp-reentry-cooldown", player));
            return;
        }
        if (!isHoldingPvpItem(player) || !enterCountdown.add(player.getUniqueId())) return;
        player.sendMessage(message("pvp-countdown", player).replace("%seconds%", "3"));
        Bukkit.getScheduler().runTaskLater(plugin, () -> player.sendMessage(message("pvp-countdown", player).replace("%seconds%", "2")), 20L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> player.sendMessage(message("pvp-countdown", player).replace("%seconds%", "1")), 40L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!enterCountdown.remove(player.getUniqueId()) || !isHoldingPvpItem(player)) return;
            pvpPlayers.add(player.getUniqueId());
            player.getInventory().setHelmet(gear("LEATHER_HELMET", false));
            player.getInventory().setChestplate(gear("LEATHER_CHESTPLATE", false));
            player.getInventory().setLeggings(gear("LEATHER_LEGGINGS", true));
            player.getInventory().setBoots(gear("LEATHER_BOOTS", true));
            player.getInventory().setItem(plugin.settings().pvpSlot, gear("IRON_SWORD", false));
            player.sendMessage(message("pvp-enabled", player));
        }, 60L);
    }

    public void startExit(Player player) {
        if (isInCombat(player)) {
            player.sendMessage(message("pvp-in-combat", player));
            return;
        }
        if (!exitCountdown.add(player.getUniqueId())) return;
        sendExitCountdown(player, 3);
        Bukkit.getScheduler().runTaskLater(plugin, () -> sendExitCountdown(player, 2), 20L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> sendExitCountdown(player, 1), 40L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!exitCountdown.remove(player.getUniqueId()) || player.getInventory().getHeldItemSlot() == plugin.settings().pvpSlot) return;
            disableIfSafe(player);
        }, 60L);
    }

    /** Selecting the PvP slot starts PvP, leaving it cancels the countdown or starts the exit. */
    public void handleHeldItemChange(Player player, int newSlot) {
        if (!plugin.settings().pvpEnabled || !plugin.settings().isHubWorld(player.getWorld())) return;
        int pvpSlot = plugin.settings().pvpSlot;
        if (isInPvp(player) && newSlot == pvpSlot) {
            exitCountdown.remove(player.getUniqueId());
        } else if (!isInPvp(player) && newSlot == pvpSlot) Bukkit.getScheduler().runTask(plugin, () -> startCountdown(player));
        else if (enterCountdown.remove(player.getUniqueId())) player.sendMessage(message("pvp-cancelled", player));
        else if (isInPvp(player) && newSlot != pvpSlot) startExit(player);
    }

    /** Hit between two players: only allowed when both are in PvP; starts the combat timer. */
    public void handleDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !plugin.settings().isHubWorld(event.getEntity().getWorld())) return;
        Player attacker = (Player) event.getDamager();
        if (!isInPvp(attacker)) {
            event.setCancelled(true);
            return;
        }
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            if (!isInPvp(victim)) {
                event.setCancelled(true);
                return;
            }
            long until = System.currentTimeMillis() + COMBAT_MILLIS;
            combatUntil.put(attacker.getUniqueId(), until);
            combatUntil.put(victim.getUniqueId(), until);
        }
    }

    public boolean isMutualPvpDamage(EntityDamageEvent event) {
        if (!(event instanceof EntityDamageByEntityEvent)) return false;
        EntityDamageByEntityEvent damage = (EntityDamageByEntityEvent) event;
        return damage.getDamager() instanceof Player && damage.getEntity() instanceof Player
                && isInPvp((Player) damage.getDamager()) && isInPvp((Player) damage.getEntity());
    }

    /** Returns true if the dead player was in PvP; their gear is never dropped in the hub. */
    public boolean handleDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        if (!isInPvp(victim)) return false;
        event.getDrops().clear();
        event.setDroppedExp(0);
        if (event.getKeepInventory()) victim.getInventory().setArmorContents(null);
        clearState(victim.getUniqueId());
        reentryCooldown.put(victim.getUniqueId(), System.currentTimeMillis() + REENTRY_MILLIS);
        return true;
    }

    public void forget(UUID uuid) {
        clearState(uuid);
        reentryCooldown.remove(uuid);
    }

    private void disableIfSafe(Player player) {
        if (isInCombat(player)) {
            player.sendMessage(message("pvp-in-combat", player));
            return;
        }
        clearState(player.getUniqueId());
        reentryCooldown.put(player.getUniqueId(), System.currentTimeMillis() + REENTRY_MILLIS);
        player.getInventory().setArmorContents(null);
        plugin.hubItems().give(player, false);
        player.sendMessage(message("pvp-disabled", player));
    }

    private boolean isInCombat(Player player) {
        Long until = combatUntil.get(player.getUniqueId());
        return until != null && until > System.currentTimeMillis();
    }

    private void clearState(UUID uuid) {
        pvpPlayers.remove(uuid);
        enterCountdown.remove(uuid);
        exitCountdown.remove(uuid);
        combatUntil.remove(uuid);
    }

    private void sendExitCountdown(Player player, int seconds) {
        if (exitCountdown.contains(player.getUniqueId())) player.sendMessage(message("pvp-exit-countdown", player).replace("%seconds%", String.valueOf(seconds)));
    }

    private boolean isHoldingPvpItem(Player player) {
        return player.isOnline() && player.getInventory().getHeldItemSlot() == plugin.settings().pvpSlot
                && HubItems.matches(player.getItemInHand(), plugin.settings().pvpItem);
    }

    private ItemStack gear(String material, boolean protection) {
        ItemStack item = new ItemStack(Items.materialByName(material));
        if (protection) {
            Enchantment enchantment = protectionEnchantment();
            if (enchantment != null) item.addUnsafeEnchantment(enchantment, 1);
        }
        try {
            ItemMeta meta = item.getItemMeta();
            ItemMeta.class.getMethod("setUnbreakable", boolean.class).invoke(meta, true);
            item.setItemMeta(meta);
        } catch (ReflectiveOperationException ignored) { }
        return item;
    }

    // PROTECTION_ENVIRONMENTAL was renamed to PROTECTION in 1.20.5.
    @SuppressWarnings("deprecation")
    private static Enchantment protectionEnchantment() {
        Enchantment enchantment = Enchantment.getByName("PROTECTION_ENVIRONMENTAL");
        return enchantment != null ? enchantment : Enchantment.getByName("PROTECTION");
    }

    private String message(String path, Player player) {
        return plugin.messages().get(path, player);
    }
}
