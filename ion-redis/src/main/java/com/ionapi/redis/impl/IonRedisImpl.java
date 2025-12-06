package com.ionapi.redis.impl;

import com.ionapi.redis.IonRedis;
import com.ionapi.redis.RedisMessage;
import com.ionapi.redis.RedisStats;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Lettuce-based Redis implementation.
 */
public class IonRedisImpl implements IonRedis {

    private final RedisClient client;
    private final StatefulRedisConnection<String, String> connection;
    private final StatefulRedisPubSubConnection<String, String> pubSubConnection;
    private final RedisAsyncCommands<String, String> async;
    private final RedisPubSubAsyncCommands<String, String> pubSubAsync;
    
    private final Map<String, Consumer<RedisMessage>> subscriptions = new ConcurrentHashMap<>();
    private final AtomicLong messagesPublished = new AtomicLong();
    private final AtomicLong messagesReceived = new AtomicLong();
    private final long connectionTime;

    public IonRedisImpl(String host, int port, String password, int database, int timeout, boolean ssl) {
        this.connectionTime = System.currentTimeMillis();
        
        RedisURI.Builder uriBuilder = RedisURI.builder()
            .withHost(host)
            .withPort(port)
            .withDatabase(database)
            .withTimeout(Duration.ofMillis(timeout));
        
        if (password != null && !password.isEmpty()) {
            uriBuilder.withPassword(password.toCharArray());
        }
        
        if (ssl) {
            uriBuilder.withSsl(true);
        }
        
        RedisURI uri = uriBuilder.build();
        this.client = RedisClient.create(uri);
        this.connection = client.connect();
        this.async = connection.async();
        
        // Setup pub/sub
        this.pubSubConnection = client.connectPubSub();
        this.pubSubAsync = pubSubConnection.async();
        
        pubSubConnection.addListener(new RedisPubSubAdapter<String, String>() {
            @Override
            public void message(String channel, String message) {
                messagesReceived.incrementAndGet();
                Consumer<RedisMessage> handler = subscriptions.get(channel);
                if (handler != null) {
                    handler.accept(new RedisMessage(channel, message));
                }
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<Long> publish(@NotNull String channel, @NotNull String message) {
        messagesPublished.incrementAndGet();
        return pubSubAsync.publish(channel, message).toCompletableFuture();
    }

    @Override
    public void subscribe(@NotNull String channel, @NotNull Consumer<RedisMessage> handler) {
        subscriptions.put(channel, handler);
        pubSubAsync.subscribe(channel);
    }

    @Override
    public void unsubscribe(@NotNull String channel) {
        subscriptions.remove(channel);
        pubSubAsync.unsubscribe(channel);
    }

    @Override
    public @NotNull CompletableFuture<Void> set(@NotNull String key, @NotNull String value) {
        return async.set(key, value).thenApply(result -> (Void) null).toCompletableFuture();
    }

    @Override
    public @NotNull CompletableFuture<Void> set(@NotNull String key, @NotNull String value, long expirySeconds) {
        return async.setex(key, expirySeconds, value).thenApply(result -> (Void) null).toCompletableFuture();
    }

    @Override
    public @NotNull CompletableFuture<String> get(@NotNull String key) {
        return async.get(key).toCompletableFuture();
    }

    @Override
    public @NotNull CompletableFuture<Boolean> delete(@NotNull String key) {
        return async.del(key).thenApply(count -> count > 0).toCompletableFuture();
    }

    @Override
    public @NotNull CompletableFuture<Boolean> exists(@NotNull String key) {
        return async.exists(key).thenApply(count -> count > 0).toCompletableFuture();
    }

    @Override
    public @NotNull CompletableFuture<Boolean> expire(@NotNull String key, long seconds) {
        return async.expire(key, seconds).toCompletableFuture();
    }

    @Override
    public @NotNull CompletableFuture<Long> ttl(@NotNull String key) {
        return async.ttl(key).toCompletableFuture();
    }

    @Override
    public boolean isConnected() {
        return connection.isOpen() && pubSubConnection.isOpen();
    }

    @Override
    public void close() {
        subscriptions.clear();
        pubSubConnection.close();
        connection.close();
        client.shutdown();
    }

    @Override
    public @NotNull RedisStats getStats() {
        if (!isConnected()) {
            return RedisStats.disconnected();
        }
        
        long uptime = System.currentTimeMillis() - connectionTime;
        return new RedisStats(
            true,
            messagesPublished.get(),
            messagesReceived.get(),
            subscriptions.size(),
            uptime
        );
    }
}
