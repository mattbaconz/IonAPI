package com.ionapi.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a click event in an IonGui.
 */
public class GuiClickEvent {

    private final IonGui gui;
    private final Player player;
    private final int slot;
    private final ClickType clickType;
    private final InventoryAction action;
    private final ItemStack clickedItem;
    private final ItemStack cursor;
    private boolean cancelled;

    /**
     * Creates a new GUI click event.
     *
     * @param gui the GUI that was clicked
     * @param player the player who clicked
     * @param slot the slot that was clicked
     * @param clickType the type of click
     * @param action the inventory action
     * @param clickedItem the item that was clicked
     * @param cursor the item on the cursor
     */
    public GuiClickEvent(@NotNull IonGui gui,
                         @NotNull Player player,
                         int slot,
                         @NotNull ClickType clickType,
                         @NotNull InventoryAction action,
                         @Nullable ItemStack clickedItem,
                         @Nullable ItemStack cursor) {
        this.gui = gui;
        this.player = player;
        this.slot = slot;
        this.clickType = clickType;
        this.action = action;
        this.clickedItem = clickedItem;
        this.cursor = cursor;
        this.cancelled = true; // Default to cancelled for GUIs
    }

    /**
     * Gets the GUI that was clicked.
     *
     * @return the GUI
     */
    @NotNull
    public IonGui getGui() {
        return gui;
    }

    /**
     * Gets the player who clicked.
     *
     * @return the player
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the slot that was clicked.
     *
     * @return the slot index
     */
    public int getSlot() {
        return slot;
    }

    /**
     * Gets the row of the clicked slot (0-5).
     *
     * @return the row
     */
    public int getRow() {
        return slot / 9;
    }

    /**
     * Gets the column of the clicked slot (0-8).
     *
     * @return the column
     */
    public int getColumn() {
        return slot % 9;
    }

    /**
     * Gets the type of click.
     *
     * @return the click type
     */
    @NotNull
    public ClickType getClickType() {
        return clickType;
    }

    /**
     * Gets the inventory action.
     *
     * @return the action
     */
    @NotNull
    public InventoryAction getAction() {
        return action;
    }

    /**
     * Gets the item that was clicked.
     *
     * @return the clicked item, or null if empty
     */
    @Nullable
    public ItemStack getClickedItem() {
        return clickedItem;
    }

    /**
     * Gets the item on the player's cursor.
     *
     * @return the cursor item, or null if empty
     */
    @Nullable
    public ItemStack getCursor() {
        return cursor;
    }

    /**
     * Checks if this is a left click.
     *
     * @return true if left click
     */
    public boolean isLeftClick() {
        return clickType == ClickType.LEFT || clickType == ClickType.SHIFT_LEFT;
    }

    /**
     * Checks if this is a right click.
     *
     * @return true if right click
     */
    public boolean isRightClick() {
        return clickType == ClickType.RIGHT || clickType == ClickType.SHIFT_RIGHT;
    }

    /**
     * Checks if this is a shift click.
     *
     * @return true if shift click
     */
    public boolean isShiftClick() {
        return clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT;
    }

    /**
     * Checks if the event is cancelled.
     *
     * @return true if cancelled
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets whether the event is cancelled.
     * When cancelled, the default inventory behavior is prevented.
     *
     * @param cancelled true to cancel
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Closes the GUI for the player.
     */
    public void close() {
        gui.close(player);
    }

    /**
     * Updates the GUI display.
     */
    public void update() {
        gui.update();
    }

    /**
     * Sends a message to the player.
     *
     * @param message the message to send (supports MiniMessage)
     */
    public void sendMessage(@NotNull String message) {
        player.sendMessage(message);
    }
}
