package com.ionapi.compat.event.unified;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Unified event for player armor changes.
 * Works on all versions by monitoring inventory changes.
 * 
 * <p>Example:</p>
 * <pre>{@code
 * @IonEventHandler
 * public void onArmorChange(IonPlayerArmorChangeEvent event) {
 *     Player player = event.getPlayer();
 *     SlotType slot = event.getSlotType();
 *     ItemStack oldArmor = event.getOldItem();
 *     ItemStack newArmor = event.getNewItem();
 *     
 *     if (slot == SlotType.CHEST && newArmor != null) {
 *         player.sendMessage("You equipped a chestplate!");
 *     }
 * }
 * }</pre>
 */
public class IonPlayerArmorChangeEvent implements IonEvent, Cancellable {

    private final Player player;
    private final SlotType slotType;
    private final ItemStack oldItem;
    private final ItemStack newItem;
    private boolean cancelled;
    private final Object originalEvent;

    public IonPlayerArmorChangeEvent(@NotNull Player player, @NotNull SlotType slotType,
                                      @Nullable ItemStack oldItem, @Nullable ItemStack newItem,
                                      @Nullable Object originalEvent) {
        this.player = player;
        this.slotType = slotType;
        this.oldItem = oldItem;
        this.newItem = newItem;
        this.originalEvent = originalEvent != null ? originalEvent : this;
        this.cancelled = false;
    }

    /**
     * Gets the player whose armor changed.
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the armor slot type that changed.
     */
    @NotNull
    public SlotType getSlotType() {
        return slotType;
    }

    /**
     * Gets the old armor item (may be null or air).
     */
    @Nullable
    public ItemStack getOldItem() {
        return oldItem;
    }

    /**
     * Gets the new armor item (may be null or air).
     */
    @Nullable
    public ItemStack getNewItem() {
        return newItem;
    }

    /**
     * Checks if armor was equipped (old was empty, new is not).
     */
    public boolean isEquipping() {
        return (oldItem == null || oldItem.getType().isAir()) && 
               (newItem != null && !newItem.getType().isAir());
    }

    /**
     * Checks if armor was unequipped (old was not empty, new is).
     */
    public boolean isUnequipping() {
        return (oldItem != null && !oldItem.getType().isAir()) && 
               (newItem == null || newItem.getType().isAir());
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

    /**
     * Armor slot types.
     */
    public enum SlotType {
        HELMET(39),
        CHEST(38),
        LEGS(37),
        BOOTS(36);

        private final int rawSlot;

        SlotType(int rawSlot) {
            this.rawSlot = rawSlot;
        }

        public int getRawSlot() {
            return rawSlot;
        }

        public static SlotType fromRawSlot(int slot) {
            for (SlotType type : values()) {
                if (type.rawSlot == slot) return type;
            }
            return null;
        }
    }
}
