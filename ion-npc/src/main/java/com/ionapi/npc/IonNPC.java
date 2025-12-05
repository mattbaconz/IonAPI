package com.ionapi.npc;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Lightweight packet-based NPC.
 * No server ticking, purely visual using packets.
 * 
 * <p>Example usage:
 * <pre>{@code
 * IonNPC npc = IonNPC.builder(plugin)
 *     .location(spawnLocation)
 *     .name("<gold>Shop Keeper")
 *     .skin("Notch")
 *     .lookAtPlayer(true)
 *     .onClick(player -> openShop(player))
 *     .build();
 * 
 * npc.show(player);      // Show to specific player
 * npc.showAll();         // Show to all online players
 * npc.teleport(newLoc);  // Move the NPC
 * npc.destroy();         // Remove completely
 * }</pre>
 */
public interface IonNPC {

    /**
     * Creates a new NPC builder.
     *
     * @param plugin the owning plugin
     * @return a new builder
     */
    @NotNull
    static Builder builder(@NotNull Plugin plugin) {
        return new NPCBuilder(plugin);
    }

    /**
     * Gets the unique ID of this NPC.
     *
     * @return the NPC's UUID
     */
    @NotNull
    UUID getUniqueId();

    /**
     * Gets the entity ID used for packets.
     *
     * @return the entity ID
     */
    int getEntityId();

    /**
     * Gets the NPC's current location.
     *
     * @return the location
     */
    @NotNull
    Location getLocation();

    /**
     * Gets the NPC's display name.
     *
     * @return the display name component
     */
    @Nullable
    Component getName();

    /**
     * Shows this NPC to a specific player.
     *
     * @param player the player to show to
     */
    void show(@NotNull Player player);

    /**
     * Shows this NPC to multiple players.
     *
     * @param players the players to show to
     */
    void show(@NotNull Collection<? extends Player> players);

    /**
     * Shows this NPC to all online players.
     */
    void showAll();

    /**
     * Hides this NPC from a specific player.
     *
     * @param player the player to hide from
     */
    void hide(@NotNull Player player);

    /**
     * Hides this NPC from multiple players.
     *
     * @param players the players to hide from
     */
    void hide(@NotNull Collection<? extends Player> players);

    /**
     * Hides this NPC from all players currently seeing it.
     */
    void hideAll();

    /**
     * Teleports this NPC to a new location.
     *
     * @param location the new location
     */
    void teleport(@NotNull Location location);

    /**
     * Makes the NPC look at a specific location.
     *
     * @param location the location to look at
     */
    void lookAt(@NotNull Location location);

    /**
     * Makes the NPC look at a specific player.
     *
     * @param player the player to look at
     */
    void lookAt(@NotNull Player player);

    /**
     * Plays a swing animation.
     */
    void swingMainHand();

    /**
     * Plays a swing animation with off hand.
     */
    void swingOffHand();

    /**
     * Sets whether this NPC is visible.
     *
     * @param visible true to show, false to hide
     */
    void setVisible(boolean visible);

    /**
     * Checks if a player can see this NPC.
     *
     * @param player the player to check
     * @return true if the player can see this NPC
     */
    boolean isVisibleTo(@NotNull Player player);

    /**
     * Gets all players currently seeing this NPC.
     *
     * @return collection of viewers
     */
    @NotNull
    Collection<Player> getViewers();

    /**
     * Destroys this NPC and removes it from all players.
     */
    void destroy();

    /**
     * Checks if this NPC has been destroyed.
     *
     * @return true if destroyed
     */
    boolean isDestroyed();

    /**
     * Builder for creating NPCs.
     */
    interface Builder {

        /**
         * Sets the NPC's spawn location.
         *
         * @param location the location
         * @return this builder
         */
        @NotNull
        Builder location(@NotNull Location location);

        /**
         * Sets the NPC's display name using MiniMessage format.
         *
         * @param name the display name
         * @return this builder
         */
        @NotNull
        Builder name(@NotNull String name);

        /**
         * Sets the NPC's display name.
         *
         * @param name the display name component
         * @return this builder
         */
        @NotNull
        Builder name(@NotNull Component name);

        /**
         * Sets the NPC's skin from a player name.
         * Fetches skin data from Mojang API.
         *
         * @param playerName the player name to copy skin from
         * @return this builder
         */
        @NotNull
        Builder skin(@NotNull String playerName);

        /**
         * Sets the NPC's skin using raw texture data.
         *
         * @param texture the base64 texture value
         * @param signature the texture signature
         * @return this builder
         */
        @NotNull
        Builder skin(@NotNull String texture, @NotNull String signature);

        /**
         * Sets whether the NPC should automatically look at nearby players.
         *
         * @param lookAt true to enable
         * @return this builder
         */
        @NotNull
        Builder lookAtPlayer(boolean lookAt);

        /**
         * Sets the click handler for this NPC.
         *
         * @param handler the click handler
         * @return this builder
         */
        @NotNull
        Builder onClick(@NotNull Consumer<Player> handler);

        /**
         * Sets whether the NPC should be shown to players automatically
         * when they join or enter range.
         *
         * @param persistent true for persistent visibility
         * @return this builder
         */
        @NotNull
        Builder persistent(boolean persistent);

        /**
         * Sets the view distance for this NPC.
         *
         * @param distance the view distance in blocks
         * @return this builder
         */
        @NotNull
        Builder viewDistance(int distance);

        /**
         * Builds the NPC.
         *
         * @return the created NPC
         */
        @NotNull
        IonNPC build();
    }
}
