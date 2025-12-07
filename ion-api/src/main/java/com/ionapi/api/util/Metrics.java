package com.ionapi.api.util;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;

/**
 * Lightweight metrics collection for performance monitoring.
 *
 * <p>Example usage:
 * <pre>{@code
 * // Count events
 * Metrics.increment("player.join");
 * Metrics.increment("commands.executed");
 *
 * // Time operations
 * Metrics.time("database.query", () -> {
 *     // database operation
 *     return result;
 * });
 *
 * // Get stats
 * long joins = Metrics.getCount("player.join");
 * double avgQueryTime = Metrics.getAverageTime("database.query");
 * }</pre>
 *
 * @since 1.2.0
 */
public final class Metrics {

    private static final Map<String, LongAdder> COUNTERS = new ConcurrentHashMap<>();
    private static final Map<String, TimingStats> TIMINGS = new ConcurrentHashMap<>();
    private static final Map<String, AtomicLong> GAUGES = new ConcurrentHashMap<>();

    private Metrics() {}

    /**
     * Increments a counter by 1.
     *
     * @param name the counter name
     */
    public static void increment(@NotNull String name) {
        increment(name, 1);
    }

    /**
     * Increments a counter by a specific amount.
     *
     * @param name the counter name
     * @param amount the amount to add
     */
    public static void increment(@NotNull String name, long amount) {
        COUNTERS.computeIfAbsent(name, k -> new LongAdder()).add(amount);
    }

    /**
     * Gets the current count for a counter.
     *
     * @param name the counter name
     * @return the count
     */
    public static long getCount(@NotNull String name) {
        LongAdder counter = COUNTERS.get(name);
        return counter != null ? counter.sum() : 0;
    }

    /**
     * Sets a gauge value.
     *
     * @param name the gauge name
     * @param value the value
     */
    public static void gauge(@NotNull String name, long value) {
        GAUGES.computeIfAbsent(name, k -> new AtomicLong()).set(value);
    }

    /**
     * Gets a gauge value.
     *
     * @param name the gauge name
     * @return the value
     */
    public static long getGauge(@NotNull String name) {
        AtomicLong gauge = GAUGES.get(name);
        return gauge != null ? gauge.get() : 0;
    }

    /**
     * Times a runnable operation.
     *
     * @param name the timing name
     * @param operation the operation to time
     */
    public static void time(@NotNull String name, @NotNull Runnable operation) {
        long start = System.nanoTime();
        try {
            operation.run();
        } finally {
            recordTiming(name, System.nanoTime() - start);
        }
    }

    /**
     * Times a supplier operation and returns its result.
     *
     * @param name the timing name
     * @param operation the operation to time
     * @param <T> the return type
     * @return the operation result
     */
    public static <T> T time(@NotNull String name, @NotNull Supplier<T> operation) {
        long start = System.nanoTime();
        try {
            return operation.get();
        } finally {
            recordTiming(name, System.nanoTime() - start);
        }
    }

    /**
     * Records a timing manually (in nanoseconds).
     *
     * @param name the timing name
     * @param nanos the duration in nanoseconds
     */
    public static void recordTiming(@NotNull String name, long nanos) {
        TIMINGS.computeIfAbsent(name, k -> new TimingStats()).record(nanos);
    }

    /**
     * Gets the average time for a timing in milliseconds.
     *
     * @param name the timing name
     * @return average time in ms
     */
    public static double getAverageTime(@NotNull String name) {
        TimingStats stats = TIMINGS.get(name);
        return stats != null ? stats.getAverageMs() : 0;
    }

    /**
     * Gets timing statistics.
     *
     * @param name the timing name
     * @return the stats, or null if not found
     */
    public static TimingStats getTimingStats(@NotNull String name) {
        return TIMINGS.get(name);
    }

    /**
     * Gets all counter names.
     *
     * @return set of counter names
     */
    @NotNull
    public static java.util.Set<String> getCounterNames() {
        return COUNTERS.keySet();
    }

    /**
     * Gets all timing names.
     *
     * @return set of timing names
     */
    @NotNull
    public static java.util.Set<String> getTimingNames() {
        return TIMINGS.keySet();
    }

    /**
     * Resets all metrics.
     */
    public static void reset() {
        COUNTERS.clear();
        TIMINGS.clear();
        GAUGES.clear();
    }

    /**
     * Resets a specific counter.
     *
     * @param name the counter name
     */
    public static void resetCounter(@NotNull String name) {
        COUNTERS.remove(name);
    }

    /**
     * Timing statistics.
     */
    public static class TimingStats {
        private final LongAdder count = new LongAdder();
        private final LongAdder totalNanos = new LongAdder();
        private final AtomicLong minNanos = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxNanos = new AtomicLong(0);

        void record(long nanos) {
            count.increment();
            totalNanos.add(nanos);
            minNanos.updateAndGet(current -> Math.min(current, nanos));
            maxNanos.updateAndGet(current -> Math.max(current, nanos));
        }

        public long getCount() { return count.sum(); }
        public double getTotalMs() { return totalNanos.sum() / 1_000_000.0; }
        public double getAverageMs() {
            long c = count.sum();
            return c > 0 ? (totalNanos.sum() / 1_000_000.0) / c : 0;
        }
        public double getMinMs() {
            long min = minNanos.get();
            return min == Long.MAX_VALUE ? 0 : min / 1_000_000.0;
        }
        public double getMaxMs() { return maxNanos.get() / 1_000_000.0; }
    }
}
