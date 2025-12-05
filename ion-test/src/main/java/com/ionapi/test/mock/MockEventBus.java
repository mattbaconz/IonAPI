package com.ionapi.test.mock;

import com.ionapi.api.event.EventBus;
import com.ionapi.api.event.EventPriority;
import com.ionapi.api.event.IonEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Mock implementation of EventBus for unit testing.
 */
public class MockEventBus implements EventBus {

    private final Map<Class<?>, List<ListenerEntry<?>>> listeners = new HashMap<>();
    private final List<IonEvent> firedEvents = new CopyOnWriteArrayList<>();

    @Override
    @NotNull
    public <T extends IonEvent> ListenerHandle subscribe(@NotNull Class<T> eventClass, @NotNull Consumer<T> listener) {
        return subscribe(eventClass, EventPriority.NORMAL, listener);
    }

    @Override
    @NotNull
    public <T extends IonEvent> ListenerHandle subscribe(@NotNull Class<T> eventClass, 
                                                          @NotNull EventPriority priority,
                                                          @NotNull Consumer<T> listener) {
        ListenerEntry<T> entry = new ListenerEntry<>(eventClass, priority, listener);
        listeners.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>()).add(entry);
        
        // Sort by priority
        listeners.get(eventClass).sort(Comparator.comparingInt(e -> e.priority.ordinal()));
        
        return new MockListenerHandle(entry, listeners.get(eventClass));
    }

    @Override
    @NotNull
    @SuppressWarnings("unchecked")
    public <T extends IonEvent> T fire(@NotNull T event) {
        firedEvents.add(event);
        
        List<ListenerEntry<?>> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (ListenerEntry<?> entry : eventListeners) {
                ((Consumer<T>) entry.listener).accept(event);
            }
        }
        
        return event;
    }

    @Override
    public void unsubscribeAll() {
        listeners.clear();
    }

    // ========== Test Utilities ==========

    /**
     * Gets all events that have been fired.
     */
    @NotNull
    public List<IonEvent> getFiredEvents() {
        return new ArrayList<>(firedEvents);
    }

    /**
     * Gets fired events of a specific type.
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public <T extends IonEvent> List<T> getFiredEvents(@NotNull Class<T> eventClass) {
        List<T> result = new ArrayList<>();
        for (IonEvent event : firedEvents) {
            if (eventClass.isInstance(event)) {
                result.add((T) event);
            }
        }
        return result;
    }

    /**
     * Checks if an event of the given type was fired.
     */
    public boolean wasFired(@NotNull Class<? extends IonEvent> eventClass) {
        return !getFiredEvents(eventClass).isEmpty();
    }

    /**
     * Gets the number of listeners for an event type.
     */
    public int getListenerCount(@NotNull Class<? extends IonEvent> eventClass) {
        List<ListenerEntry<?>> entries = listeners.get(eventClass);
        return entries != null ? entries.size() : 0;
    }

    /**
     * Clears all fired events history.
     */
    public void clearFiredEvents() {
        firedEvents.clear();
    }

    /**
     * Resets the event bus completely.
     */
    public void reset() {
        listeners.clear();
        firedEvents.clear();
    }

    private static class ListenerEntry<T extends IonEvent> {
        final Class<T> eventClass;
        final EventPriority priority;
        final Consumer<T> listener;

        ListenerEntry(Class<T> eventClass, EventPriority priority, Consumer<T> listener) {
            this.eventClass = eventClass;
            this.priority = priority;
            this.listener = listener;
        }
    }

    private static class MockListenerHandle implements ListenerHandle {
        private final ListenerEntry<?> entry;
        private final List<ListenerEntry<?>> list;
        private boolean subscribed = true;

        MockListenerHandle(ListenerEntry<?> entry, List<ListenerEntry<?>> list) {
            this.entry = entry;
            this.list = list;
        }

        @Override
        public void unsubscribe() {
            list.remove(entry);
            subscribed = false;
        }

        @Override
        public boolean isSubscribed() {
            return subscribed && list.contains(entry);
        }
    }
}
