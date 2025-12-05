package com.ionapi.placeholder;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Registry for managing IonPlaceholder implementations.
 * Automatically hooks into PlaceholderAPI when available.
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * public class MyPlugin extends JavaPlugin {
 *     @Override
 *     public void onEnable() {
 *         IonPlaceholderRegistry.create(this)
 *             .register(new MyPlaceholders())
 *             .register(new StatsPlaceholders())
 *             .build();
 *     }
 * }
 * }</pre>
 */
public final class IonPlaceholderRegistry {

    private final JavaPlugin plugin;
    private final List<IonPlaceholder> placeholders = new ArrayList<>();
    private final List<PlaceholderExpansionWrapper> registeredExpansions = new ArrayList<>();
    private boolean papiAvailable;

    private IonPlaceholderRegistry(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.papiAvailable = checkPlaceholderAPI();
    }

    /**
     * Creates a new placeholder registry for the given plugin.
     * 
     * @param plugin the owning plugin
     * @return a new registry builder
     */
    @NotNull
    public static IonPlaceholderRegistry create(@NotNull JavaPlugin plugin) {
        return new IonPlaceholderRegistry(plugin);
    }

    /**
     * Registers a placeholder implementation.
     * 
     * @param placeholder the placeholder to register
     * @return this registry for chaining
     */
    @NotNull
    public IonPlaceholderRegistry register(@NotNull IonPlaceholder placeholder) {
        placeholders.add(placeholder);
        return this;
    }

    /**
     * Builds and activates all registered placeholders.
     * If PlaceholderAPI is available, placeholders are registered with it.
     * 
     * @return this registry
     */
    @NotNull
    public IonPlaceholderRegistry build() {
        if (!papiAvailable) {
            plugin.getLogger().info("[IonPlaceholder] PlaceholderAPI not found - placeholders will not be available");
            return this;
        }

        for (IonPlaceholder placeholder : placeholders) {
            try {
                PlaceholderExpansionWrapper expansion = new PlaceholderExpansionWrapper(plugin, placeholder);
                if (expansion.register()) {
                    registeredExpansions.add(expansion);
                    plugin.getLogger().info("[IonPlaceholder] Registered placeholder expansion: " + placeholder.getIdentifier());
                } else {
                    plugin.getLogger().warning("[IonPlaceholder] Failed to register placeholder: " + placeholder.getIdentifier());
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "[IonPlaceholder] Error registering placeholder: " + placeholder.getIdentifier(), e);
            }
        }

        return this;
    }

    /**
     * Unregisters all placeholders from PlaceholderAPI.
     * Call this in your plugin's onDisable if needed.
     */
    public void unregisterAll() {
        for (PlaceholderExpansionWrapper expansion : registeredExpansions) {
            try {
                expansion.unregister();
            } catch (Exception ignored) {
            }
        }
        registeredExpansions.clear();
    }

    /**
     * Checks if PlaceholderAPI is available on the server.
     * 
     * @return true if PlaceholderAPI is available
     */
    public boolean isPlaceholderAPIAvailable() {
        return papiAvailable;
    }

    /**
     * Gets the number of registered placeholders.
     * 
     * @return the count of registered placeholders
     */
    public int getRegisteredCount() {
        return registeredExpansions.size();
    }

    private boolean checkPlaceholderAPI() {
        return plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
    }
}
