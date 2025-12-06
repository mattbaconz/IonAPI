package com.ionapi.redis;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Redis client interface for cross-server communication.
 * Provides pub/sub messaging and key-value storage.
 * 
 * Example usage:
 * <pre>
 * IonRedis redis = IonRedisBuilder.create()
 *     .host("localhost")
 *     .port(6379)
 *     .password("secret")
 *     .build();
 * 
 * // Subscribe to channel
 * redis.subscribe("player-join", message -> {
 *     String playerName = message.getData();
 *     Bukkit.broadcastMessage(playerName + " joined!");
 * });
 * 
 * // Publish message
 * redis.publish("player-join", player.getName());
 * </pre>
 */
public interface IonRedis {

    /**
     * Publishes a message to a channel.
     */
    @NotNull CompletableFuture<Long> publish(@NotNull String channel, @NotNull String message);

    /**
     * Subscribes to a channel with a message handler.
     */
    void subscribe(@NotNull String channel, @NotNull Consumer<RedisMessage> handler);

    /**
     * Unsubscribes from a channel.
     */
    void unsubscribe(@NotNull String channel);

    /**
     * Sets a key-value pair.
     */
    @NotNull CompletableFuture<Void> set(@NotNull String key, @NotNull String value);

    /**
     * Sets a key-value pair with expiration in seconds.
     */
    @NotNull CompletableFuture<Void> set(@NotNull String key, @NotNull String value, long expirySeconds);

    /**
     * Gets a value by key.
     */
    @NotNull CompletableFuture<String> get(@NotNull String key);

    /**
     * Deletes a key.
     */
    @NotNull CompletableFuture<Boolean> delete(@NotNull String key);

    /**
     * Checks if a key exists.
     */
    @NotNull CompletableFuture<Boolean> exists(@NotNull String key);

    /**
     * Sets expiration on a key in seconds.
     */
    @NotNull CompletableFuture<Boolean> expire(@NotNull String key, long seconds);

    /**
     * Gets time-to-live for a key in seconds.
     */
    @NotNull CompletableFuture<Long> ttl(@NotNull String key);

    /**
     * Checks if the connection is active.
     */
    boolean isConnected();

    /**
     * Closes the Redis connection.
     */
    void close();

    /**
     * Gets connection statistics.
     */
    @NotNull RedisStats getStats();
}
