package com.ionapi.compat.event;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Unified event system that provides consistent events across all server versions.
 * Automatically bridges legacy events to modern equivalents.
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * public class MyPlugin extends JavaPlugin {
 *     @Override
 *     public void onEnable() {
 *         // Register unified event listeners
 *         IonEvents.register(this, new MyListener());
 *     }
 * }
 * 
 * public class MyListener implements Listener {
 *     // Works on ALL versions - IonEvents bridges legacy events automatically
 *     @IonEventHandler
 *     public void onItemPickup(IonEntityPickupItemEvent event) {
 *         Player player = event.getPlayer();
 *         ItemStack item = event.getItem();
 *         // Same code works on 1.8 through 1.21+
 *     }
 *     
 *     @IonEventHandler
 *     public void onOffHandSwap(IonPlayerSwapHandItemsEvent event) {
 *         // This event didn't exist before 1.9
 *         // On 1.8, it simply won't fire (graceful degradation)
 *     }
 * }
 * }</pre>
 */
public final class IonEvents {

    private static IonEventBridge bridge;

    private IonEvents() {}

    /**
     * Registers a listener with unified event handling.
     * 
     * @param plugin the owning plugin
     * @param listener the listener to register
     */
    public static void register(@NotNull Plugin plugin, @NotNull Object listener) {
        getBridge(plugin).registerListener(plugin, listener);
    }

    /**
     * Unregisters a listener.
     * 
     * @param listener the listener to unregister
     */
    public static void unregister(@NotNull Object listener) {
        if (bridge != null) {
            bridge.unregisterListener(listener);
        }
    }

    /**
     * Unregisters all listeners for a plugin.
     * 
     * @param plugin the plugin
     */
    public static void unregisterAll(@NotNull Plugin plugin) {
        if (bridge != null) {
            bridge.unregisterAll(plugin);
        }
    }

    private static IonEventBridge getBridge(Plugin plugin) {
        if (bridge == null) {
            bridge = new IonEventBridge(plugin);
        }
        return bridge;
    }
}
