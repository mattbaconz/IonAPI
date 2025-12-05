package com.ionapi.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Internal wrapper that bridges IonPlaceholder to PlaceholderAPI's expansion system.
 * This class is automatically created by {@link IonPlaceholderRegistry}.
 */
final class PlaceholderExpansionWrapper extends PlaceholderExpansion {

    private final JavaPlugin plugin;
    private final IonPlaceholder ionPlaceholder;

    PlaceholderExpansionWrapper(@NotNull JavaPlugin plugin, @NotNull IonPlaceholder ionPlaceholder) {
        this.plugin = plugin;
        this.ionPlaceholder = ionPlaceholder;
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return ionPlaceholder.getIdentifier();
    }

    @Override
    @NotNull
    public String getAuthor() {
        return ionPlaceholder.getAuthor();
    }

    @Override
    @NotNull
    public String getVersion() {
        return ionPlaceholder.getVersion();
    }

    @Override
    public boolean persist() {
        // Keep expansion registered even if plugin is disabled
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    @Nullable
    public String onRequest(@Nullable OfflinePlayer player, @NotNull String params) {
        // First check static placeholders
        Map<String, String> staticPlaceholders = ionPlaceholder.getStaticPlaceholders();
        if (staticPlaceholders.containsKey(params)) {
            return staticPlaceholders.get(params);
        }

        // Then delegate to the dynamic handler
        return ionPlaceholder.onRequest(player, params);
    }
}
