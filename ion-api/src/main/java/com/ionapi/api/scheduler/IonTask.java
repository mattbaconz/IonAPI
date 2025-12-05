package com.ionapi.api.scheduler;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a scheduled task that can be cancelled.
 */
public interface IonTask {

    /**
     * Gets the unique ID of this task.
     *
     * @return the task ID
     */
    int getId();

    /**
     * Checks if this task has been cancelled.
     *
     * @return true if cancelled, false otherwise
     */
    boolean isCancelled();

    /**
     * Cancels this task.
     */
    void cancel();

    /**
     * Checks if this task is currently executing.
     *
     * @return true if executing, false otherwise
     */
    boolean isRunning();

    /**
     * Gets the plugin that owns this task.
     *
     * @return the owning plugin
     */
    @NotNull
    Object getOwner();
}
