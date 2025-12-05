package com.ionapi.gui;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event when a GUI is closed for a player.
 */
public class GuiCloseEvent {

    private final IonGui gui;
    private final Player player;
    private final CloseReason reason;

    /**
     * Creates a new GUI close event.
     *
     * @param gui the GUI being closed
     * @param player the player closing the GUI
     * @param reason the reason for closing
     */
    public GuiCloseEvent(@NotNull IonGui gui, @NotNull Player player, @NotNull CloseReason reason) {
        this.gui = gui;
        this.player = player;
        this.reason = reason;
    }

    /**
     * Gets the GUI being closed.
     *
     * @return the GUI
     */
    @NotNull
    public IonGui getGui() {
        return gui;
    }

    /**
     * Gets the player closing the GUI.
     *
     * @return the player
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the reason why the GUI was closed.
     *
     * @return the close reason
     */
    @NotNull
    public CloseReason getReason() {
        return reason;
    }

    /**
     * Sends a message to the player.
     *
     * @param message the message to send
     */
    public void sendMessage(@NotNull String message) {
        player.sendMessage(message);
    }

    /**
     * Represents the reason a GUI was closed.
     */
    public enum CloseReason {
        /** The player manually closed the GUI (ESC key or inventory button) */
        PLAYER,
        /** The GUI was closed programmatically */
        PLUGIN,
        /** The player disconnected */
        DISCONNECT,
        /** The player died */
        DEATH,
        /** Another inventory was opened */
        OPEN_NEW,
        /** Unknown or other reason */
        UNKNOWN
    }
}
