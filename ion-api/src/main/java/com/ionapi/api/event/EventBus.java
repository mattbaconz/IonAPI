package com.ionapi.api.event;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Event bus for dispatching and listening to events.
 */
public interface EventBus {

    /**
     * Registers an event listener.
     *
     * @param eventClass the event class to listen for
     * @param listener   the listener
     * @param <T>        the event type
     * @return the listener handle for unregistering
     */
    @NotNull
    <T extends IonEvent> ListenerHandle subscribe(@NotNull Class<T> eventClass, @NotNull Consumer<T> listener);

    /**
     * Registers an event listener with a priority.
     *
     * @param eventClass the event class to listen for
     * @param priority   the priority
     * @param listener   the listener
     * @param <T>        the event type
     * @return the listener handle for unregistering
     */
    @NotNull
    <T extends IonEvent> ListenerHandle subscribe(@NotNull Class<T> eventClass, @NotNull EventPriority priority,
            @NotNull Consumer<T> listener);

    /**
     * Fires an event.
     *
     * @param event the event to fire
     * @param <T>   the event type
     * @return the event after all listeners have processed it
     */
    @NotNull
    <T extends IonEvent> T fire(@NotNull T event);

    /**
     * Unsubscribes all listeners for a plugin.
     */
    void unsubscribeAll();

    /**
     * Handle for an event listener subscription.
     */
    interface ListenerHandle {
        /**
         * Unsubscribes this listener.
         */
        void unsubscribe();

        /**
         * Checks if this listener is still subscribed.
         *
         * @return true if subscribed
         */
        boolean isSubscribed();
    }
}
