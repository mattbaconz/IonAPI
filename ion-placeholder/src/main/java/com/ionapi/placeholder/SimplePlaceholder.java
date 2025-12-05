package com.ionapi.placeholder;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A builder-style implementation of IonPlaceholder for quick placeholder creation.
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * IonPlaceholder placeholder = SimplePlaceholder.create("myplugin")
 *     .author("YourName")
 *     .version("1.0.0")
 *     .placeholder("name", player -> player.getName())
 *     .placeholder("level", player -> String.valueOf(getLevel(player)))
 *     .staticPlaceholder("server", "My Server")
 *     .build();
 * 
 * IonPlaceholderRegistry.create(plugin)
 *     .register(placeholder)
 *     .build();
 * }</pre>
 */
public final class SimplePlaceholder implements IonPlaceholder {

    private final String identifier;
    private final String author;
    private final String version;
    private final Map<String, String> staticPlaceholders;
    private final Map<String, Function<OfflinePlayer, String>> playerPlaceholders;
    private final BiFunction<OfflinePlayer, String, String> fallbackHandler;

    private SimplePlaceholder(Builder builder) {
        this.identifier = builder.identifier;
        this.author = builder.author;
        this.version = builder.version;
        this.staticPlaceholders = Map.copyOf(builder.staticPlaceholders);
        this.playerPlaceholders = Map.copyOf(builder.playerPlaceholders);
        this.fallbackHandler = builder.fallbackHandler;
    }

    /**
     * Creates a new SimplePlaceholder builder.
     * 
     * @param identifier the placeholder identifier (e.g., "myplugin" for %myplugin_xxx%)
     * @return a new builder
     */
    @NotNull
    public static Builder create(@NotNull String identifier) {
        return new Builder(identifier);
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return identifier;
    }

    @Override
    @NotNull
    public String getAuthor() {
        return author;
    }

    @Override
    @NotNull
    public String getVersion() {
        return version;
    }

    @Override
    @NotNull
    public Map<String, String> getStaticPlaceholders() {
        return staticPlaceholders;
    }

    @Override
    @Nullable
    public String onRequest(@Nullable OfflinePlayer player, @NotNull String params) {
        // Check player-dependent placeholders
        Function<OfflinePlayer, String> handler = playerPlaceholders.get(params);
        if (handler != null && player != null) {
            return handler.apply(player);
        }

        // Use fallback handler if provided
        if (fallbackHandler != null) {
            return fallbackHandler.apply(player, params);
        }

        return null;
    }

    /**
     * Builder for SimplePlaceholder.
     */
    public static final class Builder {
        private final String identifier;
        private String author = "Unknown";
        private String version = "1.0.0";
        private final Map<String, String> staticPlaceholders = new HashMap<>();
        private final Map<String, Function<OfflinePlayer, String>> playerPlaceholders = new HashMap<>();
        private BiFunction<OfflinePlayer, String, String> fallbackHandler;

        private Builder(@NotNull String identifier) {
            this.identifier = identifier;
        }

        /**
         * Sets the author of this placeholder expansion.
         */
        @NotNull
        public Builder author(@NotNull String author) {
            this.author = author;
            return this;
        }

        /**
         * Sets the version of this placeholder expansion.
         */
        @NotNull
        public Builder version(@NotNull String version) {
            this.version = version;
            return this;
        }

        /**
         * Adds a static placeholder (not player-dependent).
         * 
         * @param param the placeholder param (e.g., "server" for %identifier_server%)
         * @param value the static value
         */
        @NotNull
        public Builder staticPlaceholder(@NotNull String param, @NotNull String value) {
            staticPlaceholders.put(param, value);
            return this;
        }

        /**
         * Adds multiple static placeholders.
         */
        @NotNull
        public Builder staticPlaceholders(@NotNull Map<String, String> placeholders) {
            staticPlaceholders.putAll(placeholders);
            return this;
        }

        /**
         * Adds a player-dependent placeholder.
         * 
         * @param param the placeholder param (e.g., "name" for %identifier_name%)
         * @param handler function that takes a player and returns the value
         */
        @NotNull
        public Builder placeholder(@NotNull String param, @NotNull Function<OfflinePlayer, String> handler) {
            playerPlaceholders.put(param, handler);
            return this;
        }

        /**
         * Sets a fallback handler for unrecognized placeholder params.
         * Useful for dynamic placeholders like %identifier_stat_kills%.
         * 
         * @param handler function that takes player and params, returns value or null
         */
        @NotNull
        public Builder fallback(@NotNull BiFunction<OfflinePlayer, String, String> handler) {
            this.fallbackHandler = handler;
            return this;
        }

        /**
         * Builds the SimplePlaceholder.
         */
        @NotNull
        public SimplePlaceholder build() {
            return new SimplePlaceholder(this);
        }
    }
}
