package com.ionapi.api.util;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe rate limiter using sliding window algorithm.
 * Useful for preventing spam, API abuse, or limiting actions.
 *
 * <p>Example usage:
 * <pre>{@code
 * // Allow 5 messages per 10 seconds
 * RateLimiter chatLimiter = RateLimiter.create("chat", 5, 10, TimeUnit.SECONDS);
 *
 * if (!chatLimiter.tryAcquire(player.getUniqueId())) {
 *     player.sendMessage("You're sending messages too fast!");
 *     return;
 * }
 * // Process message...
 * }</pre>
 *
 * @since 1.2.0
 */
public final class RateLimiter {

    private static final Map<String, RateLimiter> LIMITERS = new ConcurrentHashMap<>();

    private final String name;
    private final int maxRequests;
    private final long windowMs;
    private final Map<UUID, TokenBucket> buckets = new ConcurrentHashMap<>();

    private RateLimiter(String name, int maxRequests, long windowMs) {
        this.name = name;
        this.maxRequests = maxRequests;
        this.windowMs = windowMs;
    }

    /**
     * Creates or gets a named rate limiter.
     *
     * @param name the unique name
     * @param maxRequests maximum requests allowed in the window
     * @param window the time window duration
     * @param unit the time unit
     * @return the rate limiter
     */
    @NotNull
    public static RateLimiter create(@NotNull String name, int maxRequests, long window, @NotNull TimeUnit unit) {
        return LIMITERS.computeIfAbsent(name, n -> new RateLimiter(n, maxRequests, unit.toMillis(window)));
    }

    /**
     * Gets an existing rate limiter by name.
     *
     * @param name the name
     * @return the limiter, or null if not found
     */
    public static RateLimiter get(@NotNull String name) {
        return LIMITERS.get(name);
    }

    /**
     * Attempts to acquire a permit for the given player.
     *
     * @param playerId the player UUID
     * @return true if permit acquired, false if rate limited
     */
    public boolean tryAcquire(@NotNull UUID playerId) {
        TokenBucket bucket = buckets.computeIfAbsent(playerId, id -> new TokenBucket(maxRequests, windowMs));
        return bucket.tryAcquire();
    }

    /**
     * Gets the number of remaining permits for a player.
     *
     * @param playerId the player UUID
     * @return remaining permits
     */
    public int getRemainingPermits(@NotNull UUID playerId) {
        TokenBucket bucket = buckets.get(playerId);
        if (bucket == null) return maxRequests;
        return bucket.getRemaining();
    }

    /**
     * Gets the time until the rate limit resets.
     *
     * @param playerId the player UUID
     * @param unit the time unit
     * @return time until reset, or 0 if not rate limited
     */
    public long getResetTime(@NotNull UUID playerId, @NotNull TimeUnit unit) {
        TokenBucket bucket = buckets.get(playerId);
        if (bucket == null) return 0;
        return bucket.getResetTime(unit);
    }

    /**
     * Resets the rate limit for a player.
     *
     * @param playerId the player UUID
     */
    public void reset(@NotNull UUID playerId) {
        buckets.remove(playerId);
    }

    /**
     * Clears all rate limits.
     */
    public void clearAll() {
        buckets.clear();
    }

    /**
     * Gets the name of this rate limiter.
     *
     * @return the name
     */
    @NotNull
    public String getName() {
        return name;
    }

    private static class TokenBucket {
        private final int maxTokens;
        private final long windowMs;
        private final AtomicInteger tokens;
        private volatile long windowStart;

        TokenBucket(int maxTokens, long windowMs) {
            this.maxTokens = maxTokens;
            this.windowMs = windowMs;
            this.tokens = new AtomicInteger(maxTokens);
            this.windowStart = System.currentTimeMillis();
        }

        synchronized boolean tryAcquire() {
            refill();
            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                return true;
            }
            return false;
        }

        synchronized int getRemaining() {
            refill();
            return tokens.get();
        }

        long getResetTime(TimeUnit unit) {
            long elapsed = System.currentTimeMillis() - windowStart;
            long remaining = windowMs - elapsed;
            return remaining > 0 ? unit.convert(remaining, TimeUnit.MILLISECONDS) : 0;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            if (now - windowStart >= windowMs) {
                tokens.set(maxTokens);
                windowStart = now;
            }
        }
    }
}
