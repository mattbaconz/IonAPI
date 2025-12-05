package com.ionapi.compat.collection;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Immutable map factory methods.
 * Backport of Map.of() from Java 9+.
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * Map<String, Integer> map = IonMap.of("one", 1, "two", 2, "three", 3);
 * Map<String, String> single = IonMap.of("key", "value");
 * Map<String, Object> empty = IonMap.of();
 * }</pre>
 */
public final class IonMap {

    private IonMap() {}

    /**
     * Creates an empty immutable map.
     */
    @NotNull
    public static <K, V> Map<K, V> of() {
        return Collections.emptyMap();
    }

    /**
     * Creates an immutable map with one entry.
     */
    @NotNull
    public static <K, V> Map<K, V> of(K k1, V v1) {
        return Collections.singletonMap(k1, v1);
    }

    /**
     * Creates an immutable map with two entries.
     */
    @NotNull
    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2) {
        Map<K, V> map = new LinkedHashMap<>(2);
        map.put(k1, v1);
        map.put(k2, v2);
        return Collections.unmodifiableMap(map);
    }

    /**
     * Creates an immutable map with three entries.
     */
    @NotNull
    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        Map<K, V> map = new LinkedHashMap<>(3);
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return Collections.unmodifiableMap(map);
    }

    /**
     * Creates an immutable map with four entries.
     */
    @NotNull
    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        Map<K, V> map = new LinkedHashMap<>(4);
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        return Collections.unmodifiableMap(map);
    }

    /**
     * Creates an immutable map with five entries.
     */
    @NotNull
    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        Map<K, V> map = new LinkedHashMap<>(5);
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        map.put(k5, v5);
        return Collections.unmodifiableMap(map);
    }

    /**
     * Creates an immutable map with six entries.
     */
    @NotNull
    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, 
                                       K k4, V v4, K k5, V v5, K k6, V v6) {
        Map<K, V> map = new LinkedHashMap<>(6);
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        map.put(k5, v5);
        map.put(k6, v6);
        return Collections.unmodifiableMap(map);
    }

    /**
     * Creates an immutable map with seven entries.
     */
    @NotNull
    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, 
                                       K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
        Map<K, V> map = new LinkedHashMap<>(7);
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        map.put(k5, v5);
        map.put(k6, v6);
        map.put(k7, v7);
        return Collections.unmodifiableMap(map);
    }

    /**
     * Creates an immutable map with eight entries.
     */
    @NotNull
    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4,
                                       K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8) {
        Map<K, V> map = new LinkedHashMap<>(8);
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        map.put(k5, v5);
        map.put(k6, v6);
        map.put(k7, v7);
        map.put(k8, v8);
        return Collections.unmodifiableMap(map);
    }

    /**
     * Creates an immutable map with nine entries.
     */
    @NotNull
    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4,
                                       K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8,
                                       K k9, V v9) {
        Map<K, V> map = new LinkedHashMap<>(9);
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        map.put(k5, v5);
        map.put(k6, v6);
        map.put(k7, v7);
        map.put(k8, v8);
        map.put(k9, v9);
        return Collections.unmodifiableMap(map);
    }

    /**
     * Creates an immutable map with ten entries.
     */
    @NotNull
    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4,
                                       K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8,
                                       K k9, V v9, K k10, V v10) {
        Map<K, V> map = new LinkedHashMap<>(10);
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        map.put(k5, v5);
        map.put(k6, v6);
        map.put(k7, v7);
        map.put(k8, v8);
        map.put(k9, v9);
        map.put(k10, v10);
        return Collections.unmodifiableMap(map);
    }

    /**
     * Creates an immutable copy of the given map.
     * Backport of Map.copyOf() from Java 10.
     */
    @NotNull
    public static <K, V> Map<K, V> copyOf(@NotNull Map<? extends K, ? extends V> map) {
        if (map.isEmpty()) return Collections.emptyMap();
        return Collections.unmodifiableMap(new LinkedHashMap<>(map));
    }

    /**
     * Creates a mutable HashMap from entries.
     */
    @NotNull
    public static <K, V> HashMap<K, V> mutableOf(K k1, V v1) {
        HashMap<K, V> map = new HashMap<>();
        map.put(k1, v1);
        return map;
    }

    /**
     * Creates a mutable HashMap from entries.
     */
    @NotNull
    public static <K, V> HashMap<K, V> mutableOf(K k1, V v1, K k2, V v2) {
        HashMap<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }

    /**
     * Creates a mutable HashMap from entries.
     */
    @NotNull
    public static <K, V> HashMap<K, V> mutableOf(K k1, V v1, K k2, V v2, K k3, V v3) {
        HashMap<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return map;
    }

    /**
     * Creates a map entry (for use with ofEntries).
     * Backport of Map.entry() from Java 9.
     */
    @NotNull
    public static <K, V> Map.Entry<K, V> entry(K key, V value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

    /**
     * Creates an immutable map from entries.
     * Backport of Map.ofEntries() from Java 9.
     */
    @NotNull
    @SafeVarargs
    public static <K, V> Map<K, V> ofEntries(Map.Entry<? extends K, ? extends V>... entries) {
        if (entries.length == 0) return Collections.emptyMap();
        Map<K, V> map = new LinkedHashMap<>(entries.length);
        for (Map.Entry<? extends K, ? extends V> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return Collections.unmodifiableMap(map);
    }
}
