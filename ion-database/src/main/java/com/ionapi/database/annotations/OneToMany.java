package com.ionapi.database.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a one-to-many relationship between entities.
 * The annotated field should be a Collection type.
 * 
 * Example:
 * <pre>{@code
 * @Table("guilds")
 * public class Guild {
 *     @PrimaryKey
 *     private UUID id;
 *     
 *     @OneToMany(mappedBy = "guild", fetch = FetchType.LAZY)
 *     private List<GuildMember> members;
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OneToMany {

    /**
     * The field in the target entity that owns the relationship.
     */
    String mappedBy();

    /**
     * The fetch strategy for this relationship.
     */
    FetchType fetch() default FetchType.LAZY;

    /**
     * Cascade operations to apply.
     */
    CascadeType[] cascade() default {};

    /**
     * Whether to remove orphaned entities.
     */
    boolean orphanRemoval() default false;
}
