package com.ionapi.placeholder;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Interface for defining custom placeholders.
 * Implement this interface and register it with {@link IonPlaceholderRegistry}
 * to automatically hook into PlaceholderAPI when available.
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * public class MyPlaceholders implements IonPlaceholder {
 *     @Override
 *     public String getIdentifier() {
 *         return "myplugin";
 *     }
 *     
 *     @Override
 *     public String onRequest(OfflinePlayer player, String params) {
 *         return switch (params) {
 *             case "name" -> player.getName();
 *             case "level" -> String.valueOf(getLevel(player));
 *             default -> null;
 *         };
 *     }
 * }
 * }</pre>
 * 
 * <p>This creates placeholders like %myplugin_name% and %myplugin_level%</p>
 */
public interface IonPlaceholder {

    /**
     * The unique identifier for this placeholder expansion.
     * This becomes the prefix in %identifier_params% format.
     * 
     * @return the identifier (e.g., "myplugin" for %myplugin_xxx%)
     */
    @NotNull
    String getIdentifier();

    /**
     * The author of this placeholder expansion.
     * 
     * @return the author name
     */
    @NotNull
    default String getAuthor() {
        return "Unknown";
    }

    /**
     * The version of this placeholder expansion.
     * 
     * @return the version string
     */
    @NotNull
    default String getVersion() {
        return "1.0.0";
    }

    /**
     * Called when a placeholder with this identifier is requested.
     * 
     * @param player the player requesting the placeholder (may be null for non-player contexts)
     * @param params the parameters after the identifier (e.g., "level" from %myplugin_level%)
     * @return the replacement value, or null if the placeholder is invalid
     */
    @Nullable
    String onRequest(@Nullable OfflinePlayer player, @NotNull String params);

    /**
     * Optional: Define static placeholders as a simple key-value map.
     * These are placeholders that don't depend on the player.
     * 
     * <p>Example:</p>
     * <pre>{@code
     * @Override
     * public Map<String, String> getStaticPlaceholders() {
     *     return Map.of(
     *         "server_name", "My Server",
     *         "version", "1.0.0"
     *     );
     * }
     * }</pre>
     * 
     * @return map of placeholder params to values
     */
    @NotNull
    default Map<String, String> getStaticPlaceholders() {
        return Map.of();
    }
}
