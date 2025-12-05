package com.ionapi.api.event;

/**
 * Priority levels for event handlers.
 */
public enum EventPriority {
    /**
     * Lowest priority, executed first.
     */
    LOWEST,

    /**
     * Low priority.
     */
    LOW,

    /**
     * Normal priority (default).
     */
    NORMAL,

    /**
     * High priority.
     */
    HIGH,

    /**
     * Highest priority, executed last.
     */
    HIGHEST,

    /**
     * Monitor priority - for observing final state only.
     * Should not modify the event.
     */
    MONITOR
}
