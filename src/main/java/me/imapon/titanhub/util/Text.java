package me.imapon.titanhub.util;

import org.bukkit.ChatColor;

public final class Text {
    private Text() {
    }

    public static String color(String value) {
        return ChatColor.translateAlternateColorCodes('&', value == null ? "" : value);
    }

    /** Cuts a legacy-colored string without leaving a dangling color character at the end. */
    public static String truncateLegacy(String value, int maxLength) {
        if (value.length() <= maxLength) return value;
        String truncated = value.substring(0, maxLength);
        return truncated.charAt(truncated.length() - 1) == ChatColor.COLOR_CHAR ? truncated.substring(0, truncated.length() - 1) : truncated;
    }
}
