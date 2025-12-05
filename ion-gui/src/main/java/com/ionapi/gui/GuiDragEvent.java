package com.ionapi.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.DragType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * Represents a drag event in an IonGui.
 */
public class GuiDragEvent {

    private final IonGui gui;
    private final Player player;
    private final DragType type;
    private final Map<Integer, ItemStack> newItems;
    private final Set<Integer> rawSlots;
    private final ItemStack oldCursor;
    private final ItemStack cursor;
    private boolean cancelled;

    public GuiDragEvent(@NotNull IonGui gui,
                        @NotNull Player player,
                        @NotNull DragType type,
                        @NotNull Map<Integer, ItemStack> newItems,
                        @NotNull Set<Integer> rawSlots,
                        @Nullable ItemStack oldCursor,
                        @Nullable ItemStack cursor) {
        this.gui = gui;
        this.player = player;
        this.type = type;
        this.newItems = newItems;
        this.rawSlots = rawSlots;
        this.oldCursor = oldCursor;
        this.cursor = cursor;
        this.cancelled = true;
    }

    @NotNull public IonGui getGui() { return gui; }
    @NotNull public Player getPlayer() { return player; }
    @NotNull public DragType getType() { return type; }
    @NotNull public Map<Integer, ItemStack> getNewItems() { return newItems; }
    @NotNull public Set<Integer> getRawSlots() { return rawSlots; }
    @Nullable public ItemStack getOldCursor() { return oldCursor; }
    @Nullable public ItemStack getCursor() { return cursor; }

    public boolean isLeftDrag() { return type == DragType.EVEN; }
    public boolean isRightDrag() { return type == DragType.SINGLE; }

    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }

    public void close() { gui.close(player); }
}
