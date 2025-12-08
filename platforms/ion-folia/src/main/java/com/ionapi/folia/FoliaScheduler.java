package com.ionapi.folia;

import com.ionapi.api.scheduler.IonScheduler;
import com.ionapi.api.scheduler.IonTask;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class FoliaScheduler implements IonScheduler {

    private final Plugin plugin;

    public FoliaScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    private RegionScheduler getRegionScheduler() {
        return Bukkit.getRegionScheduler();
    }

    private GlobalRegionScheduler getGlobalRegionScheduler() {
        return Bukkit.getGlobalRegionScheduler();
    }

    private AsyncScheduler getAsyncScheduler() {
        return Bukkit.getAsyncScheduler();
    }

    @Override
    public @NotNull IonTask run(@NotNull Runnable task) {
        return new FoliaTask(getGlobalRegionScheduler().run(plugin, t -> task.run()));
    }

    @Override
    public @NotNull IonTask runAsync(@NotNull Runnable task) {
        return new FoliaTask(getAsyncScheduler().runNow(plugin, t -> task.run()));
    }

    @Override
    public @NotNull IonTask runLater(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        return new FoliaTask(getGlobalRegionScheduler().runDelayed(plugin, t -> task.run(), toTicks(delay, unit)));
    }

    @Override
    public @NotNull IonTask runLaterAsync(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        return new FoliaTask(getAsyncScheduler().runDelayed(plugin, t -> task.run(), delay, unit));
    }

    @Override
    public @NotNull IonTask runTimer(@NotNull Runnable task, long delay, long period, @NotNull TimeUnit unit) {
        return new FoliaTask(getGlobalRegionScheduler().runAtFixedRate(plugin, t -> task.run(), toTicks(delay, unit),
                toTicks(period, unit)));
    }

    @Override
    public @NotNull IonTask runTimerAsync(@NotNull Runnable task, long delay, long period, @NotNull TimeUnit unit) {
        return new FoliaTask(getAsyncScheduler().runAtFixedRate(plugin, t -> task.run(), delay, period, unit));
    }

    @Override
    public void cancelAll() {
        getGlobalRegionScheduler().cancelTasks(plugin);
        getAsyncScheduler().cancelTasks(plugin);
        // Region scheduler doesn't have a simple cancelAll for plugin, usually tied to
        // regions/entities
        // But cancelTasks on global should cover global tasks.
    }

    @Override
    public @NotNull IonTask runAt(@NotNull Entity entity, @NotNull Runnable task) {
        return new FoliaTask(entity.getScheduler().run(plugin, t -> task.run(), null));
    }

    @Override
    public @NotNull IonTask runAt(@NotNull Location location, @NotNull Runnable task) {
        return new FoliaTask(getRegionScheduler().run(plugin, location, t -> task.run()));
    }

    @Override
    public @NotNull IonTask runAtLater(@NotNull Entity entity, @NotNull Runnable task, long delay,
            @NotNull TimeUnit unit) {
        return new FoliaTask(entity.getScheduler().runDelayed(plugin, t -> task.run(), null, toTicks(delay, unit)));
    }

    @Override
    public @NotNull IonTask runAtLater(@NotNull Location location, @NotNull Runnable task, long delay,
            @NotNull TimeUnit unit) {
        return new FoliaTask(getRegionScheduler().runDelayed(plugin, location, t -> task.run(), toTicks(delay, unit)));
    }

    @Override
    public @NotNull IonTask runAtTimer(@NotNull Entity entity, @NotNull Runnable task, long delay, long period,
            @NotNull TimeUnit unit) {
        return new FoliaTask(entity.getScheduler().runAtFixedRate(plugin, t -> task.run(), null, toTicks(delay, unit),
                toTicks(period, unit)));
    }

    @Override
    public @NotNull IonTask runAtTimer(@NotNull Location location, @NotNull Runnable task, long delay, long period,
            @NotNull TimeUnit unit) {
        return new FoliaTask(getRegionScheduler().runAtFixedRate(plugin, location, t -> task.run(),
                toTicks(delay, unit), toTicks(period, unit)));
    }

    @Override
    public boolean isMainThread() {
        return Bukkit.isGlobalTickThread(); // Close enough for simple check, or verify specific region thread
    }

    private long toTicks(long time, TimeUnit unit) {
        return unit.toMillis(time) / 50;
    }

    private static class FoliaTask implements IonTask {
        private final ScheduledTask task;

        public FoliaTask(ScheduledTask task) {
            this.task = task;
        }

        @Override
        public int getId() {
            return task.hashCode(); // ScheduledTask doesn't expose ID easily
        }

        @Override
        public boolean isCancelled() {
            return task.isCancelled();
        }

        @Override
        public void cancel() {
            task.cancel();
        }

        @Override
        public boolean isRunning() {
            return task.getExecutionState() == ScheduledTask.ExecutionState.RUNNING;
        }

        @Override
        public @NotNull Object getOwner() {
            return task.getOwningPlugin();
        }
    }
}
