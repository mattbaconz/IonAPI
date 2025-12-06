package com.ionapi.database.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Thread-safe entity cache with TTL support.
 * Reduces database queries for frequently accessed entities.
 */
public class EntityCache<K, V> {

    private final Map<K, CacheEntry<V>> cache = new ConcurrentHashMap<>();
    private final int ttlSeconds;
    private final int maxSize;
    private final ScheduledExecutorService cleaner;

    public EntityCache(int ttlSeconds, int maxSize) {
        this.ttlSeconds = ttlSeconds;
        this.maxSize = maxSize;
        this.cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "IonAPI-CacheCleaner");
            t.setDaemon(true);
            return t;
        });
        
        // Schedule periodic cleanup
        cleaner.scheduleAtFixedRate(this::cleanup, ttlSeconds, ttlSeconds, TimeUnit.SECONDS);
    }

    /**
     * Gets a cached value.
     * @return the cached value, or null if not found or expired
     */
    public @Nullable V get(@NotNull K key) {
        CacheEntry<V> entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired()) {
            cache.remove(key);
            return null;
        }
        return entry.getValue();
    }

    /**
     * Puts a value in the cache.
     */
    public void put(@NotNull K key, @NotNull V value) {
        // Evict oldest if at capacity
        if (cache.size() >= maxSize) {
            evictOldest();
        }
        cache.put(key, new CacheEntry<>(value, ttlSeconds));
    }

    /**
     * Removes a value from the cache.
     */
    public void remove(@NotNull K key) {
        cache.remove(key);
    }

    /**
     * Clears all cached entries.
     */
    public void clear() {
        cache.clear();
    }

    /**
     * Gets the current cache size.
     */
    public int size() {
        return cache.size();
    }

    /**
     * Checks if a key is cached and not expired.
     */
    public boolean contains(@NotNull K key) {
        return get(key) != null;
    }

    /**
     * Shuts down the cache cleaner.
     */
    public void shutdown() {
        cleaner.shutdown();
        cache.clear();
    }

    private void cleanup() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    private void evictOldest() {
        cache.entrySet().stream()
            .min((a, b) -> Long.compare(a.getValue().getCreatedAt(), b.getValue().getCreatedAt()))
            .ifPresent(oldest -> cache.remove(oldest.getKey()));
    }

    /**
     * Cache entry with expiration tracking.
     */
    private static class CacheEntry<V> {
        private final V value;
        private final long expiresAt;
        private final long createdAt;

        CacheEntry(V value, int ttlSeconds) {
            this.value = value;
            this.createdAt = System.currentTimeMillis();
            this.expiresAt = createdAt + (ttlSeconds * 1000L);
        }

        V getValue() {
            return value;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }

        long getCreatedAt() {
            return createdAt;
        }
    }
}
