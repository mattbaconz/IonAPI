package com.ionapi.compat.event.unified;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Unified event for when a player's main hand item changes.
 * Fires when the player switches hotbar slots or the item in their hand changes.
 * 
 * <p>Example:</p>
 * <pre>{@code
 * @IonEventHandler
 * public void onHandChange(IonPlayerMainHandChangeEvent event) {
 *     Player player = event.getPlayer();
 *     ItemStack newItem = event.getNewItem();
 *     
 *     if (newItem != null && newItem.getType() == Material.DIAMOND_SWORD) {
 *         player.sendMessage("You equipped a diamond sword!");
 *     }
 * }
 * }</pre>
 */
public class IonPlayerMainHandChangeEvent implements IonEvent {

    private final Player player;
    private final ItemStack previousItem;
    private final ItemStack newItem;
    private final int previousSlot;
    private final int newSlot;
    private final Object originalEvent;

    public IonPlayerMainHandChangeEvent(@NotNull Player player,
                                         @Nullable ItemStack previousItem,
                                         @Nullable ItemStack newItem,
                                         int previousSlot,
                                         int newSlot,
                                         @NotNull Object originalEvent) {
        this.player = player;
        this.previousItem = previousItem;
        this.newItem = newItem;
        this.previousSlot = previousSlot;
        this.newSlot = newSlot;
        this.originalEvent = originalEvent;
    }

    /**
     * Gets the player whose hand item changed.
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the previous item in hand (may be null or air).
     */
    @Nullable
    public ItemStack getPreviousItem() {
        return previousItem;
    }

    /**
     * Gets the new item in hand (may be null or air).
     */
    @Nullable
    public ItemStack getNewItem() {
        return newItem;
    }

    /**
     * Gets the previous hotbar slot.
     */
    public int getPreviousSlot() {
        return previousSlot;
    }

    /**
     * Gets the new hotbar slot.
     */
    public int getNewSlot() {
        return newSlot;
    }

    @Override
    @NotNull
    public Object getOriginalEvent() {
        return originalEvent;
    }
}
