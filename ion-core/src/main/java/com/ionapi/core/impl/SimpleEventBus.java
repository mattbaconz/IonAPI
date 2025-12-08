package com.ionapi.core.impl;

import com.ionapi.api.IonPlugin;
import com.ionapi.api.event.EventBus;
import com.ionapi.api.event.EventPriority;
import com.ionapi.api.event.IonEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class SimpleEventBus implements EventBus {

    public SimpleEventBus(IonPlugin plugin) {
    }

    @Override
    public @NotNull <T extends IonEvent> ListenerHandle subscribe(@NotNull Class<T> eventClass,
            @NotNull Consumer<T> listener) {
        return subscribe(eventClass, EventPriority.NORMAL, listener);
    }

    @Override
    public @NotNull <T extends IonEvent> ListenerHandle subscribe(@NotNull Class<T> eventClass,
            @NotNull EventPriority priority, @NotNull Consumer<T> listener) {
        // Here we would register with Bukkit's event system or internal map
        return new ListenerHandle() {
            @Override
            public void unsubscribe() {
                // remove listener
            }

            @Override
            public boolean isSubscribed() {
                return true;
            }
        };
    }

    @Override
    public @NotNull <T extends IonEvent> T fire(@NotNull T event) {
        // Dispatch event
        return event;
    }

    @Override
    public void unsubscribeAll() {
        // clear listeners
    }
}
