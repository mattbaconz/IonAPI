package com.ionapi.compat.collection;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Immutable list factory methods.
 * Backport of List.of() from Java 9+.
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * List<String> list = IonList.of("a", "b", "c");
 * List<Integer> numbers = IonList.of(1, 2, 3, 4, 5);
 * List<String> empty = IonList.of();
 * }</pre>
 */
public final class IonList {

    private IonList() {}

    /**
     * Creates an empty immutable list.
     */
    @NotNull
    public static <T> List<T> of() {
        return Collections.emptyList();
    }

    /**
     * Creates an immutable list with one element.
     */
    @NotNull
    public static <T> List<T> of(T e1) {
        return Collections.singletonList(e1);
    }

    /**
     * Creates an immutable list with two elements.
     */
    @NotNull
    public static <T> List<T> of(T e1, T e2) {
        return Collections.unmodifiableList(Arrays.asList(e1, e2));
    }

    /**
     * Creates an immutable list with three elements.
     */
    @NotNull
    public static <T> List<T> of(T e1, T e2, T e3) {
        return Collections.unmodifiableList(Arrays.asList(e1, e2, e3));
    }

    /**
     * Creates an immutable list with four elements.
     */
    @NotNull
    public static <T> List<T> of(T e1, T e2, T e3, T e4) {
        return Collections.unmodifiableList(Arrays.asList(e1, e2, e3, e4));
    }

    /**
     * Creates an immutable list with five elements.
     */
    @NotNull
    public static <T> List<T> of(T e1, T e2, T e3, T e4, T e5) {
        return Collections.unmodifiableList(Arrays.asList(e1, e2, e3, e4, e5));
    }

    /**
     * Creates an immutable list with any number of elements.
     */
    @NotNull
    @SafeVarargs
    public static <T> List<T> of(T... elements) {
        if (elements.length == 0) return Collections.emptyList();
        if (elements.length == 1) return Collections.singletonList(elements[0]);
        return Collections.unmodifiableList(Arrays.asList(elements.clone()));
    }

    /**
     * Creates an immutable copy of the given collection.
     * Backport of List.copyOf() from Java 10.
     */
    @NotNull
    public static <T> List<T> copyOf(@NotNull Collection<? extends T> collection) {
        if (collection.isEmpty()) return Collections.emptyList();
        return Collections.unmodifiableList(new ArrayList<>(collection));
    }

    /**
     * Creates a mutable ArrayList from elements.
     */
    @NotNull
    @SafeVarargs
    public static <T> ArrayList<T> mutableOf(T... elements) {
        return new ArrayList<>(Arrays.asList(elements));
    }

    /**
     * Creates a mutable ArrayList from a collection.
     */
    @NotNull
    public static <T> ArrayList<T> mutableCopyOf(@NotNull Collection<? extends T> collection) {
        return new ArrayList<>(collection);
    }
}
