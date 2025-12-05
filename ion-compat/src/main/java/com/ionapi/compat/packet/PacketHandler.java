package com.ionapi.compat.packet;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Internal interface for packet handling implementations.
 */
interface PacketHandler {

    void sendActionBar(@NotNull Player player, @NotNull String message);
    
    void sendActionBar(@NotNull Player player, @NotNull Component message);

    void sendTitle(@NotNull Player player, @NotNull String title, @Nullable String subtitle,
                   int fadeIn, int stay, int fadeOut);

    void sendTitle(@NotNull Player player, @NotNull Component title, @Nullable Component subtitle,
                   int fadeIn, int stay, int fadeOut);

    void clearTitle(@NotNull Player player);

    void resetTitle(@NotNull Player player);

    void sendTabList(@NotNull Player player, @NotNull String header, @NotNull String footer);

    void sendTabList(@NotNull Player player, @NotNull Component header, @NotNull Component footer);

    void sendPluginMessage(@NotNull Plugin plugin, @NotNull Player player, 
                           @NotNull String channel, byte[] data);

    @NotNull
    String getMinecraftVersion();

    int getProtocolVersion();

    boolean supportsAdventure();
}
