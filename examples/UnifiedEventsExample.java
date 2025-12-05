package examples;

import com.ionapi.compat.event.IonEventHandler;
import com.ionapi.compat.event.IonEvents;
import com.ionapi.compat.event.unified.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Example demonstrating the unified event system.
 * Write event handlers once, they work on ALL server versions.
 */
public class UnifiedEventsExample extends JavaPlugin {

    @Override
    public void onEnable() {
        // Register unified event listeners
        IonEvents.register(this, new MyEventListener());
        IonEvents.register(this, new ArmorListener());
        
        getLogger().info("Unified events registered!");
    }

    @Override
    public void onDisable() {
        // Clean up
        IonEvents.unregisterAll(this);
    }

    /**
     * Example listener using unified events.
     */
    public class MyEventListener implements Listener {

        /**
         * Handles item pickup - works on ALL versions!
         * - 1.12+: Uses EntityPickupItemEvent
         * - 1.8-1.11: Uses PlayerPickupItemEvent (legacy)
         * 
         * Your code stays the same regardless of version.
         */
        @IonEventHandler
        public void onItemPickup(IonEntityPickupItemEvent event) {
            // Only handle player pickups
            if (!event.isPlayer()) return;
            
            Player player = event.getPlayer();
            ItemStack item = event.getItemStack();
            
            // Example: Prevent picking up dirt
            if (item.getType() == Material.DIRT) {
                event.setCancelled(true);
                player.sendMessage("§cYou cannot pick up dirt!");
                return;
            }
            
            // Example: Log diamond pickups
            if (item.getType() == Material.DIAMOND) {
                getLogger().info(player.getName() + " picked up " + item.getAmount() + " diamonds!");
            }
        }

        /**
         * Handles block breaking with unified drop control.
         */
        @IonEventHandler
        public void onBlockBreak(IonBlockBreakEvent event) {
            Player player = event.getPlayer();
            
            // Example: Double drops for VIPs
            if (player.hasPermission("vip.doubledrops")) {
                // Add extra drops
                for (ItemStack drop : event.getBlock().getDrops()) {
                    event.addDrop(drop.clone());
                }
            }
            
            // Example: Bonus XP
            if (event.getExpToDrop() > 0) {
                event.setExpToDrop(event.getExpToDrop() * 2);
            }
        }

        /**
         * Handles hand item swapping (F key).
         * Only fires on 1.9+ (off-hand didn't exist before).
         * On 1.8, this simply won't fire - graceful degradation.
         */
        @IonEventHandler
        public void onSwapHands(IonPlayerSwapHandItemsEvent event) {
            Player player = event.getPlayer();
            
            // Example: Prevent swapping certain items
            ItemStack mainHand = event.getMainHandItem();
            if (mainHand != null && mainHand.getType() == Material.COMPASS) {
                event.setCancelled(true);
                player.sendMessage("§cYou cannot swap the compass to your off-hand!");
            }
        }

        /**
         * Handles hotbar slot changes.
         */
        @IonEventHandler
        public void onHandChange(IonPlayerMainHandChangeEvent event) {
            Player player = event.getPlayer();
            ItemStack newItem = event.getNewItem();
            
            // Example: Notify when equipping a sword
            if (newItem != null && newItem.getType().name().endsWith("_SWORD")) {
                player.sendMessage("§7You equipped a sword!");
            }
        }
    }

    /**
     * Separate listener for armor events.
     */
    public class ArmorListener implements Listener {

        /**
         * Handles armor changes.
         * Works by monitoring inventory changes.
         */
        @IonEventHandler(priority = EventPriority.HIGH)
        public void onArmorChange(IonPlayerArmorChangeEvent event) {
            Player player = event.getPlayer();
            IonPlayerArmorChangeEvent.SlotType slot = event.getSlotType();
            
            if (event.isEquipping()) {
                player.sendMessage("§aYou equipped " + slot.name().toLowerCase() + " armor!");
            } else if (event.isUnequipping()) {
                player.sendMessage("§cYou removed " + slot.name().toLowerCase() + " armor!");
            }
        }
    }
}

/*
 * UNIFIED EVENTS SUMMARY:
 * 
 * IonEntityPickupItemEvent
 *   - Works on ALL versions (1.8+)
 *   - Bridges EntityPickupItemEvent (1.12+) and PlayerPickupItemEvent (legacy)
 *   - Provides: getEntity(), getPlayer(), getItem(), getItemStack(), getRemaining()
 * 
 * IonBlockBreakEvent
 *   - Works on ALL versions
 *   - Adds: addDrop(), getCustomDrops(), clearCustomDrops()
 *   - Provides: getPlayer(), getBlock(), isDropItems(), getExpToDrop()
 * 
 * IonPlayerSwapHandItemsEvent
 *   - Only fires on 1.9+ (off-hand feature)
 *   - Graceful degradation on 1.8 (simply doesn't fire)
 *   - Provides: getPlayer(), getMainHandItem(), getOffHandItem()
 * 
 * IonPlayerMainHandChangeEvent
 *   - Works on ALL versions
 *   - Fires when player changes hotbar slot
 *   - Provides: getPlayer(), getPreviousItem(), getNewItem(), slots
 * 
 * IonPlayerArmorChangeEvent
 *   - Works on ALL versions
 *   - Fires when armor slot changes
 *   - Provides: getPlayer(), getSlotType(), getOldItem(), getNewItem()
 *   - Helpers: isEquipping(), isUnequipping()
 * 
 * USAGE:
 * 1. Use @IonEventHandler instead of @EventHandler
 * 2. Register with IonEvents.register(plugin, listener)
 * 3. Unregister with IonEvents.unregisterAll(plugin)
 */
