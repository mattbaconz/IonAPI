package com.ionapi.paper;

import com.ionapi.api.scheduler.IonScheduler;
import com.ionapi.api.scheduler.IonTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class PaperScheduler implements IonScheduler {

    private final Plugin plugin;

    public PaperScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull IonTask run(@NotNull Runnable task) {
        return new PaperTask(Bukkit.getScheduler().runTask(plugin, task));
    }

    @Override
    public @NotNull IonTask runAsync(@NotNull Runnable task) {
        return new PaperTask(Bukkit.getScheduler().runTaskAsynchronously(plugin, task));
    }

    @Override
    public @NotNull IonTask runLater(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        return new PaperTask(Bukkit.getScheduler().runTaskLater(plugin, task, toTicks(delay, unit)));
    }

    @Override
    public @NotNull IonTask runLaterAsync(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        return new PaperTask(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, toTicks(delay, unit)));
    }

    @Override
    public @NotNull IonTask runTimer(@NotNull Runnable task, long delay, long period, @NotNull TimeUnit unit) {
        // period is in ticks for Bukkit, but unit might be diff.
        // Javadoc says delay/period.
        // Need to convert unit to ticks.
        return new PaperTask(
                Bukkit.getScheduler().runTaskTimer(plugin, task, toTicks(delay, unit), toTicks(period, unit)));
    }

    @Override
    public @NotNull IonTask runTimerAsync(@NotNull Runnable task, long delay, long period, @NotNull TimeUnit unit) {
        return new PaperTask(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, toTicks(delay, unit),
                toTicks(period, unit)));
    }

    @Override
    public void cancelAll() {
        Bukkit.getScheduler().cancelTasks(plugin);
    }

    // Context-sensitive methods delegate to main thread on Paper
    @Override
    public @NotNull IonTask runAt(@NotNull Entity entity, @NotNull Runnable task) {
        return run(task);
    }

    @Override
    public @NotNull IonTask runAt(@NotNull Location location, @NotNull Runnable task) {
        return run(task);
    }

    @Override
    public @NotNull IonTask runAtLater(@NotNull Entity entity, @NotNull Runnable task, long delay,
            @NotNull TimeUnit unit) {
        return runLater(task, delay, unit);
    }

    @Override
    public @NotNull IonTask runAtLater(@NotNull Location location, @NotNull Runnable task, long delay,
            @NotNull TimeUnit unit) {
        return runLater(task, delay, unit);
    }

    @Override
    public @NotNull IonTask runAtTimer(@NotNull Entity entity, @NotNull Runnable task, long delay, long period,
            @NotNull TimeUnit unit) {
        return runTimer(task, delay, period, unit);
    }

    @Override
    public @NotNull IonTask runAtTimer(@NotNull Location location, @NotNull Runnable task, long delay, long period,
            @NotNull TimeUnit unit) {
        return runTimer(task, delay, period, unit);
    }

    @Override
    public boolean isMainThread() {
        return Bukkit.isPrimaryThread();
    }

    private long toTicks(long time, TimeUnit unit) {
        return unit.toMillis(time) / 50;
    }

    private static class PaperTask implements IonTask {
        private final BukkitTask task;

        public PaperTask(BukkitTask task) {
            this.task = task;
        }

        @Override
        public int getId() {
            return task.getTaskId();
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
            return Bukkit.getScheduler().isCurrentlyRunning(task.getTaskId());
        }

        @Override
        public @NotNull Object getOwner() {
            return task.getOwner();
        }
    }
}
