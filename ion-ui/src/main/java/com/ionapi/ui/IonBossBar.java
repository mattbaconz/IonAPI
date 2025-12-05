package com.ionapi.ui;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Function;

/**
 * Fluent API for creating and managing boss bars.
 * <p>
 * Example usage:
 * <pre>{@code
 * IonBossBar bar = IonBossBar.create()
 *     .title("<red>Boss Health")
 *     .progress(0.75)
 *     .color(BossBar.Color.RED)
 *     .style(BossBar.Overlay.PROGRESS)
 *     .show(player);
 *
 * // Update progress
 * bar.progress(0.5);
 *
 * // Hide after delay
 * scheduler.runLater(() -> bar.hide(), 5, TimeUnit.SECONDS);
 * }</pre>
 */
public interface IonBossBar {

    /**
     * Creates a new boss bar builder.
     *
     * @return a new boss bar builder
     */
    @NotNull
    static IonBossBarBuilder create() {
        return new IonBossBarBuilder();
    }

    /**
     * Creates a new boss bar builder with a title.
     *
     * @param title the title (supports MiniMessage)
     * @return a new boss bar builder
     */
    @NotNull
    static IonBossBarBuilder create(@NotNull String title) {
        return (IonBossBarBuilder) new IonBossBarBuilder().title(title);
    }

    /**
     * Sets the title of the boss bar.
     *
     * @param title the title (supports MiniMessage)
     * @return this boss bar
     */
    @NotNull
    IonBossBar title(@NotNull String title);

    /**
     * Sets the title of the boss bar.
     *
     * @param title the title component
     * @return this boss bar
     */
    @NotNull
    IonBossBar title(@NotNull Component title);

    /**
     * Gets the current title.
     *
     * @return the title
     */
    @NotNull
    Component getTitle();

    /**
     * Sets the progress of the boss bar.
     *
     * @param progress the progress (0.0 to 1.0)
     * @return this boss bar
     */
    @NotNull
    IonBossBar progress(float progress);

    /**
     * Sets the progress as a percentage.
     *
     * @param percent the progress percentage (0 to 100)
     * @return this boss bar
     */
    @NotNull
    IonBossBar progressPercent(int percent);

    /**
     * Gets the current progress.
     *
     * @return the progress (0.0 to 1.0)
     */
    float getProgress();

    /**
     * Sets the color of the boss bar.
     *
     * @param color the color
     * @return this boss bar
     */
    @NotNull
    IonBossBar color(@NotNull BossBar.Color color);

    /**
     * Gets the current color.
     *
     * @return the color
     */
    @NotNull
    BossBar.Color getColor();

    /**
     * Sets the overlay style of the boss bar.
     *
     * @param overlay the overlay style
     * @return this boss bar
     */
    @NotNull
    IonBossBar overlay(@NotNull BossBar.Overlay overlay);

    /**
     * Gets the current overlay style.
     *
     * @return the overlay
     */
    @NotNull
    BossBar.Overlay getOverlay();

    /**
     * Adds a flag to the boss bar.
     *
     * @param flag the flag to add
     * @return this boss bar
     */
    @NotNull
    IonBossBar addFlag(@NotNull BossBar.Flag flag);

    /**
     * Removes a flag from the boss bar.
     *
     * @param flag the flag to remove
     * @return this boss bar
     */
    @NotNull
    IonBossBar removeFlag(@NotNull BossBar.Flag flag);

    /**
     * Checks if the boss bar has a specific flag.
     *
     * @param flag the flag to check
     * @return true if the flag is present
     */
    boolean hasFlag(@NotNull BossBar.Flag flag);

    /**
     * Shows the boss bar to a player.
     *
     * @param player the player to show to
     * @return this boss bar
     */
    @NotNull
    IonBossBar show(@NotNull Player player);

    /**
     * Shows the boss bar to multiple players.
     *
     * @param players the players to show to
     * @return this boss bar
     */
    @NotNull
    IonBossBar show(@NotNull Player... players);

    /**
     * Shows the boss bar to multiple players.
     *
     * @param players the players to show to
     * @return this boss bar
     */
    @NotNull
    IonBossBar show(@NotNull Collection<Player> players);

    /**
     * Hides the boss bar from a player.
     *
     * @param player the player to hide from
     * @return this boss bar
     */
    @NotNull
    IonBossBar hide(@NotNull Player player);

    /**
     * Hides the boss bar from multiple players.
     *
     * @param players the players to hide from
     * @return this boss bar
     */
    @NotNull
    IonBossBar hide(@NotNull Player... players);

    /**
     * Hides the boss bar from all viewers.
     *
     * @return this boss bar
     */
    @NotNull
    IonBossBar hideAll();

    /**
     * Checks if the boss bar is visible to a player.
     *
     * @param player the player to check
     * @return true if visible
     */
    boolean isVisible(@NotNull Player player);

    /**
     * Gets all players who can see this boss bar.
     *
     * @return the collection of viewers
     */
    @NotNull
    Collection<Player> getViewers();

    /**
     * Gets the number of viewers.
     *
     * @return the viewer count
     */
    int getViewerCount();

    /**
     * Sets a dynamic title supplier.
     * The supplier will be called every time the boss bar updates.
     *
     * @param supplier the title supplier function
     * @return this boss bar
     */
    @NotNull
    IonBossBar dynamicTitle(@NotNull Function<IonBossBar, String> supplier);

    /**
     * Sets a dynamic progress supplier.
     * The supplier will be called every time the boss bar updates.
     *
     * @param supplier the progress supplier function (returns 0.0 to 1.0)
     * @return this boss bar
     */
    @NotNull
    IonBossBar dynamicProgress(@NotNull Function<IonBossBar, Float> supplier);

    /**
     * Enables automatic updating of the boss bar.
     *
     * @param intervalTicks the update interval in ticks
     * @return this boss bar
     */
    @NotNull
    IonBossBar autoUpdate(long intervalTicks);

    /**
     * Disables automatic updating.
     *
     * @return this boss bar
     */
    @NotNull
    IonBossBar stopAutoUpdate();

    /**
     * Checks if auto-update is enabled.
     *
     * @return true if auto-updating
     */
    boolean isAutoUpdating();

    /**
     * Manually updates all dynamic content.
     *
     * @return this boss bar
     */
    @NotNull
    IonBossBar update();

    /**
     * Destroys this boss bar and cleans up resources.
     */
    void destroy();

    /**
     * Gets the underlying Adventure boss bar.
     *
     * @return the adventure boss bar
     */
    @NotNull
    BossBar getAdventureBossBar();
}
