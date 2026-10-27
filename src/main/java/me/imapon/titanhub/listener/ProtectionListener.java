package me.imapon.titanhub.listener;

import me.imapon.titanhub.TitanHub;
import me.imapon.titanhub.config.HubSettings;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

/** Protection of the whole hub world. Admins bypass the player-caused protections. */
public final class ProtectionListener implements Listener {
    private final TitanHub plugin;

    public ProtectionListener(TitanHub plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (settings().preventDamage && event.getEntity() instanceof Player && inHub(event.getEntity().getWorld()) && !plugin.pvp().isMutualPvpDamage(event)) event.setCancelled(true);
    }

    @EventHandler
    public void onPvpDamage(EntityDamageByEntityEvent event) {
        plugin.pvp().handleDamage(event);
    }

    @EventHandler
    public void onFood(FoodLevelChangeEvent event) {
        if (settings().preventHungerLoss && event.getEntity() instanceof Player && inHub(event.getEntity().getWorld())) event.setCancelled(true);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (settings().preventBlockBreak && isRestricted(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (settings().preventBlockPlace && isRestricted(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (settings().preventInteractions && isRestricted(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (settings().preventInteractions && isRestricted(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (settings().preventInteractions && isRestricted(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onIgnite(BlockIgniteEvent event) {
        if (settings().preventBlockPlace && inHub(event.getBlock().getWorld())) event.setCancelled(true);
    }

    @EventHandler
    public void onBurn(BlockBurnEvent event) {
        if (settings().preventBlockPlace && inHub(event.getBlock().getWorld())) event.setCancelled(true);
    }

    @EventHandler
    public void onSpread(BlockSpreadEvent event) {
        if (settings().preventBlockPlace && inHub(event.getBlock().getWorld())) event.setCancelled(true);
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        if (settings().preventBlockBreak && inHub(event.getLocation().getWorld())) event.setCancelled(true);
    }

    @EventHandler
    public void onWeather(WeatherChangeEvent event) {
        if (settings().preventWeather && inHub(event.getWorld()) && event.toWeatherState()) event.setCancelled(true);
    }

    private HubSettings settings() {
        return plugin.settings();
    }

    private boolean inHub(World world) {
        return settings().isHubWorld(world);
    }

    private boolean isRestricted(Player player) {
        return inHub(player.getWorld()) && !player.hasPermission(TitanHub.ADMIN_PERMISSION);
    }
}
