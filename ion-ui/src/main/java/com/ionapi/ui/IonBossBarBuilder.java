package com.ionapi.ui;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Implementation of IonBossBar for managing boss bars.
 */
public class IonBossBarBuilder implements IonBossBar {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final Plugin PLUGIN = JavaPlugin.getProvidingPlugin(IonBossBarBuilder.class);

    private final BossBar bossBar;
    private final Set<Player> viewers;
    private Function<IonBossBar, String> dynamicTitleSupplier;
    private Function<IonBossBar, Float> dynamicProgressSupplier;
    private boolean autoUpdate;
    private BukkitTask updateTask;
    private long updateInterval;

    /**
     * Creates a new boss bar builder with default settings.
     */
    public IonBossBarBuilder() {
        this.bossBar = BossBar.bossBar(
                Component.text("Boss Bar"),
                1.0f,
                BossBar.Color.PURPLE,
                BossBar.Overlay.PROGRESS
        );
        this.viewers = new HashSet<>();
        this.autoUpdate = false;
        this.updateInterval = 20L;
    }

    @Override
    public @NotNull IonBossBar title(@NotNull String title) {
        bossBar.name(MINI_MESSAGE.deserialize(title));
        return this;
    }

    @Override
    public @NotNull IonBossBar title(@NotNull Component title) {
        bossBar.name(title);
        return this;
    }

    @Override
    public @NotNull Component getTitle() {
        return bossBar.name();
    }

    @Override
    public @NotNull IonBossBar progress(float progress) {
        bossBar.progress(Math.max(0.0f, Math.min(1.0f, progress)));
        return this;
    }

    @Override
    public @NotNull IonBossBar progressPercent(int percent) {
        return progress(percent / 100.0f);
    }

    @Override
    public float getProgress() {
        return bossBar.progress();
    }

    @Override
    public @NotNull IonBossBar color(@NotNull BossBar.Color color) {
        bossBar.color(color);
        return this;
    }

    @Override
    public @NotNull BossBar.Color getColor() {
        return bossBar.color();
    }

    @Override
    public @NotNull IonBossBar overlay(@NotNull BossBar.Overlay overlay) {
        bossBar.overlay(overlay);
        return this;
    }

    @Override
    public @NotNull BossBar.Overlay getOverlay() {
        return bossBar.overlay();
    }

    @Override
    public @NotNull IonBossBar addFlag(@NotNull BossBar.Flag flag) {
        bossBar.addFlag(flag);
        return this;
    }

    @Override
    public @NotNull IonBossBar removeFlag(@NotNull BossBar.Flag flag) {
        bossBar.removeFlag(flag);
        return this;
    }

    @Override
    public boolean hasFlag(@NotNull BossBar.Flag flag) {
        return bossBar.hasFlag(flag);
    }

    @Override
    public @NotNull IonBossBar show(@NotNull Player player) {
        if (!viewers.contains(player)) {
            player.showBossBar(bossBar);
            viewers.add(player);

            if (autoUpdate && updateTask == null) {
                startAutoUpdate();
            }
        }
        return this;
    }

    @Override
    public @NotNull IonBossBar show(@NotNull Player... players) {
        for (Player player : players) {
            show(player);
        }
        return this;
    }

    @Override
    public @NotNull IonBossBar show(@NotNull Collection<Player> players) {
        for (Player player : players) {
            show(player);
        }
        return this;
    }

    @Override
    public @NotNull IonBossBar hide(@NotNull Player player) {
        if (viewers.contains(player)) {
            player.hideBossBar(bossBar);
            viewers.remove(player);

            if (viewers.isEmpty() && updateTask != null) {
                stopAutoUpdate();
            }
        }
        return this;
    }

    @Override
    public @NotNull IonBossBar hide(@NotNull Player... players) {
        for (Player player : players) {
            hide(player);
        }
        return this;
    }

    @Override
    public @NotNull IonBossBar hideAll() {
        for (Player player : new HashSet<>(viewers)) {
            hide(player);
        }
        return this;
    }

    @Override
    public boolean isVisible(@NotNull Player player) {
        return viewers.contains(player);
    }

    @Override
    public @NotNull Collection<Player> getViewers() {
        return new HashSet<>(viewers);
    }

    @Override
    public int getViewerCount() {
        return viewers.size();
    }

    @Override
    public @NotNull IonBossBar dynamicTitle(@NotNull Function<IonBossBar, String> supplier) {
        this.dynamicTitleSupplier = supplier;
        return this;
    }

    @Override
    public @NotNull IonBossBar dynamicProgress(@NotNull Function<IonBossBar, Float> supplier) {
        this.dynamicProgressSupplier = supplier;
        return this;
    }

    @Override
    public @NotNull IonBossBar autoUpdate(long intervalTicks) {
        this.autoUpdate = true;
        this.updateInterval = intervalTicks;
        if (!viewers.isEmpty()) {
            startAutoUpdate();
        }
        return this;
    }

    @Override
    public @NotNull IonBossBar stopAutoUpdate() {
        this.autoUpdate = false;
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        return this;
    }

    @Override
    public boolean isAutoUpdating() {
        return autoUpdate && updateTask != null;
    }

    @Override
    public @NotNull IonBossBar update() {
        // Update dynamic title
        if (dynamicTitleSupplier != null) {
            String titleText = dynamicTitleSupplier.apply(this);
            title(titleText);
        }

        // Update dynamic progress
        if (dynamicProgressSupplier != null) {
            Float progressValue = dynamicProgressSupplier.apply(this);
            if (progressValue != null) {
                progress(progressValue);
            }
        }

        return this;
    }

    @Override
    public void destroy() {
        hideAll();
        stopAutoUpdate();
        dynamicTitleSupplier = null;
        dynamicProgressSupplier = null;
    }

    @Override
    public @NotNull BossBar getAdventureBossBar() {
        return bossBar;
    }

    // Private helper methods

    private void startAutoUpdate() {
        if (autoUpdate && updateTask == null) {
            updateTask = Bukkit.getScheduler().runTaskTimer(PLUGIN, () -> {
                update();

                // Remove offline players
                viewers.removeIf(player -> !player.isOnline());

                // Stop updating if no viewers
                if (viewers.isEmpty()) {
                    stopAutoUpdate();
                }
            }, updateInterval, updateInterval);
        }
    }
}
