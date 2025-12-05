package com.ionapi.compat.packet;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Legacy packet handler using reflection for older servers (pre-1.16.5).
 * Falls back to Bukkit API where possible.
 */
final class LegacyPacketHandler implements PacketHandler {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    
    // Cached reflection data
    private static Class<?> craftPlayerClass;
    private static Method getHandleMethod;
    private static Class<?> packetClass;
    private static boolean reflectionInitialized = false;
    private static boolean reflectionFailed = false;

    @Override
    public void sendActionBar(@NotNull Player player, @NotNull String message) {
        // Try Spigot API first (1.9+)
        try {
            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                net.md_5.bungee.api.chat.TextComponent.fromLegacyText(translateColors(message)));
            return;
        } catch (Exception ignored) {}

        // Fall back to title packet with empty title
        sendTitle(player, "", message, 0, 40, 0);
    }

    @Override
    public void sendActionBar(@NotNull Player player, @NotNull Component message) {
        sendActionBar(player, LEGACY.serialize(message));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void sendTitle(@NotNull Player player, @NotNull String title, @Nullable String subtitle,
                          int fadeIn, int stay, int fadeOut) {
        // Use Bukkit API (available since 1.8.8)
        player.sendTitle(
            translateColors(title),
            subtitle != null ? translateColors(subtitle) : "",
            fadeIn, stay, fadeOut
        );
    }

    @Override
    public void sendTitle(@NotNull Player player, @NotNull Component title, @Nullable Component subtitle,
                          int fadeIn, int stay, int fadeOut) {
        sendTitle(player, LEGACY.serialize(title), 
            subtitle != null ? LEGACY.serialize(subtitle) : null, 
            fadeIn, stay, fadeOut);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void clearTitle(@NotNull Player player) {
        player.sendTitle("", "", 0, 0, 0);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void resetTitle(@NotNull Player player) {
        player.resetTitle();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void sendTabList(@NotNull Player player, @NotNull String header, @NotNull String footer) {
        player.setPlayerListHeaderFooter(translateColors(header), translateColors(footer));
    }

    @Override
    public void sendTabList(@NotNull Player player, @NotNull Component header, @NotNull Component footer) {
        sendTabList(player, LEGACY.serialize(header), LEGACY.serialize(footer));
    }

    @Override
    public void sendPluginMessage(@NotNull Plugin plugin, @NotNull Player player,
                                   @NotNull String channel, byte[] data) {
        player.sendPluginMessage(plugin, channel, data);
    }

    @Override
    @NotNull
    public String getMinecraftVersion() {
        String version = Bukkit.getBukkitVersion();
        // Format: 1.20.4-R0.1-SNAPSHOT -> 1.20.4
        int dash = version.indexOf('-');
        return dash > 0 ? version.substring(0, dash) : version;
    }

    @Override
    public int getProtocolVersion() {
        String version = getMinecraftVersion();
        return parseProtocolVersion(version);
    }

    @Override
    public boolean supportsAdventure() {
        return false;
    }

    private String translateColors(String message) {
        // Handle MiniMessage-style tags by converting to legacy
        message = message.replace("<red>", "§c")
                        .replace("<green>", "§a")
                        .replace("<blue>", "§9")
                        .replace("<yellow>", "§e")
                        .replace("<gold>", "§6")
                        .replace("<aqua>", "§b")
                        .replace("<white>", "§f")
                        .replace("<gray>", "§7")
                        .replace("<dark_gray>", "§8")
                        .replace("<black>", "§0")
                        .replace("<dark_red>", "§4")
                        .replace("<dark_green>", "§2")
                        .replace("<dark_blue>", "§1")
                        .replace("<dark_aqua>", "§3")
                        .replace("<dark_purple>", "§5")
                        .replace("<light_purple>", "§d")
                        .replace("<bold>", "§l")
                        .replace("<italic>", "§o")
                        .replace("<underlined>", "§n")
                        .replace("<strikethrough>", "§m")
                        .replace("<obfuscated>", "§k")
                        .replace("<reset>", "§r")
                        .replaceAll("</[^>]+>", "§r"); // Close tags reset
        
        // Also handle & color codes
        return message.replace("&", "§");
    }

    private int parseProtocolVersion(String version) {
        return switch (version) {
            case "1.16.5", "1.16.4" -> 754;
            case "1.16.3" -> 753;
            case "1.16.2" -> 751;
            case "1.16.1" -> 736;
            case "1.16" -> 735;
            case "1.15.2" -> 578;
            case "1.15.1" -> 575;
            case "1.15" -> 573;
            case "1.14.4" -> 498;
            case "1.14.3" -> 490;
            case "1.14.2" -> 485;
            case "1.14.1" -> 480;
            case "1.14" -> 477;
            case "1.13.2" -> 404;
            case "1.13.1" -> 401;
            case "1.13" -> 393;
            case "1.12.2" -> 340;
            case "1.12.1" -> 338;
            case "1.12" -> 335;
            case "1.11.2", "1.11.1" -> 316;
            case "1.11" -> 315;
            case "1.10.2", "1.10.1", "1.10" -> 210;
            case "1.9.4" -> 110;
            case "1.9.2" -> 109;
            case "1.9.1" -> 108;
            case "1.9" -> 107;
            case "1.8.9", "1.8.8" -> 47;
            default -> 0;
        };
    }
}
