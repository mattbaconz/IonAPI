package com.ionapi.api.util;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Thread-safe cooldown manager for player actions.
 * Automatically cleans up expired cooldowns.
 *
 * <p>Example usage:
 * <pre>{@code
 * CooldownManager cooldowns = CooldownManager.create("teleport");
 *
 * if (cooldowns.isOnCooldown(player.getUniqueId())) {
 *     long remaining = cooldowns.getRemainingTime(player.getUniqueId(), TimeUnit.SECONDS);
 *     player.sendMessage("Wait " + remaining + "s before teleporting again!");
 *     return;
 * }
 *
 * // Perform teleport...
 * cooldowns.setCooldown(player.getUniqueId(), 30, TimeUnit.SECONDS);
 * }</pre>
 *
 * @since 1.2.0
 */
public final class CooldownManager {

    private static final Map<String, CooldownManager> MANAGERS = new ConcurrentHashMap<>();

    private final String name;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    private CooldownManager(String name) {
        this.name = name;
    }

    /**
     * Creates or gets a named cooldown manager.
     *
     * @param name the unique name for this cooldown manager
     * @return the cooldown manager
     */
    @NotNull
    public static CooldownManager create(@NotNull String name) {
        return MANAGERS.computeIfAbsent(name, CooldownManager::new);
    }

    /**
     * Gets an existing cooldown manager by name.
     *
     * @param name the name
     * @return the manager, or null if not found
     */
    public static CooldownManager get(@NotNull String name) {
        return MANAGERS.get(name);
    }

    /**
     * Sets a cooldown for a player.
     *
     * @param playerId the player UUID
     * @param duration the cooldown duration
     * @param unit the time unit
     */
    public void setCooldown(@NotNull UUID playerId, long duration, @NotNull TimeUnit unit) {
        cooldowns.put(playerId, System.currentTimeMillis() + unit.toMillis(duration));
    }

    /**
     * Checks if a player is on cooldown.
     *
     * @param playerId the player UUID
     * @return true if on cooldown
     */
    public boolean isOnCooldown(@NotNull UUID playerId) {
        Long expiry = cooldowns.get(playerId);
        if (expiry == null) return false;
        if (System.currentTimeMillis() >= expiry) {
            cooldowns.remove(playerId);
            return false;
        }
        return true;
    }

    /**
     * Gets the remaining cooldown time.
     *
     * @param playerId the player UUID
     * @param unit the time unit for the result
     * @return remaining time, or 0 if not on cooldown
     */
    public long getRemainingTime(@NotNull UUID playerId, @NotNull TimeUnit unit) {
        Long expiry = cooldowns.get(playerId);
        if (expiry == null) return 0;
        long remaining = expiry - System.currentTimeMillis();
        if (remaining <= 0) {
            cooldowns.remove(playerId);
            return 0;
        }
        return unit.convert(remaining, TimeUnit.MILLISECONDS);
    }

    /**
     * Removes a player's cooldown.
     *
     * @param playerId the player UUID
     */
    public void removeCooldown(@NotNull UUID playerId) {
        cooldowns.remove(playerId);
    }

    /**
     * Clears all cooldowns.
     */
    public void clearAll() {
        cooldowns.clear();
    }

    /**
     * Cleans up expired cooldowns to free memory.
     *
     * @return number of expired cooldowns removed
     */
    public int cleanup() {
        long now = System.currentTimeMillis();
        int removed = 0;
        var iterator = cooldowns.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue() <= now) {
                iterator.remove();
                removed++;
            }
        }
        return removed;
    }

    /**
     * Gets the name of this cooldown manager.
     *
     * @return the name
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Gets the number of active cooldowns.
     *
     * @return active cooldown count
     */
    public int size() {
        return cooldowns.size();
    }
}
