package com.ionapi.compat.collection;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Immutable set factory methods.
 * Backport of Set.of() from Java 9+.
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * Set<String> set = IonSet.of("a", "b", "c");
 * Set<Integer> numbers = IonSet.of(1, 2, 3, 4, 5);
 * Set<String> empty = IonSet.of();
 * }</pre>
 */
public final class IonSet {

    private IonSet() {}

    /**
     * Creates an empty immutable set.
     */
    @NotNull
    public static <T> Set<T> of() {
        return Collections.emptySet();
    }

    /**
     * Creates an immutable set with one element.
     */
    @NotNull
    public static <T> Set<T> of(T e1) {
        return Collections.singleton(e1);
    }

    /**
     * Creates an immutable set with two elements.
     */
    @NotNull
    public static <T> Set<T> of(T e1, T e2) {
        Set<T> set = new LinkedHashSet<>(2);
        set.add(e1);
        set.add(e2);
        return Collections.unmodifiableSet(set);
    }

    /**
     * Creates an immutable set with three elements.
     */
    @NotNull
    public static <T> Set<T> of(T e1, T e2, T e3) {
        Set<T> set = new LinkedHashSet<>(3);
        set.add(e1);
        set.add(e2);
        set.add(e3);
        return Collections.unmodifiableSet(set);
    }

    /**
     * Creates an immutable set with four elements.
     */
    @NotNull
    public static <T> Set<T> of(T e1, T e2, T e3, T e4) {
        Set<T> set = new LinkedHashSet<>(4);
        set.add(e1);
        set.add(e2);
        set.add(e3);
        set.add(e4);
        return Collections.unmodifiableSet(set);
    }

    /**
     * Creates an immutable set with five elements.
     */
    @NotNull
    public static <T> Set<T> of(T e1, T e2, T e3, T e4, T e5) {
        Set<T> set = new LinkedHashSet<>(5);
        set.add(e1);
        set.add(e2);
        set.add(e3);
        set.add(e4);
        set.add(e5);
        return Collections.unmodifiableSet(set);
    }

    /**
     * Creates an immutable set with any number of elements.
     */
    @NotNull
    @SafeVarargs
    public static <T> Set<T> of(T... elements) {
        if (elements.length == 0) return Collections.emptySet();
        if (elements.length == 1) return Collections.singleton(elements[0]);
        Set<T> set = new LinkedHashSet<>(elements.length);
        Collections.addAll(set, elements);
        return Collections.unmodifiableSet(set);
    }

    /**
     * Creates an immutable copy of the given collection.
     * Backport of Set.copyOf() from Java 10.
     */
    @NotNull
    public static <T> Set<T> copyOf(@NotNull Collection<? extends T> collection) {
        if (collection.isEmpty()) return Collections.emptySet();
        return Collections.unmodifiableSet(new LinkedHashSet<>(collection));
    }

    /**
     * Creates a mutable HashSet from elements.
     */
    @NotNull
    @SafeVarargs
    public static <T> HashSet<T> mutableOf(T... elements) {
        HashSet<T> set = new HashSet<>(elements.length);
        Collections.addAll(set, elements);
        return set;
    }

    /**
     * Creates a mutable LinkedHashSet from elements (preserves order).
     */
    @NotNull
    @SafeVarargs
    public static <T> LinkedHashSet<T> orderedOf(T... elements) {
        LinkedHashSet<T> set = new LinkedHashSet<>(elements.length);
        Collections.addAll(set, elements);
        return set;
    }
}
