package com.ionapi.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a singleton service.
 * Classes annotated with @Singleton will only have one instance
 * created and shared across all injections.
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * @Singleton
 * public class PlayerService {
 *     private final Map<UUID, PlayerData> cache = new HashMap<>();
 *     
 *     public PlayerData getData(Player player) {
 *         return cache.computeIfAbsent(player.getUniqueId(), 
 *             uuid -> loadFromDatabase(uuid));
 *     }
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Singleton {
}
