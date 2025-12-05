package com.ionapi.test.mock;

import com.ionapi.api.scheduler.IonTask;
import org.jetbrains.annotations.NotNull;

/**
 * Mock implementation of IonTask for unit testing.
 */
public class MockTask implements IonTask {

    private final int id;
    private final Runnable task;
    private long scheduledTick;
    private final long period;
    private final boolean async;
    private final boolean repeating;
    private boolean cancelled;
    private boolean running;
    private int executionCount;

    public MockTask(int id, Runnable task, long scheduledTick, long period, boolean async, boolean repeating) {
        this.id = id;
        this.task = task;
        this.scheduledTick = scheduledTick;
        this.period = period;
        this.async = async;
        this.repeating = repeating;
        this.cancelled = false;
        this.running = false;
        this.executionCount = 0;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void cancel() {
        this.cancelled = true;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    @NotNull
    public Object getOwner() {
        return "MockPlugin";
    }

    /**
     * Executes the task.
     */
    public void execute() {
        if (cancelled) return;
        running = true;
        try {
            task.run();
            executionCount++;
        } finally {
            running = false;
        }
    }

    /**
     * Reschedules the task for a new tick.
     */
    public void reschedule(long newTick) {
        this.scheduledTick = newTick;
    }

    /**
     * Gets the tick this task is scheduled for.
     */
    public long getScheduledTick() {
        return scheduledTick;
    }

    /**
     * Gets the period for repeating tasks.
     */
    public long getPeriod() {
        return period;
    }

    /**
     * Checks if this is an async task.
     */
    public boolean isAsync() {
        return async;
    }

    /**
     * Checks if this is a repeating task.
     */
    public boolean isRepeating() {
        return repeating;
    }

    /**
     * Gets how many times this task has been executed.
     */
    public int getExecutionCount() {
        return executionCount;
    }
}
