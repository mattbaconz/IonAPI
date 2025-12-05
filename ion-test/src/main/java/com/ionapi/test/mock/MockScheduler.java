package com.ionapi.test.mock;

import com.ionapi.api.scheduler.IonScheduler;
import com.ionapi.api.scheduler.IonTask;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mock implementation of IonScheduler for unit testing.
 * Tasks are not executed automatically - use {@link #runPendingTasks()} to execute them.
 */
public class MockScheduler implements IonScheduler {

    private final List<MockTask> pendingTasks = new CopyOnWriteArrayList<>();
    private final List<MockTask> executedTasks = new CopyOnWriteArrayList<>();
    private final AtomicInteger taskIdCounter = new AtomicInteger(0);
    private long currentTick = 0;
    private boolean isMainThread = true;

    @Override
    @NotNull
    public IonTask run(@NotNull Runnable task) {
        return scheduleTask(task, 0, 0, false, false);
    }

    @Override
    @NotNull
    public IonTask runAsync(@NotNull Runnable task) {
        return scheduleTask(task, 0, 0, true, false);
    }

    @Override
    @NotNull
    public IonTask runLater(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        return scheduleTask(task, toTicks(delay, unit), 0, false, false);
    }

    @Override
    @NotNull
    public IonTask runLaterAsync(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        return scheduleTask(task, toTicks(delay, unit), 0, true, false);
    }

    @Override
    @NotNull
    public IonTask runTimer(@NotNull Runnable task, long delay, long period, @NotNull TimeUnit unit) {
        return scheduleTask(task, toTicks(delay, unit), toTicks(period, unit), false, true);
    }

    @Override
    @NotNull
    public IonTask runTimerAsync(@NotNull Runnable task, long delay, long period, @NotNull TimeUnit unit) {
        return scheduleTask(task, toTicks(delay, unit), toTicks(period, unit), true, true);
    }

    @Override
    @NotNull
    public IonTask runAt(@NotNull Entity entity, @NotNull Runnable task) {
        return run(task);
    }

    @Override
    @NotNull
    public IonTask runAt(@NotNull Location location, @NotNull Runnable task) {
        return run(task);
    }

    @Override
    @NotNull
    public IonTask runAtLater(@NotNull Entity entity, @NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        return runLater(task, delay, unit);
    }

    @Override
    @NotNull
    public IonTask runAtLater(@NotNull Location location, @NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        return runLater(task, delay, unit);
    }

    @Override
    @NotNull
    public IonTask runAtTimer(@NotNull Entity entity, @NotNull Runnable task, long delay, long period, @NotNull TimeUnit unit) {
        return runTimer(task, delay, period, unit);
    }

    @Override
    @NotNull
    public IonTask runAtTimer(@NotNull Location location, @NotNull Runnable task, long delay, long period, @NotNull TimeUnit unit) {
        return runTimer(task, delay, period, unit);
    }

    @Override
    public void cancelAll() {
        pendingTasks.forEach(MockTask::cancel);
        pendingTasks.clear();
    }

    @Override
    public boolean isMainThread() {
        return isMainThread;
    }

    // ========== Test Utilities ==========

    /**
     * Runs all pending tasks that are ready to execute.
     */
    public void runPendingTasks() {
        List<MockTask> toRun = new ArrayList<>();
        for (MockTask task : pendingTasks) {
            if (!task.isCancelled() && task.getScheduledTick() <= currentTick) {
                toRun.add(task);
            }
        }
        
        for (MockTask task : toRun) {
            task.execute();
            executedTasks.add(task);
            
            if (!task.isRepeating()) {
                pendingTasks.remove(task);
            } else {
                task.reschedule(currentTick + task.getPeriod());
            }
        }
    }

    /**
     * Advances the tick counter and runs any tasks that become ready.
     * 
     * @param ticks number of ticks to advance
     */
    public void advanceTicks(long ticks) {
        for (long i = 0; i < ticks; i++) {
            currentTick++;
            runPendingTasks();
        }
    }

    /**
     * Gets all pending tasks.
     */
    @NotNull
    public List<MockTask> getPendingTasks() {
        return new ArrayList<>(pendingTasks);
    }

    /**
     * Gets all executed tasks.
     */
    @NotNull
    public List<MockTask> getExecutedTasks() {
        return new ArrayList<>(executedTasks);
    }

    /**
     * Gets the current tick.
     */
    public long getCurrentTick() {
        return currentTick;
    }

    /**
     * Sets whether the current thread should be considered the main thread.
     */
    public void setMainThread(boolean mainThread) {
        this.isMainThread = mainThread;
    }

    /**
     * Clears all tasks and resets the scheduler.
     */
    public void reset() {
        pendingTasks.clear();
        executedTasks.clear();
        currentTick = 0;
        taskIdCounter.set(0);
    }

    private MockTask scheduleTask(Runnable task, long delay, long period, boolean async, boolean repeating) {
        MockTask mockTask = new MockTask(
            taskIdCounter.incrementAndGet(),
            task,
            currentTick + delay,
            period,
            async,
            repeating
        );
        pendingTasks.add(mockTask);
        return mockTask;
    }

    private long toTicks(long time, TimeUnit unit) {
        return unit.toMillis(time) / 50; // 50ms per tick
    }
}
