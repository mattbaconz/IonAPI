package com.ionapi.database.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a database entity and specifies the table name.
 * <p>
 * Example usage:
 * <pre>{@code
 * @Table("players")
 * public class PlayerData {
 *     @PrimaryKey
 *     private UUID uuid;
 *     private String name;
 *     private int level;
 *     // getters/setters
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {

    /**
     * The name of the database table.
     * If not specified, the class name will be used.
     *
     * @return the table name
     */
    String value() default "";

    /**
     * The database schema name (optional).
     *
     * @return the schema name, or empty string if not specified
     */
    String schema() default "";
}
