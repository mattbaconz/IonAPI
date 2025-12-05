package com.ionapi.compat.version;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

/**
 * Server version detection and comparison utilities.
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * if (ServerVersion.isAtLeast(1, 20)) {
 *     // Use 1.20+ features
 * }
 * 
 * if (ServerVersion.isFolia()) {
 *     // Use Folia-specific code
 * }
 * 
 * String version = ServerVersion.getMinecraftVersion(); // "1.20.4"
 * }</pre>
 */
public final class ServerVersion {

    private static final int[] VERSION = parseVersion();
    private static final boolean IS_PAPER = detectPaper();
    private static final boolean IS_FOLIA = detectFolia();
    private static final boolean IS_SPIGOT = detectSpigot();

    private ServerVersion() {}

    /**
     * Gets the Minecraft version as a string (e.g., "1.20.4").
     */
    @NotNull
    public static String getMinecraftVersion() {
        return Bukkit.getMinecraftVersion();
    }

    /**
     * Gets the major version number (e.g., 1 for 1.20.4).
     */
    public static int getMajor() {
        return VERSION[0];
    }

    /**
     * Gets the minor version number (e.g., 20 for 1.20.4).
     */
    public static int getMinor() {
        return VERSION[1];
    }

    /**
     * Gets the patch version number (e.g., 4 for 1.20.4).
     */
    public static int getPatch() {
        return VERSION[2];
    }

    /**
     * Checks if the server is running at least the specified version.
     * 
     * @param major major version (e.g., 1)
     * @param minor minor version (e.g., 20)
     * @return true if server version >= specified version
     */
    public static boolean isAtLeast(int major, int minor) {
        return isAtLeast(major, minor, 0);
    }

    /**
     * Checks if the server is running at least the specified version.
     * 
     * @param major major version (e.g., 1)
     * @param minor minor version (e.g., 20)
     * @param patch patch version (e.g., 4)
     * @return true if server version >= specified version
     */
    public static boolean isAtLeast(int major, int minor, int patch) {
        if (VERSION[0] != major) return VERSION[0] > major;
        if (VERSION[1] != minor) return VERSION[1] > minor;
        return VERSION[2] >= patch;
    }

    /**
     * Checks if the server is running exactly the specified version.
     */
    public static boolean isExactly(int major, int minor, int patch) {
        return VERSION[0] == major && VERSION[1] == minor && VERSION[2] == patch;
    }

    /**
     * Checks if the server is running below the specified version.
     */
    public static boolean isBelow(int major, int minor) {
        return !isAtLeast(major, minor);
    }

    /**
     * Checks if the server is running Paper.
     */
    public static boolean isPaper() {
        return IS_PAPER;
    }

    /**
     * Checks if the server is running Folia.
     */
    public static boolean isFolia() {
        return IS_FOLIA;
    }

    /**
     * Checks if the server is running Spigot (but not Paper/Folia).
     */
    public static boolean isSpigot() {
        return IS_SPIGOT && !IS_PAPER && !IS_FOLIA;
    }

    /**
     * Checks if the server supports Adventure API natively.
     * (Paper 1.16.5+)
     */
    public static boolean supportsAdventure() {
        return IS_PAPER && isAtLeast(1, 16, 5);
    }

    /**
     * Checks if the server supports the modern command API.
     * (Paper 1.20.6+ with Brigadier)
     */
    public static boolean supportsModernCommands() {
        return IS_PAPER && isAtLeast(1, 20, 6);
    }

    /**
     * Checks if the server supports component-based item names.
     * (1.20.5+)
     */
    public static boolean supportsComponentItems() {
        return isAtLeast(1, 20, 5);
    }

    /**
     * Gets a version string for display.
     */
    @NotNull
    public static String getVersionString() {
        String platform = IS_FOLIA ? "Folia" : IS_PAPER ? "Paper" : IS_SPIGOT ? "Spigot" : "Unknown";
        return platform + " " + getMinecraftVersion();
    }

    private static int[] parseVersion() {
        String version = Bukkit.getMinecraftVersion();
        String[] parts = version.split("\\.");
        int[] result = new int[3];
        for (int i = 0; i < Math.min(parts.length, 3); i++) {
            try {
                // Handle versions like "1.20.4-pre1"
                String part = parts[i].split("-")[0];
                result[i] = Integer.parseInt(part);
            } catch (NumberFormatException e) {
                result[i] = 0;
            }
        }
        return result;
    }

    private static boolean detectPaper() {
        try {
            Class.forName("io.papermc.paper.configuration.Configuration");
            return true;
        } catch (ClassNotFoundException ignored) {}
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            return true;
        } catch (ClassNotFoundException ignored) {}
        return false;
    }

    private static boolean detectFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException ignored) {}
        return false;
    }

    private static boolean detectSpigot() {
        try {
            Class.forName("org.spigotmc.SpigotConfig");
            return true;
        } catch (ClassNotFoundException ignored) {}
        return false;
    }
}
