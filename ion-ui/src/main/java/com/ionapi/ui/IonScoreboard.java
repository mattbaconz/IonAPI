package com.ionapi.ui;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

/**
 * Fluent API for creating and managing player scoreboards.
 * <p>
 * Example usage:
 * <pre>{@code
 * IonScoreboard board = IonScoreboard.create(player)
 *     .title("<gold><bold>Server Stats")
 *     .line("Players: " + Bukkit.getOnlinePlayers().size())
 *     .line("TPS: 20.0")
 *     .line("")
 *     .line("<gray>example.com")
 *     .show();
 *
 * // Update a specific line
 * board.updateLine(1, "Players: " + newCount);
 *
 * // Or update all lines
 * board.update();
 * }</pre>
 */
public interface IonScoreboard {

    /**
     * Creates a new scoreboard for the specified player.
     *
     * @param player the player to create the scoreboard for
     * @return a new scoreboard builder
     */
    @NotNull
    static IonScoreboardBuilder create(@NotNull Player player) {
        return new IonScoreboardBuilder(player);
    }

    /**
     * Gets the player this scoreboard belongs to.
     *
     * @return the player
     */
    @NotNull
    Player getPlayer();

    /**
     * Sets the title of the scoreboard.
     *
     * @param title the title (supports MiniMessage)
     * @return this scoreboard
     */
    @NotNull
    IonScoreboard title(@NotNull String title);

    /**
     * Sets the title of the scoreboard.
     *
     * @param title the title component
     * @return this scoreboard
     */
    @NotNull
    IonScoreboard title(@NotNull Component title);

    /**
     * Gets the current title of the scoreboard.
     *
     * @return the title
     */
    @NotNull
    Component getTitle();

    /**
     * Adds a line to the scoreboard.
     *
     * @param text the line text (supports MiniMessage)
     * @return this scoreboard
     */
    @NotNull
    IonScoreboard line(@NotNull String text);

    /**
     * Adds a line to the scoreboard.
     *
     * @param component the line component
     * @return this scoreboard
     */
    @NotNull
    IonScoreboard line(@NotNull Component component);

    /**
     * Sets all lines at once.
     *
     * @param lines the lines (supports MiniMessage)
     * @return this scoreboard
     */
    @NotNull
    IonScoreboard lines(@NotNull String... lines);

    /**
     * Sets all lines at once.
     *
     * @param lines the lines (supports MiniMessage)
     * @return this scoreboard
     */
    @NotNull
    IonScoreboard lines(@NotNull List<String> lines);

    /**
     * Sets all lines at once using components.
     *
     * @param lines the line components
     * @return this scoreboard
     */
    @NotNull
    IonScoreboard linesComponents(@NotNull List<Component> lines);

    /**
     * Updates a specific line.
     *
     * @param index the line index (0-based, from top)
     * @param text the new text (supports MiniMessage)
     * @return this scoreboard
     */
    @NotNull
    IonScoreboard updateLine(int index, @NotNull String text);

    /**
     * Updates a specific line.
     *
     * @param index the line index (0-based, from top)
     * @param component the new component
     * @return this scoreboard
     */
    @NotNull
    IonScoreboard updateLine(int index, @NotNull Component component);

    /**
     * Removes a specific line.
     *
     * @param index the line index (0-based, from top)
     * @return this scoreboard
     */
    @NotNull
    IonScoreboard removeLine(int index);

    /**
     * Inserts a line at the specified index.
     *
     * @param index the index to insert at
     * @param text the line text (supports MiniMessage)
     * @return this scoreboard
     */
    @NotNull
    IonScoreboard insertLine(int index, @NotNull String text);

    /**
     * Clears all lines from the scoreboard.
     *
     * @return this scoreboard
     */
    @NotNull
    IonScoreboard clearLines();

    /**
     * Gets the current lines.
     *
     * @return the list of line components
     */
    @NotNull
    List<Component> getLines();

    /**
     * Gets the number of lines.
     *
     * @return the line count
     */
    int getLineCount();

    /**
     * Sets a dynamic line supplier for a specific line.
     * The supplier will be called every time the scoreboard updates.
     *
     * @param index the line index
     * @param supplier the text supplier function
     * @return this scoreboard
     */
    @NotNull
    IonScoreboard dynamicLine(int index, @NotNull Function<Player, String> supplier);

    /**
     * Sets a dynamic title supplier.
     * The supplier will be called every time the scoreboard updates.
     *
     * @param supplier the title supplier function
     * @return this scoreboard
     */
    @NotNull
    IonScoreboard dynamicTitle(@NotNull Function<Player, String> supplier);

    /**
     * Enables automatic updating of the scoreboard.
     *
     * @param intervalTicks the update interval in ticks
     * @return this scoreboard
     */
    @NotNull
    IonScoreboard autoUpdate(long intervalTicks);

    /**
     * Disables automatic updating.
     *
     * @return this scoreboard
     */
    @NotNull
    IonScoreboard stopAutoUpdate();

    /**
     * Checks if auto-update is enabled.
     *
     * @return true if auto-updating
     */
    boolean isAutoUpdating();

    /**
     * Manually updates all dynamic content.
     *
     * @return this scoreboard
     */
    @NotNull
    IonScoreboard update();

    /**
     * Shows the scoreboard to the player.
     *
     * @return this scoreboard
     */
    @NotNull
    IonScoreboard show();

    /**
     * Hides the scoreboard from the player.
     *
     * @return this scoreboard
     */
    @NotNull
    IonScoreboard hide();

    /**
     * Checks if the scoreboard is currently visible.
     *
     * @return true if visible
     */
    boolean isVisible();

    /**
     * Destroys this scoreboard and cleans up resources.
     */
    void destroy();

    /**
     * Gets the underlying Bukkit scoreboard.
     *
     * @return the bukkit scoreboard, or null if not shown
     */
    @Nullable
    org.bukkit.scoreboard.Scoreboard getBukkitScoreboard();
}
