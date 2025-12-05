package com.ionapi.compat.packet;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

/**
 * Modern packet handler using Paper's Adventure API (1.16.5+).
 */
final class ModernPacketHandler implements PacketHandler {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    @Override
    public void sendActionBar(@NotNull Player player, @NotNull String message) {
        player.sendActionBar(parse(message));
    }

    @Override
    public void sendActionBar(@NotNull Player player, @NotNull Component message) {
        player.sendActionBar(message);
    }

    @Override
    public void sendTitle(@NotNull Player player, @NotNull String title, @Nullable String subtitle,
                          int fadeIn, int stay, int fadeOut) {
        sendTitle(player, parse(title), subtitle != null ? parse(subtitle) : Component.empty(), fadeIn, stay, fadeOut);
    }

    @Override
    public void sendTitle(@NotNull Player player, @NotNull Component title, @Nullable Component subtitle,
                          int fadeIn, int stay, int fadeOut) {
        Title.Times times = Title.Times.times(
            Duration.ofMillis(fadeIn * 50L),
            Duration.ofMillis(stay * 50L),
            Duration.ofMillis(fadeOut * 50L)
        );
        player.showTitle(Title.title(
            title,
            subtitle != null ? subtitle : Component.empty(),
            times
        ));
    }

    @Override
    public void clearTitle(@NotNull Player player) {
        player.clearTitle();
    }

    @Override
    public void resetTitle(@NotNull Player player) {
        player.resetTitle();
    }

    @Override
    public void sendTabList(@NotNull Player player, @NotNull String header, @NotNull String footer) {
        player.sendPlayerListHeaderAndFooter(parse(header), parse(footer));
    }

    @Override
    public void sendTabList(@NotNull Player player, @NotNull Component header, @NotNull Component footer) {
        player.sendPlayerListHeaderAndFooter(header, footer);
    }

    @Override
    public void sendPluginMessage(@NotNull Plugin plugin, @NotNull Player player,
                                   @NotNull String channel, byte[] data) {
        player.sendPluginMessage(plugin, channel, data);
    }

    @Override
    @NotNull
    public String getMinecraftVersion() {
        return Bukkit.getMinecraftVersion();
    }

    @Override
    public int getProtocolVersion() {
        try {
            // Paper 1.20.4+
            return Bukkit.getUnsafe().getProtocolVersion();
        } catch (Exception e) {
            // Fallback: parse from version string
            return parseProtocolVersion(getMinecraftVersion());
        }
    }

    @Override
    public boolean supportsAdventure() {
        return true;
    }

    private Component parse(String message) {
        return MINI_MESSAGE.deserialize(message);
    }

    private int parseProtocolVersion(String version) {
        // Common version mappings
        return switch (version) {
            case "1.21.4" -> 769;
            case "1.21.3" -> 768;
            case "1.21.2" -> 768;
            case "1.21.1" -> 767;
            case "1.21" -> 767;
            case "1.20.6" -> 766;
            case "1.20.5" -> 766;
            case "1.20.4" -> 765;
            case "1.20.3" -> 765;
            case "1.20.2" -> 764;
            case "1.20.1" -> 763;
            case "1.20" -> 763;
            case "1.19.4" -> 762;
            case "1.19.3" -> 761;
            case "1.19.2" -> 760;
            case "1.19.1" -> 760;
            case "1.19" -> 759;
            case "1.18.2" -> 758;
            case "1.18.1" -> 757;
            case "1.18" -> 757;
            case "1.17.1" -> 756;
            case "1.17" -> 755;
            case "1.16.5" -> 754;
            case "1.16.4" -> 754;
            default -> 0;
        };
    }
}
