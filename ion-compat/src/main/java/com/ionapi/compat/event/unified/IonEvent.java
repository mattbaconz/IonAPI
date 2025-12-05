package com.ionapi.compat.event.unified;

/**
 * Base interface for all unified Ion events.
 * These events provide a consistent API across all server versions.
 */
public interface IonEvent {
    
    /**
     * Gets the original Bukkit event that triggered this unified event.
     * 
     * @return the original event
     */
    Object getOriginalEvent();
}
