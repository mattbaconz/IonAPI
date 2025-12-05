package com.ionapi.api.scheduler;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * A unified scheduler API that works seamlessly across Paper and Folia
 * platforms.
 * This abstraction handles the differences between single-threaded (Paper) and
 * multi-threaded region-based (Folia) scheduling.
 */
public interface IonScheduler {

    /**
     * Runs a task on the main thread.
     *
     * @apiNote On <b>Folia</b>, this uses the Global Region Scheduler which may
     *          limit
     *          concurrency. Consider using {@link #runAt(Entity, Runnable)}
     *          or {@link #runAt(Location, Runnable)} for better performance
     *          when working with specific entities or locations.
     * @param task the task to run
     * @return the scheduled task
     */
    @NotNull
    IonTask run(@NotNull Runnable task);

    /**
     * Runs a task asynchronously.
     *
     * @param task the task to run
     * @return the scheduled task
     */
    @NotNull
    IonTask runAsync(@NotNull Runnable task);

    /**
     * Runs a task on the main thread after a delay.
     *
     * @apiNote On <b>Folia</b>, this uses the Global Region Scheduler which may
     *          limit
     *          concurrency. Consider using
     *          {@link #runAtLater(Entity, Runnable, long, TimeUnit)}
     *          or {@link #runAtLater(Location, Runnable, long, TimeUnit)}
     *          for better performance when working with specific entities or
     *          locations.
     * @param task  the task to run
     * @param delay the delay before execution
     * @param unit  the time unit of the delay
     * @return the scheduled task
     */
    @NotNull
    IonTask runLater(@NotNull Runnable task, long delay, @NotNull TimeUnit unit);

    /**
     * Runs a task asynchronously after a delay.
     *
     * @param task  the task to run
     * @param delay the delay before execution
     * @param unit  the time unit of the delay
     * @return the scheduled task
     */
    @NotNull
    IonTask runLaterAsync(@NotNull Runnable task, long delay, @NotNull TimeUnit unit);

    /**
     * Runs a task repeatedly on the main thread.
     *
     * @apiNote On <b>Folia</b>, this uses the Global Region Scheduler which may
     *          limit
     *          concurrency. Consider using
     *          {@link #runAtTimer(Entity, Runnable, long, long, TimeUnit)}
     *          or
     *          {@link #runAtTimer(Location, Runnable, long, long, TimeUnit)}
     *          for better performance when working with specific entities or
     *          locations.
     * @param task   the task to run
     * @param delay  the initial delay before first execution
     * @param period the period between executions
     * @param unit   the time unit
     * @return the scheduled task
     */
    @NotNull
    IonTask runTimer(@NotNull Runnable task, long delay, long period, @NotNull TimeUnit unit);

    /**
     * Runs a task repeatedly asynchronously.
     *
     * @param task   the task to run
     * @param delay  the initial delay before first execution
     * @param period the period between executions
     * @param unit   the time unit
     * @return the scheduled task
     */
    @NotNull
    IonTask runTimerAsync(@NotNull Runnable task, long delay, long period, @NotNull TimeUnit unit);

    /**
     * Cancels all tasks scheduled by this scheduler.
     */
    void cancelAll();

    // ========== Context-Aware Scheduling (Folia Optimization) ==========

    /**
     * Runs a task on the thread owning the specified entity.
     * <p>
     * On <b>Folia</b>: Runs on the region thread that owns the entity, enabling
     * optimal performance and thread safety.
     * <p>
     * On <b>Paper</b>: Runs on the main thread (entity parameter is ignored).
     *
     * @apiNote Prefer this method over {@link #run(Runnable)} when working with
     *          entities on Folia servers for better performance and proper thread
     *          safety.
     * @param entity the entity whose region thread should execute the task
     * @param task   the task to run
     * @return the scheduled task
     * @throws IllegalArgumentException if entity is null
     * @since 1.1.0
     */
    @NotNull
    IonTask runAt(@NotNull Entity entity, @NotNull Runnable task);

    /**
     * Runs a task on the thread owning the region at the specified location.
     * <p>
     * On <b>Folia</b>: Runs on the region thread that owns the location, enabling
     * optimal performance and thread safety.
     * <p>
     * On <b>Paper</b>: Runs on the main thread (location parameter is ignored).
     *
     * @apiNote Prefer this method over {@link #run(Runnable)} when working with
     *          specific locations on Folia servers for better performance.
     * @param location the location whose region thread should execute the task
     * @param task     the task to run
     * @return the scheduled task
     * @throws IllegalArgumentException if location is null
     * @since 1.1.0
     */
    @NotNull
    IonTask runAt(@NotNull Location location, @NotNull Runnable task);

    /**
     * Runs a task on the thread owning the specified entity after a delay.
     * <p>
     * On <b>Folia</b>: Runs on the region thread that owns the entity.
     * <p>
     * On <b>Paper</b>: Runs on the main thread (entity parameter is ignored).
     *
     * @param entity the entity whose region thread should execute the task
     * @param task   the task to run
     * @param delay  the delay before execution
     * @param unit   the time unit of the delay
     * @return the scheduled task
     * @throws IllegalArgumentException if entity or unit is null
     * @since 1.1.0
     */
    @NotNull
    IonTask runAtLater(@NotNull Entity entity, @NotNull Runnable task,
            long delay, @NotNull TimeUnit unit);

    /**
     * Runs a task on the thread owning the region at the specified location after a
     * delay.
     * <p>
     * On <b>Folia</b>: Runs on the region thread that owns the location.
     * <p>
     * On <b>Paper</b>: Runs on the main thread (location parameter is ignored).
     *
     * @param location the location whose region thread should execute the task
     * @param task     the task to run
     * @param delay    the delay before execution
     * @param unit     the time unit of the delay
     * @return the scheduled task
     * @throws IllegalArgumentException if location or unit is null
     * @since 1.1.0
     */
    @NotNull
    IonTask runAtLater(@NotNull Location location, @NotNull Runnable task,
            long delay, @NotNull TimeUnit unit);

    /**
     * Runs a task repeatedly on the thread owning the specified entity.
     * <p>
     * On <b>Folia</b>: Runs on the region thread that owns the entity.
     * <p>
     * On <b>Paper</b>: Runs on the main thread (entity parameter is ignored).
     *
     * @param entity the entity whose region thread should execute the task
     * @param task   the task to run
     * @param delay  the initial delay before first execution
     * @param period the period between executions
     * @param unit   the time unit
     * @return the scheduled task
     * @throws IllegalArgumentException if entity or unit is null
     * @since 1.1.0
     */
    @NotNull
    IonTask runAtTimer(@NotNull Entity entity, @NotNull Runnable task,
            long delay, long period, @NotNull TimeUnit unit);

    /**
     * Runs a task repeatedly on the thread owning the region at the specified
     * location.
     * <p>
     * On <b>Folia</b>: Runs on the region thread that owns the location.
     * <p>
     * On <b>Paper</b>: Runs on the main thread (location parameter is ignored).
     *
     * @param location the location whose region thread should execute the task
     * @param task     the task to run
     * @param delay    the initial delay before first execution
     * @param period   the period between executions
     * @param unit     the time unit
     * @return the scheduled task
     * @throws IllegalArgumentException if location or unit is null
     * @since 1.1.0
     */
    @NotNull
    IonTask runAtTimer(@NotNull Location location, @NotNull Runnable task,
            long delay, long period, @NotNull TimeUnit unit);

    /**
     * Checks if the current thread is the main server thread.
     * On Folia, this checks if the current thread is a region thread.
     *
     * @return true if on main/region thread, false otherwise
     */
    boolean isMainThread();
}
