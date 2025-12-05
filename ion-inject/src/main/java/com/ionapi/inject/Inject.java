package com.ionapi.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field for dependency injection.
 * Fields annotated with @Inject will be automatically populated
 * when the class is registered with {@link IonInjector}.
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * public class MyCommand implements IonCommand {
 *     @Inject
 *     private MyPlugin plugin;
 *     
 *     @Inject
 *     private PlayerService playerService;
 *     
 *     @Inject
 *     private IonConfig config;
 *     
 *     @Override
 *     public boolean execute(CommandContext ctx) {
 *         // All injected fields are available here
 *         playerService.doSomething(ctx.getPlayer());
 *         return true;
 *     }
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Inject {
    
    /**
     * Optional name qualifier for the injection.
     * Use when multiple instances of the same type exist.
     */
    String value() default "";
}
