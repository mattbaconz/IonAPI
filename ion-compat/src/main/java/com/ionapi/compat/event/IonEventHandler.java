package com.ionapi.compat.event;

import org.bukkit.event.EventPriority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a unified event handler.
 * Methods annotated with this will receive IonEvents that work across all versions.
 * 
 * <p>Example:</p>
 * <pre>{@code
 * @IonEventHandler
 * public void onPickup(IonEntityPickupItemEvent event) {
 *     // Handle event
 * }
 * 
 * @IonEventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 * public void onPickupHigh(IonEntityPickupItemEvent event) {
 *     // High priority, only if not cancelled
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface IonEventHandler {
    
    /**
     * The priority of this handler.
     */
    EventPriority priority() default EventPriority.NORMAL;
    
    /**
     * Whether to ignore cancelled events.
     */
    boolean ignoreCancelled() default false;
}
