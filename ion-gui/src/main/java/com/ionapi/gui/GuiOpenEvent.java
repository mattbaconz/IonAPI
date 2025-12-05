package com.ionapi.gui;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event when a GUI is opened for a player.
 */
public class GuiOpenEvent {

    private final IonGui gui;
    private final Player player;
    private boolean cancelled;

    /**
     * Creates a new GUI open event.
     *
     * @param gui the GUI being opened
     * @param player the player opening the GUI
     */
    public GuiOpenEvent(@NotNull IonGui gui, @NotNull Player player) {
        this.gui = gui;
        this.player = player;
        this.cancelled = false;
    }

    /**
     * Gets the GUI being opened.
     *
     * @return the GUI
     */
    @NotNull
    public IonGui getGui() {
        return gui;
    }

    /**
     * Gets the player opening the GUI.
     *
     * @return the player
     */
    @NotNull
    public Player getPlayer() {
        return player;
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
     * When cancelled, the GUI will not open.
     *
     * @param cancelled true to cancel
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Sends a message to the player.
     *
     * @param message the message to send
     */
    public void sendMessage(@NotNull String message) {
        player.sendMessage(message);
    }
}
