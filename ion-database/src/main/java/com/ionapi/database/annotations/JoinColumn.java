package com.ionapi.database.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the foreign key column for a relationship.
 * 
 * Example:
 * <pre>{@code
 * @ManyToOne
 * @JoinColumn(name = "guild_id", referencedColumnName = "id")
 * private Guild guild;
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JoinColumn {

    /**
     * The name of the foreign key column.
     */
    String name();

    /**
     * The name of the column referenced in the target entity.
     * Defaults to the primary key of the target entity.
     */
    String referencedColumnName() default "";

    /**
     * Whether the foreign key column is nullable.
     */
    boolean nullable() default true;

    /**
     * Whether the foreign key column should be unique.
     */
    boolean unique() default false;

    /**
     * Whether to create a foreign key constraint.
     */
    boolean foreignKey() default true;
}
