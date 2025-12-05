package com.ionapi.compat.packet;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Version-agnostic packet utilities for NMS features.
 * Provides a unified API for sending packets across different Minecraft versions.
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * // Action bar
 * IonPacket.actionBar(player, "<red>Health: <white>" + health);
 * 
 * // Title
 * IonPacket.title(player, "<gold>Welcome!", "<gray>to the server", 10, 70, 20);
 * 
 * // Tab list
 * IonPacket.tabList(player, "<gold>My Server", "<gray>Players: " + count);
 * 
 * // Clear title
 * IonPacket.clearTitle(player);
 * }</pre>
 */
public final class IonPacket {

    private static final PacketHandler HANDLER = detectHandler();

    private IonPacket() {}

    /**
     * Sends an action bar message to a player.
     * 
     * @param player the player
     * @param message the message (supports MiniMessage format)
     */
    public static void actionBar(@NotNull Player player, @NotNull String message) {
        HANDLER.sendActionBar(player, message);
    }

    /**
     * Sends an action bar message to a player.
     * 
     * @param player the player
     * @param message the message component
     */
    public static void actionBar(@NotNull Player player, @NotNull Component message) {
        HANDLER.sendActionBar(player, message);
    }

    /**
     * Sends a title to a player.
     * 
     * @param player the player
     * @param title the main title (supports MiniMessage format)
     * @param subtitle the subtitle (supports MiniMessage format)
     */
    public static void title(@NotNull Player player, @NotNull String title, @Nullable String subtitle) {
        title(player, title, subtitle, 10, 70, 20);
    }

    /**
     * Sends a title to a player with custom timing.
     * 
     * @param player the player
     * @param title the main title (supports MiniMessage format)
     * @param subtitle the subtitle (supports MiniMessage format)
     * @param fadeIn fade in time in ticks
     * @param stay stay time in ticks
     * @param fadeOut fade out time in ticks
     */
    public static void title(@NotNull Player player, @NotNull String title, @Nullable String subtitle,
                             int fadeIn, int stay, int fadeOut) {
        HANDLER.sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);
    }

    /**
     * Sends a title to a player with components.
     */
    public static void title(@NotNull Player player, @NotNull Component title, @Nullable Component subtitle,
                             int fadeIn, int stay, int fadeOut) {
        HANDLER.sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);
    }

    /**
     * Clears the title from a player's screen.
     */
    public static void clearTitle(@NotNull Player player) {
        HANDLER.clearTitle(player);
    }

    /**
     * Resets the title (clears and resets timing).
     */
    public static void resetTitle(@NotNull Player player) {
        HANDLER.resetTitle(player);
    }

    /**
     * Sets the tab list header and footer.
     * 
     * @param player the player
     * @param header the header (supports MiniMessage format)
     * @param footer the footer (supports MiniMessage format)
     */
    public static void tabList(@NotNull Player player, @NotNull String header, @NotNull String footer) {
        HANDLER.sendTabList(player, header, footer);
    }

    /**
     * Sets the tab list header and footer with components.
     */
    public static void tabList(@NotNull Player player, @NotNull Component header, @NotNull Component footer) {
        HANDLER.sendTabList(player, header, footer);
    }

    /**
     * Sends a plugin message to a player.
     * 
     * @param plugin the plugin
     * @param player the player
     * @param channel the channel
     * @param data the data
     */
    public static void pluginMessage(@NotNull Plugin plugin, @NotNull Player player, 
                                      @NotNull String channel, byte[] data) {
        HANDLER.sendPluginMessage(plugin, player, channel, data);
    }

    /**
     * Gets the server's Minecraft version.
     */
    @NotNull
    public static String getMinecraftVersion() {
        return HANDLER.getMinecraftVersion();
    }

    /**
     * Gets the server's protocol version.
     */
    public static int getProtocolVersion() {
        return HANDLER.getProtocolVersion();
    }

    /**
     * Checks if the server supports Adventure API natively.
     */
    public static boolean supportsAdventure() {
        return HANDLER.supportsAdventure();
    }

    private static PacketHandler detectHandler() {
        // Try modern Paper API first (1.16.5+)
        try {
            Player.class.getMethod("sendActionBar", Component.class);
            return new ModernPacketHandler();
        } catch (NoSuchMethodException ignored) {}

        // Fall back to legacy handler
        return new LegacyPacketHandler();
    }
}
