package com.ionapi.compat.event.unified;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Unified event for entity item pickup.
 * Works on all server versions by bridging:
 * - 1.12+: EntityPickupItemEvent
 * - 1.8-1.11: PlayerPickupItemEvent (deprecated)
 * 
 * <p>Example:</p>
 * <pre>{@code
 * @IonEventHandler
 * public void onPickup(IonEntityPickupItemEvent event) {
 *     if (event.isPlayer()) {
 *         Player player = event.getPlayer();
 *         ItemStack item = event.getItemStack();
 *         player.sendMessage("You picked up " + item.getType());
 *     }
 * }
 * }</pre>
 */
public class IonEntityPickupItemEvent implements IonEvent, Cancellable {

    private final Entity entity;
    private final Item item;
    private final int remaining;
    private boolean cancelled;
    private final Object originalEvent;

    public IonEntityPickupItemEvent(@NotNull Entity entity, @NotNull Item item, 
                                     int remaining, @NotNull Object originalEvent) {
        this.entity = entity;
        this.item = item;
        this.remaining = remaining;
        this.originalEvent = originalEvent;
        this.cancelled = false;
    }

    /**
     * Gets the entity that picked up the item.
     */
    @NotNull
    public Entity getEntity() {
        return entity;
    }

    /**
     * Gets the item entity that was picked up.
     */
    @NotNull
    public Item getItem() {
        return item;
    }

    /**
     * Gets the ItemStack of the picked up item.
     */
    @NotNull
    public ItemStack getItemStack() {
        return item.getItemStack();
    }

    /**
     * Gets the remaining amount of items left on the ground.
     */
    public int getRemaining() {
        return remaining;
    }

    /**
     * Checks if the entity is a player.
     */
    public boolean isPlayer() {
        return entity instanceof Player;
    }

    /**
     * Gets the player if the entity is a player.
     */
    @Nullable
    public Player getPlayer() {
        return entity instanceof Player ? (Player) entity : null;
    }

    /**
     * Gets the original Bukkit event.
     */
    @NotNull
    public Object getOriginalEvent() {
        return originalEvent;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
        // Propagate to original event
        if (originalEvent instanceof Cancellable) {
            ((Cancellable) originalEvent).setCancelled(cancel);
        }
    }
}
