package com.ionapi.database.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as a database column.
 * <p>
 * Example usage:
 * <pre>{@code
 * @Table("players")
 * public class Player {
 *     @PrimaryKey
 *     private UUID id;
 *
 *     @Column(name = "player_name", nullable = false)
 *     private String name;
 *
 *     @Column(defaultValue = "0")
 *     private int level;
 *
 *     @Column(length = 500)
 *     private String description;
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {

    /**
     * The column name in the database.
     * If not specified, the field name will be used.
     *
     * @return the column name
     */
    String name() default "";

    /**
     * Whether the column can be null.
     *
     * @return true if nullable
     */
    boolean nullable() default true;

    /**
     * Whether the column should be unique.
     *
     * @return true if unique
     */
    boolean unique() default false;

    /**
     * The length of the column (for VARCHAR types).
     * Default is 255.
     *
     * @return the column length
     */
    int length() default 255;

    /**
     * The default value for the column.
     *
     * @return the default value as a string
     */
    String defaultValue() default "";

    /**
     * The column definition (for advanced usage).
     * If specified, this overrides all other properties.
     *
     * @return the column definition SQL
     */
    String columnDefinition() default "";
}
