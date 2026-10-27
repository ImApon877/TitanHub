package me.imapon.titanhub.util;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

/** Builds items and resolves materials and sounds by name, across 1.8 and modern versions. */
public final class Items {
    private Items() {
    }

    /** An item whose material is read at {@code materialPath}, with the optional {@code .data} value next to it. */
    public static ItemStack item(FileConfiguration config, String materialPath, String name, List<String> lore) {
        ItemStack stack = new ItemStack(material(config, materialPath));
        int separator = materialPath.lastIndexOf('.');
        if (separator > 0) stack.setDurability((short) config.getInt(materialPath.substring(0, separator) + ".data", 0));
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(Text.color(name));
        List<String> coloredLore = new ArrayList<String>();
        for (String line : lore) coloredLore.add(Text.color(line));
        meta.setLore(coloredLore);
        stack.setItemMeta(meta);
        return stack;
    }

    /** Like {@link #item}, plus the skin set at {@code path + ".head-owner"} for player heads. */
    public static ItemStack configuredItem(FileConfiguration config, String path, String materialKey) {
        ItemStack stack = item(config, path + "." + materialKey, config.getString(path + ".name"), config.getStringList(path + ".lore"));
        applyHeadOwner(stack, config.getString(path + ".head-owner", ""));
        return stack;
    }

    public static ItemStack simpleItem(Material material, String name, String... lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(Text.color(name));
        List<String> coloredLore = new ArrayList<String>();
        for (String line : lore) coloredLore.add(Text.color(line));
        meta.setLore(coloredLore);
        stack.setItemMeta(meta);
        return stack;
    }

    public static Material material(FileConfiguration config, String path) {
        return materialByName(config.getString(path, "STONE"));
    }

    public static Material materialByName(String name) {
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

    public static Sound sound(FileConfiguration config, String path) {
        String configured = config.getString(path, "").toUpperCase();
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

    private static void applyHeadOwner(ItemStack stack, String owner) {
        if (owner == null || owner.isEmpty() || !(stack.getItemMeta() instanceof SkullMeta)) return;
        stack.setDurability((short) 3);
        SkullMeta meta = (SkullMeta) stack.getItemMeta();
        meta.setOwner(owner);
        stack.setItemMeta(meta);
    }
}
