package com.ionapi.compat.event.unified;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Unified block break event with consistent drop handling across versions.
 * 
 * <p>Example:</p>
 * <pre>{@code
 * @IonEventHandler
 * public void onBreak(IonBlockBreakEvent event) {
 *     Player player = event.getPlayer();
 *     Block block = event.getBlock();
 *     
 *     // Modify drops (works on all versions)
 *     event.setDropItems(false);
 *     // Or add custom drops
 *     event.addDrop(new ItemStack(Material.DIAMOND));
 * }
 * }</pre>
 */
public class IonBlockBreakEvent implements IonEvent, Cancellable {

    private final Player player;
    private final Block block;
    private boolean dropItems;
    private int expToDrop;
    private List<ItemStack> customDrops;
    private boolean cancelled;
    private final Object originalEvent;

    public IonBlockBreakEvent(@NotNull Player player, @NotNull Block block,
                               boolean dropItems, int expToDrop,
                               @NotNull Object originalEvent) {
        this.player = player;
        this.block = block;
        this.dropItems = dropItems;
        this.expToDrop = expToDrop;
        this.originalEvent = originalEvent;
        this.cancelled = false;
        this.customDrops = new java.util.ArrayList<>();
    }

    /**
     * Gets the player who broke the block.
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the block being broken.
     */
    @NotNull
    public Block getBlock() {
        return block;
    }

    /**
     * Checks if the block will drop items.
     */
    public boolean isDropItems() {
        return dropItems;
    }

    /**
     * Sets whether the block should drop items.
     */
    public void setDropItems(boolean drop) {
        this.dropItems = drop;
    }

    /**
     * Gets the experience to drop.
     */
    public int getExpToDrop() {
        return expToDrop;
    }

    /**
     * Sets the experience to drop.
     */
    public void setExpToDrop(int exp) {
        this.expToDrop = Math.max(0, exp);
    }

    /**
     * Adds a custom drop to the block.
     */
    public void addDrop(@NotNull ItemStack item) {
        customDrops.add(item);
    }

    /**
     * Gets custom drops added to this event.
     */
    @NotNull
    public List<ItemStack> getCustomDrops() {
        return customDrops;
    }

    /**
     * Clears all custom drops.
     */
    public void clearCustomDrops() {
        customDrops.clear();
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
