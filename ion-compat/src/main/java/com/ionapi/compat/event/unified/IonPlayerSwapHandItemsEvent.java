package com.ionapi.compat.event.unified;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Unified event for player hand item swapping (F key).
 * Only fires on 1.9+ servers (off-hand didn't exist before 1.9).
 * 
 * <p>Example:</p>
 * <pre>{@code
 * @IonEventHandler
 * public void onSwap(IonPlayerSwapHandItemsEvent event) {
 *     Player player = event.getPlayer();
 *     ItemStack mainHand = event.getMainHandItem();
 *     ItemStack offHand = event.getOffHandItem();
 * }
 * }</pre>
 */
public class IonPlayerSwapHandItemsEvent implements IonEvent, Cancellable {

    private final Player player;
    private ItemStack mainHandItem;
    private ItemStack offHandItem;
    private boolean cancelled;
    private final Object originalEvent;

    public IonPlayerSwapHandItemsEvent(@NotNull Player player, 
                                        @Nullable ItemStack mainHandItem,
                                        @Nullable ItemStack offHandItem,
                                        @NotNull Object originalEvent) {
        this.player = player;
        this.mainHandItem = mainHandItem;
        this.offHandItem = offHandItem;
        this.originalEvent = originalEvent;
        this.cancelled = false;
    }

    /**
     * Gets the player who swapped items.
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the item being moved to the main hand.
     */
    @Nullable
    public ItemStack getMainHandItem() {
        return mainHandItem;
    }

    /**
     * Sets the item being moved to the main hand.
     */
    public void setMainHandItem(@Nullable ItemStack item) {
        this.mainHandItem = item;
    }

    /**
     * Gets the item being moved to the off hand.
     */
    @Nullable
    public ItemStack getOffHandItem() {
        return offHandItem;
    }

    /**
     * Sets the item being moved to the off hand.
     */
    public void setOffHandItem(@Nullable ItemStack item) {
        this.offHandItem = item;
    }

    @Override
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
        if (originalEvent instanceof Cancellable) {
            ((Cancellable) originalEvent).setCancelled(cancel);
        }
    }
}
