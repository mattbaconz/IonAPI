package com.ionapi.inject;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Lightweight dependency injection container for IonAPI plugins.
 * Automatically injects dependencies into fields annotated with {@link Inject}.
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * public class MyPlugin extends JavaPlugin {
 *     private IonInjector injector;
 *     
 *     @Override
 *     public void onEnable() {
 *         injector = IonInjector.create(this)
 *             .register(PlayerService.class)
 *             .register(EconomyService.class)
 *             .register(IonConfig.class, () -> getConfig())
 *             .build();
 *         
 *         // Register command with injection
 *         MyCommand cmd = injector.create(MyCommand.class);
 *         getCommand("mycommand").setExecutor(cmd);
 *         
 *         // Or inject into existing instance
 *         MyListener listener = new MyListener();
 *         injector.inject(listener);
 *         getServer().getPluginManager().registerEvents(listener, this);
 *     }
 * }
 * }</pre>
 */
public final class IonInjector {

    private final JavaPlugin plugin;
    private final Map<Class<?>, Object> singletons = new HashMap<>();
    private final Map<Class<?>, Supplier<?>> factories = new HashMap<>();
    private final Map<String, Object> namedBindings = new HashMap<>();

    private IonInjector(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        // Auto-register the plugin itself
        singletons.put(plugin.getClass(), plugin);
        singletons.put(JavaPlugin.class, plugin);
    }

    /**
     * Creates a new injector builder for the given plugin.
     */
    @NotNull
    public static Builder create(@NotNull JavaPlugin plugin) {
        return new Builder(plugin);
    }

    /**
     * Creates an instance of the given class with dependencies injected.
     * 
     * @param clazz the class to instantiate
     * @return the created instance with injected dependencies
     */
    @NotNull
    public <T> T create(@NotNull Class<T> clazz) {
        try {
            // Check if it's a registered singleton
            if (singletons.containsKey(clazz)) {
                return clazz.cast(singletons.get(clazz));
            }

            // Check if there's a factory
            if (factories.containsKey(clazz)) {
                T instance = clazz.cast(factories.get(clazz).get());
                inject(instance);
                return instance;
            }

            // Create new instance
            T instance = instantiate(clazz);
            inject(instance);

            // Cache if singleton
            if (clazz.isAnnotationPresent(Singleton.class)) {
                singletons.put(clazz, instance);
            }

            return instance;
        } catch (Exception e) {
            throw new InjectionException("Failed to create instance of " + clazz.getName(), e);
        }
    }

    /**
     * Injects dependencies into an existing instance.
     * 
     * @param instance the instance to inject into
     */
    public void inject(@NotNull Object instance) {
        Class<?> clazz = instance.getClass();
        
        // Process all fields including inherited ones
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    injectField(instance, field);
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    /**
     * Gets a registered instance by type.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T get(@NotNull Class<T> clazz) {
        if (singletons.containsKey(clazz)) {
            return (T) singletons.get(clazz);
        }
        if (factories.containsKey(clazz)) {
            return (T) factories.get(clazz).get();
        }
        return null;
    }

    /**
     * Gets a named binding.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T get(@NotNull String name) {
        return (T) namedBindings.get(name);
    }

    /**
     * Registers a singleton instance.
     */
    public <T> void registerInstance(@NotNull Class<T> clazz, @NotNull T instance) {
        singletons.put(clazz, instance);
    }

    /**
     * Registers a factory for creating instances.
     */
    public <T> void registerFactory(@NotNull Class<T> clazz, @NotNull Supplier<T> factory) {
        factories.put(clazz, factory);
    }

    private void injectField(Object instance, Field field) {
        try {
            field.setAccessible(true);
            
            Inject annotation = field.getAnnotation(Inject.class);
            String name = annotation.value();
            
            Object value;
            if (!name.isEmpty()) {
                // Named injection
                value = namedBindings.get(name);
            } else {
                // Type-based injection
                value = resolve(field.getType());
            }

            if (value != null) {
                field.set(instance, value);
            } else {
                plugin.getLogger().warning("[IonInjector] Could not resolve dependency for field: " 
                    + field.getDeclaringClass().getSimpleName() + "." + field.getName() 
                    + " (type: " + field.getType().getSimpleName() + ")");
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[IonInjector] Failed to inject field: " + field.getName(), e);
        }
    }

    @Nullable
    private Object resolve(Class<?> type) {
        // Check singletons first
        if (singletons.containsKey(type)) {
            return singletons.get(type);
        }

        // Check factories
        if (factories.containsKey(type)) {
            return factories.get(type).get();
        }

        // Try to find a compatible singleton
        for (Map.Entry<Class<?>, Object> entry : singletons.entrySet()) {
            if (type.isAssignableFrom(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Try to create if it's a concrete class with @Singleton
        if (!type.isInterface() && !java.lang.reflect.Modifier.isAbstract(type.getModifiers())) {
            if (type.isAnnotationPresent(Singleton.class)) {
                return create(type);
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T instantiate(Class<T> clazz) throws Exception {
        // Try no-arg constructor first
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException ignored) {}

        // Try constructor with plugin parameter
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor(JavaPlugin.class);
            constructor.setAccessible(true);
            return constructor.newInstance(plugin);
        } catch (NoSuchMethodException ignored) {}

        // Try constructor with plugin's specific type
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor(plugin.getClass());
            constructor.setAccessible(true);
            return constructor.newInstance(plugin);
        } catch (NoSuchMethodException ignored) {}

        throw new InjectionException("No suitable constructor found for " + clazz.getName() 
            + ". Add a no-arg constructor or a constructor accepting JavaPlugin.");
    }

    /**
     * Builder for IonInjector.
     */
    public static class Builder {
        private final IonInjector injector;

        private Builder(@NotNull JavaPlugin plugin) {
            this.injector = new IonInjector(plugin);
        }

        /**
         * Registers a class for injection.
         * If the class is annotated with @Singleton, only one instance will be created.
         */
        @NotNull
        public Builder register(@NotNull Class<?> clazz) {
            if (clazz.isAnnotationPresent(Singleton.class)) {
                // Create singleton immediately
                Object instance = injector.create(clazz);
                injector.singletons.put(clazz, instance);
            }
            return this;
        }

        /**
         * Registers a singleton instance.
         */
        @NotNull
        public <T> Builder register(@NotNull Class<T> clazz, @NotNull T instance) {
            injector.singletons.put(clazz, instance);
            return this;
        }

        /**
         * Registers a factory for creating instances.
         */
        @NotNull
        public <T> Builder register(@NotNull Class<T> clazz, @NotNull Supplier<T> factory) {
            injector.factories.put(clazz, factory);
            return this;
        }

        /**
         * Registers a named binding.
         */
        @NotNull
        public Builder registerNamed(@NotNull String name, @NotNull Object instance) {
            injector.namedBindings.put(name, instance);
            return this;
        }

        /**
         * Builds the injector.
         */
        @NotNull
        public IonInjector build() {
            return injector;
        }
    }
}
