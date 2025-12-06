package com.ionapi.redis;

/**
 * Redis connection statistics.
 */
public record RedisStats(
    boolean connected,
    long messagesPublished,
    long messagesReceived,
    int activeSubscriptions,
    long connectionUptime
) {
    
    public static RedisStats disconnected() {
        return new RedisStats(false, 0, 0, 0, 0);
    }
}
