package com.ionapi.proxy.impl;

import com.ionapi.proxy.IonMessenger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

/**
 * Redis pub/sub implementation for cross-server messaging.
 * Requires Jedis library.
 */
public class RedisMessenger implements IonMessenger {

    private final Plugin plugin;
    private final JedisPool pool;
    private final Map<String, BiConsumer<Player, String>> stringHandlers = new ConcurrentHashMap<>();
    private final Map<String, BiConsumer<Player, byte[]>> rawHandlers = new ConcurrentHashMap<>();
    private final ExecutorService executor;
    private final IonPubSub pubSub;
    private volatile boolean connected = false;

    public RedisMessenger(@NotNull Plugin plugin, @NotNull String host, int port, @Nullable String password) {
        this.plugin = plugin;
        
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(10);
        config.setMaxIdle(5);
        
        if (password != null && !password.isEmpty()) {
            this.pool = new JedisPool(config, host, port, 2000, password);
        } else {
            this.pool = new JedisPool(config, host, port);
        }
        
        this.executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "IonRedis-" + plugin.getName());
            t.setDaemon(true);
            return t;
        });
        
        this.pubSub = new IonPubSub();
        this.connected = true;
    }

    @Override
    public @NotNull IonMessenger subscribe(@NotNull String channel, @NotNull BiConsumer<@Nullable Player, String> handler) {
        stringHandlers.put(channel, handler);
        executor.submit(() -> {
            try (Jedis jedis = pool.getResource()) {
                jedis.subscribe(pubSub, channel);
            } catch (Exception e) {
                plugin.getLogger().warning("Redis subscribe error: " + e.getMessage());
            }
        });
        return this;
    }

    @Override
    public @NotNull IonMessenger unsubscribe(@NotNull String channel) {
        stringHandlers.remove(channel);
        rawHandlers.remove(channel);
        if (pubSub.isSubscribed()) {
            pubSub.unsubscribe(channel);
        }
        return this;
    }

    @Override
    public void send(@NotNull Player player, @NotNull String channel, @NotNull String message) {
        // Redis doesn't have player-specific routing, broadcast instead
        broadcast(channel, message);
    }

    @Override
    public void sendToServer(@NotNull String server, @NotNull String channel, @NotNull String message) {
        // Prefix message with target server for filtering
        broadcast(channel, "server:" + server + ":" + message);
    }

    @Override
    public void broadcast(@NotNull String channel, @NotNull String message) {
        executor.submit(() -> {
            try (Jedis jedis = pool.getResource()) {
                jedis.publish(channel, message);
            } catch (Exception e) {
                plugin.getLogger().warning("Redis publish error: " + e.getMessage());
            }
        });
    }

    @Override
    public void sendRaw(@NotNull Player player, @NotNull String channel, byte[] data) {
        String encoded = Base64.getEncoder().encodeToString(data);
        broadcast(channel + ":raw", encoded);
    }

    @Override
    public @NotNull IonMessenger subscribeRaw(@NotNull String channel, @NotNull BiConsumer<@Nullable Player, byte[]> handler) {
        rawHandlers.put(channel, handler);
        subscribe(channel + ":raw", (player, message) -> {
            byte[] data = Base64.getDecoder().decode(message);
            handler.accept(player, data);
        });
        return this;
    }

    @Override
    public boolean isConnected() {
        if (!connected) return false;
        try (Jedis jedis = pool.getResource()) {
            return jedis.ping().equals("PONG");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void close() {
        connected = false;
        if (pubSub.isSubscribed()) {
            pubSub.unsubscribe();
        }
        executor.shutdownNow();
        pool.close();
        stringHandlers.clear();
        rawHandlers.clear();
    }

    private class IonPubSub extends JedisPubSub {
        @Override
        public void onMessage(String channel, String message) {
            BiConsumer<Player, String> handler = stringHandlers.get(channel);
            if (handler != null) {
                Bukkit.getScheduler().runTask(plugin, () -> handler.accept(null, message));
            }
        }
    }
}
