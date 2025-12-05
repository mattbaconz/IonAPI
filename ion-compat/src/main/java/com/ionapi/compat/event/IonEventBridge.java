package com.ionapi.compat.event;

import com.ionapi.compat.event.unified.*;
import com.ionapi.compat.version.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Internal bridge that translates Bukkit events to unified Ion events.
 */
class IonEventBridge implements Listener {

    private final Plugin plugin;
    private final Map<Class<?>, List<RegisteredHandler>> handlers = new ConcurrentHashMap<>();
    private final Map<Plugin, List<Object>> pluginListeners = new ConcurrentHashMap<>();
    private boolean registered = false;

    IonEventBridge(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    void registerListener(@NotNull Plugin owningPlugin, @NotNull Object listener) {
        // Scan for @IonEventHandler methods
        for (Method method : listener.getClass().getDeclaredMethods()) {
            IonEventHandler annotation = method.getAnnotation(IonEventHandler.class);
            if (annotation == null) continue;

            Class<?>[] params = method.getParameterTypes();
            if (params.length != 1) {
                plugin.getLogger().warning("[IonEvents] Invalid handler method: " + method.getName() 
                    + " - must have exactly one parameter");
                continue;
            }

            Class<?> eventType = params[0];
            if (!IonEvent.class.isAssignableFrom(eventType)) {
                plugin.getLogger().warning("[IonEvents] Invalid handler method: " + method.getName() 
                    + " - parameter must be an IonEvent");
                continue;
            }

            method.setAccessible(true);
            RegisteredHandler handler = new RegisteredHandler(
                listener, method, annotation.priority(), annotation.ignoreCancelled()
            );

            handlers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(handler);
            
            // Sort by priority
            handlers.get(eventType).sort(Comparator.comparingInt(h -> h.priority.ordinal()));
        }

        // Track listener for plugin
        pluginListeners.computeIfAbsent(owningPlugin, k -> new ArrayList<>()).add(listener);

        // Register Bukkit listeners if not already
        if (!registered) {
            registerBukkitListeners();
            registered = true;
        }
    }

    void unregisterListener(@NotNull Object listener) {
        for (List<RegisteredHandler> handlerList : handlers.values()) {
            handlerList.removeIf(h -> h.listener == listener);
        }
    }

    void unregisterAll(@NotNull Plugin owningPlugin) {
        List<Object> listeners = pluginListeners.remove(owningPlugin);
        if (listeners != null) {
            for (Object listener : listeners) {
                unregisterListener(listener);
            }
        }
    }

    private void registerBukkitListeners() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        // Register version-specific listeners
        registerPickupListener();
        registerSwapHandListener();
    }

    @SuppressWarnings("unchecked")
    private <T extends IonEvent> void fireEvent(T event) {
        List<RegisteredHandler> eventHandlers = handlers.get(event.getClass());
        if (eventHandlers == null || eventHandlers.isEmpty()) return;

        boolean cancelled = event instanceof Cancellable && ((Cancellable) event).isCancelled();

        for (RegisteredHandler handler : eventHandlers) {
            if (handler.ignoreCancelled && cancelled) continue;

            try {
                handler.method.invoke(handler.listener, event);
                
                // Update cancelled state
                if (event instanceof Cancellable) {
                    cancelled = ((Cancellable) event).isCancelled();
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, 
                    "[IonEvents] Error invoking handler: " + handler.method.getName(), e);
            }
        }
    }

    // ========== Bukkit Event Handlers ==========

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        IonBlockBreakEvent ionEvent = new IonBlockBreakEvent(
            event.getPlayer(),
            event.getBlock(),
            event.isDropItems(),
            event.getExpToDrop(),
            event
        );
        
        fireEvent(ionEvent);
        
        // Apply changes back
        event.setDropItems(ionEvent.isDropItems());
        event.setExpToDrop(ionEvent.getExpToDrop());
        
        // Drop custom items
        if (!ionEvent.isCancelled() && !ionEvent.getCustomDrops().isEmpty()) {
            for (ItemStack drop : ionEvent.getCustomDrops()) {
                event.getBlock().getWorld().dropItemNaturally(
                    event.getBlock().getLocation(), drop
                );
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack previousItem = player.getInventory().getItem(event.getPreviousSlot());
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());

        IonPlayerMainHandChangeEvent ionEvent = new IonPlayerMainHandChangeEvent(
            player, previousItem, newItem,
            event.getPreviousSlot(), event.getNewSlot(),
            event
        );
        
        fireEvent(ionEvent);
    }

    // ========== Version-Specific Listeners ==========

    private void registerPickupListener() {
        if (ServerVersion.isAtLeast(1, 12)) {
            // Use modern EntityPickupItemEvent
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler(priority = EventPriority.LOWEST)
                public void onPickup(org.bukkit.event.entity.EntityPickupItemEvent event) {
                    IonEntityPickupItemEvent ionEvent = new IonEntityPickupItemEvent(
                        event.getEntity(),
                        event.getItem(),
                        event.getRemaining(),
                        event
                    );
                    fireEvent(ionEvent);
                }
            }, plugin);
        } else {
            // Use legacy PlayerPickupItemEvent via reflection
            try {
                @SuppressWarnings("deprecation")
                Class<?> legacyClass = Class.forName("org.bukkit.event.player.PlayerPickupItemEvent");
                registerLegacyPickupListener(legacyClass);
            } catch (ClassNotFoundException e) {
                plugin.getLogger().warning("[IonEvents] Could not register pickup listener for this version");
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void registerLegacyPickupListener(Class<?> eventClass) {
        Bukkit.getPluginManager().registerEvent(
            eventClass.asSubclass(Event.class),
            this,
            EventPriority.LOWEST,
            (listener, event) -> {
                try {
                    Player player = (Player) event.getClass().getMethod("getPlayer").invoke(event);
                    Item item = (Item) event.getClass().getMethod("getItem").invoke(event);
                    int remaining = (int) event.getClass().getMethod("getRemaining").invoke(event);
                    
                    IonEntityPickupItemEvent ionEvent = new IonEntityPickupItemEvent(
                        player, item, remaining, event
                    );
                    fireEvent(ionEvent);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "[IonEvents] Error handling legacy pickup event", e);
                }
            },
            plugin
        );
    }

    private void registerSwapHandListener() {
        if (ServerVersion.isAtLeast(1, 9)) {
            // Off-hand exists in 1.9+
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler(priority = EventPriority.LOWEST)
                public void onSwap(org.bukkit.event.player.PlayerSwapHandItemsEvent event) {
                    IonPlayerSwapHandItemsEvent ionEvent = new IonPlayerSwapHandItemsEvent(
                        event.getPlayer(),
                        event.getMainHandItem(),
                        event.getOffHandItem(),
                        event
                    );
                    fireEvent(ionEvent);
                    
                    // Apply changes back
                    event.setMainHandItem(ionEvent.getMainHandItem());
                    event.setOffHandItem(ionEvent.getOffHandItem());
                }
            }, plugin);
        }
        // On 1.8, this event simply doesn't fire (graceful degradation)
    }

    // ========== Internal Classes ==========

    private static class RegisteredHandler {
        final Object listener;
        final Method method;
        final EventPriority priority;
        final boolean ignoreCancelled;

        RegisteredHandler(Object listener, Method method, EventPriority priority, boolean ignoreCancelled) {
            this.listener = listener;
            this.method = method;
            this.priority = priority;
            this.ignoreCancelled = ignoreCancelled;
        }
    }
}
