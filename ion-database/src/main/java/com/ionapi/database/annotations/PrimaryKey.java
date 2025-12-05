package com.ionapi.database.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as the primary key of an entity.
 * <p>
 * Example usage:
 * <pre>{@code
 * @Table("players")
 * public class PlayerData {
 *     @PrimaryKey
 *     private UUID uuid;
 *
 *     private String name;
 *     private int level;
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PrimaryKey {

    /**
     * Whether the primary key is auto-generated.
     * For integer keys, this enables auto-increment.
     *
     * @return true if auto-generated
     */
    boolean autoGenerate() default false;

    /**
     * Custom column name for the primary key.
     * If not specified, the field name is used.
     *
     * @return the column name
     */
    String columnName() default "";
}
