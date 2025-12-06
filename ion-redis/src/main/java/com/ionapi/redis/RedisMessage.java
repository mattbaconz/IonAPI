package com.ionapi.redis;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a message received from a Redis pub/sub channel.
 */
public record RedisMessage(@NotNull String channel, @NotNull String data, long timestamp) {

    public RedisMessage(@NotNull String channel, @NotNull String data) {
        this(channel, data, System.currentTimeMillis());
    }

    /**
     * Gets the age of this message in milliseconds.
     */
    public long getAge() {
        return System.currentTimeMillis() - timestamp;
    }
}
