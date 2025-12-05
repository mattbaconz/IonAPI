package com.ionapi.compat;

import com.ionapi.compat.collection.IonList;
import com.ionapi.compat.collection.IonMap;
import com.ionapi.compat.collection.IonSet;
import org.jetbrains.annotations.NotNull;

/**
 * Compatibility utilities for writing modern Java code that runs on older JVMs.
 * Provides polyfills for Java 9+ features when running on Java 8.
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * // Instead of List.of() (Java 9+)
 * List<String> list = IonList.of("a", "b", "c");
 * 
 * // Instead of Map.of() (Java 9+)
 * Map<String, Integer> map = IonMap.of("one", 1, "two", 2);
 * 
 * // Instead of Set.of() (Java 9+)
 * Set<String> set = IonSet.of("a", "b", "c");
 * 
 * // String utilities
 * boolean blank = IonCompat.isBlank("  ");  // true
 * String repeated = IonCompat.repeat("ab", 3);  // "ababab"
 * List<String> lines = IonCompat.lines("a\nb\nc");
 * }</pre>
 */
public final class IonCompat {

    private static final int JAVA_VERSION = getJavaVersion();

    private IonCompat() {}

    /**
     * Gets the current Java major version.
     */
    public static int getJavaVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            return Integer.parseInt(version.substring(2, 3));
        }
        int dot = version.indexOf('.');
        if (dot != -1) {
            return Integer.parseInt(version.substring(0, dot));
        }
        return Integer.parseInt(version);
    }

    /**
     * Checks if running on Java 9 or higher.
     */
    public static boolean isJava9OrHigher() {
        return JAVA_VERSION >= 9;
    }

    /**
     * Checks if running on Java 11 or higher.
     */
    public static boolean isJava11OrHigher() {
        return JAVA_VERSION >= 11;
    }

    /**
     * Checks if running on Java 17 or higher.
     */
    public static boolean isJava17OrHigher() {
        return JAVA_VERSION >= 17;
    }

    /**
     * Checks if running on Java 21 or higher.
     */
    public static boolean isJava21OrHigher() {
        return JAVA_VERSION >= 21;
    }

    // ========== String Utilities (Java 11+ backports) ==========

    /**
     * Checks if a string is blank (empty or only whitespace).
     * Backport of String.isBlank() from Java 11.
     */
    public static boolean isBlank(@NotNull String str) {
        return str.trim().isEmpty();
    }

    /**
     * Strips leading and trailing whitespace.
     * Backport of String.strip() from Java 11.
     */
    @NotNull
    public static String strip(@NotNull String str) {
        return str.trim();
    }

    /**
     * Strips leading whitespace.
     * Backport of String.stripLeading() from Java 11.
     */
    @NotNull
    public static String stripLeading(@NotNull String str) {
        int start = 0;
        while (start < str.length() && Character.isWhitespace(str.charAt(start))) {
            start++;
        }
        return str.substring(start);
    }

    /**
     * Strips trailing whitespace.
     * Backport of String.stripTrailing() from Java 11.
     */
    @NotNull
    public static String stripTrailing(@NotNull String str) {
        int end = str.length();
        while (end > 0 && Character.isWhitespace(str.charAt(end - 1))) {
            end--;
        }
        return str.substring(0, end);
    }

    /**
     * Repeats a string n times.
     * Backport of String.repeat() from Java 11.
     */
    @NotNull
    public static String repeat(@NotNull String str, int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count must be non-negative");
        }
        if (count == 0 || str.isEmpty()) {
            return "";
        }
        if (count == 1) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str.length() * count);
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    /**
     * Splits a string into lines.
     * Backport of String.lines() from Java 11.
     */
    @NotNull
    public static java.util.List<String> lines(@NotNull String str) {
        return java.util.Arrays.asList(str.split("\\R"));
    }

    /**
     * Indents each line by n spaces.
     * Backport of String.indent() from Java 12.
     */
    @NotNull
    public static String indent(@NotNull String str, int n) {
        if (n == 0) return str;
        String prefix = n > 0 ? repeat(" ", n) : "";
        StringBuilder result = new StringBuilder();
        for (String line : lines(str)) {
            if (n > 0) {
                result.append(prefix).append(line);
            } else {
                int toRemove = Math.min(-n, countLeadingSpaces(line));
                result.append(line.substring(toRemove));
            }
            result.append("\n");
        }
        return result.toString();
    }

    private static int countLeadingSpaces(String str) {
        int count = 0;
        for (char c : str.toCharArray()) {
            if (c == ' ') count++;
            else break;
        }
        return count;
    }

    // ========== Optional Utilities ==========

    /**
     * Returns the value if present, otherwise returns other.
     * Backport of Optional.orElse pattern.
     */
    @NotNull
    public static <T> T orElse(T value, @NotNull T other) {
        return value != null ? value : other;
    }

    /**
     * Returns the value if present and non-empty, otherwise returns other.
     */
    @NotNull
    public static String orElseEmpty(String value, @NotNull String other) {
        return value != null && !value.isEmpty() ? value : other;
    }

    // ========== Collection Factory Methods ==========

    /**
     * Creates an immutable list. Alias for IonList.of().
     */
    @NotNull
    @SafeVarargs
    public static <T> java.util.List<T> listOf(T... elements) {
        return IonList.of(elements);
    }

    /**
     * Creates an immutable set. Alias for IonSet.of().
     */
    @NotNull
    @SafeVarargs
    public static <T> java.util.Set<T> setOf(T... elements) {
        return IonSet.of(elements);
    }

    /**
     * Creates an immutable map with one entry.
     */
    @NotNull
    public static <K, V> java.util.Map<K, V> mapOf(K k1, V v1) {
        return IonMap.of(k1, v1);
    }

    /**
     * Creates an immutable map with two entries.
     */
    @NotNull
    public static <K, V> java.util.Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
        return IonMap.of(k1, v1, k2, v2);
    }

    /**
     * Creates an immutable map with three entries.
     */
    @NotNull
    public static <K, V> java.util.Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
        return IonMap.of(k1, v1, k2, v2, k3, v3);
    }
}
