package com.ionapi.api.event;

import org.jetbrains.annotations.NotNull;

/**
 * Base interface for all Ion events.
 */
public interface IonEvent {

    /**
     * Gets the name of this event.
     *
     * @return the event name
     */
    @NotNull
    String getEventName();

    /**
     * Checks if this event has been cancelled.
     *
     * @return true if cancelled
     */
    boolean isCancelled();

    /**
     * Sets the cancelled state of this event.
     * Not all events are cancellable.
     *
     * @param cancelled the cancelled state
     */
    void setCancelled(boolean cancelled);

    /**
     * Checks if this event can be cancelled.
     *
     * @return true if cancellable
     */
    boolean isCancellable();
}
