package com.ionapi.core.impl;

import com.ionapi.api.IonPlugin;
import com.ionapi.api.event.EventBus;
import com.ionapi.api.event.EventPriority;
import com.ionapi.api.event.IonEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * In-memory event bus implementation with priority-based dispatch.
 */
public class SimpleEventBus implements EventBus {

    private final IonPlugin plugin;
    private final Map<Class<? extends IonEvent>, List<RegisteredListener<?>>> listeners = new ConcurrentHashMap<>();

    public SimpleEventBus(IonPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull <T extends IonEvent> ListenerHandle subscribe(@NotNull Class<T> eventClass,
            @NotNull Consumer<T> listener) {
        return subscribe(eventClass, EventPriority.NORMAL, listener);
    }

    @Override
    public @NotNull <T extends IonEvent> ListenerHandle subscribe(@NotNull Class<T> eventClass,
            @NotNull EventPriority priority, @NotNull Consumer<T> listener) {
        
        RegisteredListener<T> registered = new RegisteredListener<>(eventClass, listener, priority);
        
        listeners.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>()).add(registered);
        
        return new ListenerHandle() {
            private volatile boolean subscribed = true;
            
            @Override
            public void unsubscribe() {
                if (subscribed) {
                    List<RegisteredListener<?>> list = listeners.get(eventClass);
                    if (list != null) {
                        list.remove(registered);
                    }
                    subscribed = false;
                }
            }

            @Override
            public boolean isSubscribed() {
                return subscribed;
            }
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <T extends IonEvent> T fire(@NotNull T event) {
        Class<?> eventClass = event.getClass();
        List<RegisteredListener<?>> toInvoke = new ArrayList<>();
        
        // Collect listeners for the event class and its superclasses
        while (eventClass != null && IonEvent.class.isAssignableFrom(eventClass)) {
            List<RegisteredListener<?>> classListeners = listeners.get(eventClass);
            if (classListeners != null) {
                toInvoke.addAll(classListeners);
            }
            eventClass = eventClass.getSuperclass();
        }
        
        // Sort by priority (LOWEST first, MONITOR last)
        toInvoke.sort(Comparator.comparingInt(r -> r.priority.ordinal()));
        
        // Invoke all listeners
        for (RegisteredListener<?> registered : toInvoke) {
            try {
                ((Consumer<T>) registered.listener).accept(event);
            } catch (Exception e) {
                plugin.getLogger().warning("[IonEventBus] Exception in event handler for " 
                    + event.getClass().getSimpleName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return event;
    }

    @Override
    public void unsubscribeAll() {
        listeners.clear();
    }

    /**
     * Holds a registered event listener with its priority.
     */
    private static class RegisteredListener<T extends IonEvent> {
        final Class<T> eventClass;
        final Consumer<T> listener;
        final EventPriority priority;

        RegisteredListener(Class<T> eventClass, Consumer<T> listener, EventPriority priority) {
            this.eventClass = eventClass;
            this.listener = listener;
            this.priority = priority;
        }
    }
}
