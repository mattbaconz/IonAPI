package com.ionapi.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Fluent API for creating and managing GUI inventories.
 * <p>
 * Example usage:
 * <pre>{@code
 * IonGui gui = IonGui.builder()
 *     .title("<gold><bold>Shop Menu")
 *     .rows(3)
 *     .item(10, diamondItem, click -> {
 *         click.getPlayer().sendMessage("Bought diamond!");
 *         click.close();
 *     })
 *     .fillBorder(borderItem)
 *     .build();
 *
 * gui.open(player);
 * }</pre>
 */
public interface IonGui {

    /**
     * Creates a new GUI builder with default settings.
     *
     * @return a new GUI builder
     */
    @NotNull
    static IonGuiBuilder builder() {
        return new IonGuiBuilder();
    }

    /**
     * Creates a new GUI builder with a specific title.
     *
     * @param title the GUI title (supports MiniMessage)
     * @return a new GUI builder
     */
    @NotNull
    static IonGuiBuilder builder(@NotNull String title) {
        return new IonGuiBuilder().title(title);
    }

    /**
     * Creates a new GUI builder with a specific title and rows.
     *
     * @param title the GUI title (supports MiniMessage)
     * @param rows the number of rows (1-6)
     * @return a new GUI builder
     */
    @NotNull
    static IonGuiBuilder builder(@NotNull String title, int rows) {
        return new IonGuiBuilder().title(title).rows(rows);
    }

    /**
     * Opens this GUI for the specified player.
     *
     * @param player the player to open the GUI for
     */
    void open(@NotNull Player player);

    /**
     * Closes this GUI for the specified player.
     *
     * @param player the player to close the GUI for
     */
    void close(@NotNull Player player);

    /**
     * Closes this GUI for all viewers.
     */
    void closeAll();

    /**
     * Updates the GUI for all current viewers.
     */
    void update();

    /**
     * Sets an item at the specified slot.
     *
     * @param slot the slot index (0-53)
     * @param item the item to set
     */
    void setItem(int slot, @Nullable ItemStack item);

    /**
     * Sets an item at the specified slot with a click handler.
     *
     * @param slot the slot index (0-53)
     * @param item the item to set
     * @param clickHandler the click handler
     */
    void setItem(int slot, @Nullable ItemStack item, @Nullable Consumer<GuiClickEvent> clickHandler);

    /**
     * Sets an item at the specified row and column.
     *
     * @param row the row (0-5)
     * @param col the column (0-8)
     * @param item the item to set
     */
    void setItem(int row, int col, @Nullable ItemStack item);

    /**
     * Sets an item at the specified row and column with a click handler.
     *
     * @param row the row (0-5)
     * @param col the column (0-8)
     * @param item the item to set
     * @param clickHandler the click handler
     */
    void setItem(int row, int col, @Nullable ItemStack item, @Nullable Consumer<GuiClickEvent> clickHandler);

    /**
     * Gets the item at the specified slot.
     *
     * @param slot the slot index (0-53)
     * @return the item at the slot, or null if empty
     */
    @Nullable
    ItemStack getItem(int slot);

    /**
     * Removes the item at the specified slot.
     *
     * @param slot the slot index (0-53)
     */
    void removeItem(int slot);

    /**
     * Clears all items from the GUI.
     */
    void clear();

    /**
     * Fills the entire GUI with the specified item.
     *
     * @param item the item to fill with
     */
    void fill(@Nullable ItemStack item);

    /**
     * Fills the border of the GUI with the specified item.
     *
     * @param item the item to fill with
     */
    void fillBorder(@Nullable ItemStack item);

    /**
     * Fills a rectangle area with the specified item.
     *
     * @param startSlot the starting slot
     * @param endSlot the ending slot
     * @param item the item to fill with
     */
    void fillRect(int startSlot, int endSlot, @Nullable ItemStack item);

    /**
     * Gets the title of this GUI.
     *
     * @return the GUI title
     */
    @NotNull
    Component getTitle();

    /**
     * Gets the number of rows in this GUI.
     *
     * @return the number of rows
     */
    int getRows();

    /**
     * Gets the total number of slots in this GUI.
     *
     * @return the number of slots
     */
    int getSize();

    /**
     * Gets the underlying Bukkit inventory.
     *
     * @return the bukkit inventory
     */
    @NotNull
    Inventory getInventory();

    /**
     * Checks if this GUI is currently open for any player.
     *
     * @return true if open for at least one player
     */
    boolean isOpen();

    /**
     * Gets the number of viewers currently viewing this GUI.
     *
     * @return the viewer count
     */
    int getViewerCount();

    /**
     * Sets a handler to be called when the GUI is opened.
     *
     * @param handler the open handler
     */
    void onOpen(@Nullable Consumer<GuiOpenEvent> handler);

    /**
     * Sets a handler to be called when the GUI is closed.
     *
     * @param handler the close handler
     */
    void onClose(@Nullable Consumer<GuiCloseEvent> handler);

    /**
     * Sets a global click handler for the entire GUI.
     *
     * @param handler the click handler
     */
    void onClick(@Nullable Consumer<GuiClickEvent> handler);

    /**
     * Sets whether players can take items from this GUI.
     *
     * @param allowTake true to allow taking items
     */
    void setAllowTake(boolean allowTake);

    /**
     * Sets whether players can place items into this GUI.
     *
     * @param allowPlace true to allow placing items
     */
    void setAllowPlace(boolean allowPlace);

    /**
     * Sets whether this GUI should update automatically.
     *
     * @param autoUpdate true to enable auto-update
     * @param intervalTicks the update interval in ticks
     */
    void setAutoUpdate(boolean autoUpdate, long intervalTicks);

    /**
     * Destroys this GUI and cleans up resources.
     */
    void destroy();

    // ==================== Sound Support ====================

    /**
     * Sets the sound to play when the GUI is opened.
     *
     * @param sound the sound, or null to disable
     */
    void setOpenSound(@Nullable GuiSound sound);

    /**
     * Sets the sound to play when the GUI is closed.
     *
     * @param sound the sound, or null to disable
     */
    void setCloseSound(@Nullable GuiSound sound);

    /**
     * Sets the sound to play when an item is clicked.
     *
     * @param sound the sound, or null to disable
     */
    void setClickSound(@Nullable GuiSound sound);

    // ==================== Animated Items ====================

    /**
     * Sets an animated item at the specified slot.
     *
     * @param slot the slot index
     * @param animatedItem the animated item
     */
    void setAnimatedItem(int slot, @NotNull AnimatedItem animatedItem);

    /**
     * Sets an animated item at the specified slot with a click handler.
     *
     * @param slot the slot index
     * @param animatedItem the animated item
     * @param clickHandler the click handler
     */
    void setAnimatedItem(int slot, @NotNull AnimatedItem animatedItem, @Nullable Consumer<GuiClickEvent> clickHandler);

    /**
     * Removes an animated item from the specified slot.
     *
     * @param slot the slot index
     */
    void removeAnimatedItem(int slot);

    /**
     * Gets all animated items in this GUI.
     *
     * @return map of slot to animated item
     */
    @NotNull
    Map<Integer, AnimatedItem> getAnimatedItems();

    // ==================== Drag Support ====================

    /**
     * Sets a handler for drag events.
     *
     * @param handler the drag handler
     */
    void onDrag(@Nullable Consumer<GuiDragEvent> handler);

    /**
     * Sets whether dragging items is allowed.
     *
     * @param allowDrag true to allow dragging
     */
    void setAllowDrag(boolean allowDrag);
}
