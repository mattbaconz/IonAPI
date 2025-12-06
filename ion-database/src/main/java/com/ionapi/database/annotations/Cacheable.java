package com.ionapi.database.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an entity as cacheable to reduce database queries.
 * Cached entities are stored in memory and refreshed based on TTL.
 * 
 * Example:
 * <pre>{@code
 * @Table("player_settings")
 * @Cacheable(ttl = 60) // Cache for 60 seconds
 * public class PlayerSettings {
 *     @PrimaryKey
 *     private UUID playerId;
 *     private boolean notifications;
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Cacheable {

    /**
     * Time-to-live in seconds. After this time, cached entries are refreshed.
     * Default is 60 seconds.
     */
    int ttl() default 60;

    /**
     * Maximum number of entries to cache.
     * Default is 1000.
     */
    int maxSize() default 1000;

    /**
     * Whether to refresh the cache on write operations.
     * Default is true.
     */
    boolean refreshOnWrite() default true;
}
