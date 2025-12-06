package com.ionapi.database.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a many-to-one relationship between entities.
 * 
 * Example:
 * <pre>{@code
 * @Table("guild_members")
 * public class GuildMember {
 *     @PrimaryKey
 *     private UUID id;
 *     
 *     @ManyToOne(fetch = FetchType.EAGER)
 *     @JoinColumn(name = "guild_id")
 *     private Guild guild;
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ManyToOne {

    /**
     * The fetch strategy for this relationship.
     */
    FetchType fetch() default FetchType.EAGER;

    /**
     * Whether this relationship is optional (nullable).
     */
    boolean optional() default true;

    /**
     * Cascade operations to apply.
     */
    CascadeType[] cascade() default {};
}
