package com.ionapi.database.cache;

import com.ionapi.database.annotations.Cacheable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages entity caches for different entity types.
 * Automatically creates caches based on @Cacheable annotations.
 */
public class CacheManager {

    private final Map<Class<?>, EntityCache<Object, Object>> caches = new ConcurrentHashMap<>();

    /**
     * Gets or creates a cache for the given entity class.
     * Returns null if the entity is not cacheable.
     */
    @SuppressWarnings("unchecked")
    public @Nullable <K, V> EntityCache<K, V> getCache(@NotNull Class<V> entityClass) {
        Cacheable annotation = entityClass.getAnnotation(Cacheable.class);
        if (annotation == null) {
            return null;
        }

        return (EntityCache<K, V>) caches.computeIfAbsent(entityClass, 
            clazz -> new EntityCache<>(annotation.ttl(), annotation.maxSize()));
    }

    /**
     * Checks if an entity class is cacheable.
     */
    public boolean isCacheable(@NotNull Class<?> entityClass) {
        return entityClass.isAnnotationPresent(Cacheable.class);
    }

    /**
     * Gets a cached entity by key.
     */
    @SuppressWarnings("unchecked")
    public @Nullable <K, V> V get(@NotNull Class<V> entityClass, @NotNull K key) {
        EntityCache<K, V> cache = getCache(entityClass);
        return cache != null ? cache.get(key) : null;
    }

    /**
     * Caches an entity.
     */
    @SuppressWarnings("unchecked")
    public <K, V> void put(@NotNull Class<V> entityClass, @NotNull K key, @NotNull V value) {
        EntityCache<K, V> cache = getCache(entityClass);
        if (cache != null) {
            cache.put(key, value);
        }
    }

    /**
     * Removes an entity from cache.
     */
    @SuppressWarnings("unchecked")
    public <K, V> void remove(@NotNull Class<V> entityClass, @NotNull K key) {
        EntityCache<K, V> cache = getCache(entityClass);
        if (cache != null) {
            cache.remove(key);
        }
    }

    /**
     * Clears all caches for an entity type.
     */
    public void clearCache(@NotNull Class<?> entityClass) {
        EntityCache<?, ?> cache = caches.get(entityClass);
        if (cache != null) {
            cache.clear();
        }
    }

    /**
     * Clears all caches.
     */
    public void clearAll() {
        caches.values().forEach(EntityCache::clear);
    }

    /**
     * Shuts down all caches.
     */
    public void shutdown() {
        caches.values().forEach(EntityCache::shutdown);
        caches.clear();
    }

    /**
     * Gets cache statistics for an entity type.
     */
    public @Nullable CacheStats getStats(@NotNull Class<?> entityClass) {
        EntityCache<?, ?> cache = caches.get(entityClass);
        if (cache == null) {
            return null;
        }
        
        Cacheable annotation = entityClass.getAnnotation(Cacheable.class);
        return new CacheStats(
            entityClass.getSimpleName(),
            cache.size(),
            annotation != null ? annotation.maxSize() : 0,
            annotation != null ? annotation.ttl() : 0
        );
    }

    /**
     * Cache statistics.
     */
    public record CacheStats(String entityName, int currentSize, int maxSize, int ttlSeconds) {}
}
