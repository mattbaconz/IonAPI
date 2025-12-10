package com.ionapi.gui;

import com.ionapi.item.IonItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A simple confirmation dialog GUI.
 * Provides a yes/no interface for user confirmations.
 *
 * <p>
 * Example usage:
 * 
 * <pre>{@code
 * ConfirmationGui.create()
 *         .title("<red>⚠ Confirm Action")
 *         .message("<gray>Are you sure you want to delete all data?")
 *         .onConfirm(player -> {
 *             deleteAllData();
 *             player.sendMessage("Data deleted!");
 *         })
 *         .onCancel(player -> player.sendMessage("Cancelled"))
 *         .open(player);
 * }</pre>
 *
 * @since 1.3.0
 */
public class ConfirmationGui {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private final Component title;
    private final Component message;
    private final ItemStack confirmItem;
    private final ItemStack cancelItem;
    private final ItemStack infoItem;
    private final ItemStack fillerItem;
    private final Consumer<Player> onConfirm;
    private final Consumer<Player> onCancel;
    private final GuiSound confirmSound;
    private final GuiSound cancelSound;
    private final int rows;

    private IonGui gui;

    private ConfirmationGui(Builder builder) {
        this.title = builder.title;
        this.message = builder.message;
        this.confirmItem = builder.confirmItem;
        this.cancelItem = builder.cancelItem;
        this.infoItem = builder.infoItem;
        this.fillerItem = builder.fillerItem;
        this.onConfirm = builder.onConfirm;
        this.onCancel = builder.onCancel;
        this.confirmSound = builder.confirmSound;
        this.cancelSound = builder.cancelSound;
        this.rows = builder.rows;
    }

    /**
     * Creates a new confirmation GUI builder.
     *
     * @return a new builder
     */
    @NotNull
    public static Builder create() {
        return new Builder();
    }

    /**
     * Creates a simple confirmation with default styling.
     *
     * @param message   the confirmation message
     * @param onConfirm action when confirmed
     * @return a new builder
     */
    @NotNull
    public static Builder simple(@NotNull String message, @NotNull Consumer<Player> onConfirm) {
        return new Builder()
                .message(message)
                .onConfirm(onConfirm);
    }

    /**
     * Opens this confirmation GUI for a player.
     *
     * @param player the player
     */
    public void open(@NotNull Player player) {
        buildGui();
        gui.open(player);
    }

    /**
     * Closes this confirmation GUI.
     *
     * @param player the player
     */
    public void close(@NotNull Player player) {
        if (gui != null) {
            gui.close(player);
        }
    }

    private void buildGui() {
        IonGuiBuilder builder = IonGui.builder()
                .title(title)
                .rows(rows);

        // Fill background
        if (fillerItem != null) {
            builder.fillBuilder(fillerItem);
        }

        // Add info item in the center-top
        if (infoItem != null) {
            int infoSlot = 4; // Top center
            if (rows >= 3) {
                infoSlot = 13; // Second row center for larger GUIs
            }
            builder.item(infoSlot, infoItem);
        }

        // Calculate button positions based on rows
        int confirmSlot, cancelSlot;
        if (rows == 3) {
            confirmSlot = 11; // Left-center
            cancelSlot = 15; // Right-center
        } else if (rows == 4) {
            confirmSlot = 20;
            cancelSlot = 24;
        } else {
            confirmSlot = 29;
            cancelSlot = 33;
        }

        // Add confirm button
        builder.item(confirmSlot, confirmItem, click -> {
            if (confirmSound != null) {
                confirmSound.play(click.getPlayer());
            }
            click.close();
            if (onConfirm != null) {
                onConfirm.accept(click.getPlayer());
            }
        });

        // Add cancel button
        builder.item(cancelSlot, cancelItem, click -> {
            if (cancelSound != null) {
                cancelSound.play(click.getPlayer());
            }
            click.close();
            if (onCancel != null) {
                onCancel.accept(click.getPlayer());
            }
        });

        // Handle close as cancel
        builder.onCloseHandler(event -> {
            if (onCancel != null) {
                // Use scheduler to avoid issues with closing during event
                org.bukkit.Bukkit.getScheduler().runTask(
                        org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(ConfirmationGui.class),
                        () -> onCancel.accept(event.getPlayer()));
            }
        });

        gui = builder.build();
    }

    /**
     * Builder for ConfirmationGui.
     */
    public static class Builder {
        private Component title = MINI.deserialize("<red><bold>⚠ Confirm");
        private Component message = MINI.deserialize("<gray>Are you sure?");
        private ItemStack confirmItem;
        private ItemStack cancelItem;
        private ItemStack infoItem;
        private ItemStack fillerItem;
        private Consumer<Player> onConfirm;
        private Consumer<Player> onCancel;
        private GuiSound confirmSound = GuiSound.SUCCESS;
        private GuiSound cancelSound = GuiSound.CLOSE;
        private int rows = 3;
        private boolean closeOnCancel = true;

        public Builder() {
            // Default confirm button
            confirmItem = IonItem.builder(Material.LIME_STAINED_GLASS_PANE)
                    .name("<green><bold>✓ Confirm")
                    .lore("<gray>Click to confirm")
                    .build();

            // Default cancel button
            cancelItem = IonItem.builder(Material.RED_STAINED_GLASS_PANE)
                    .name("<red><bold>✗ Cancel")
                    .lore("<gray>Click to cancel")
                    .build();

            // Default filler
            fillerItem = IonItem.builder(Material.GRAY_STAINED_GLASS_PANE)
                    .name(" ")
                    .build();
        }

        /**
         * Sets the GUI title.
         *
         * @param title the title (supports MiniMessage)
         * @return this builder
         */
        @NotNull
        public Builder title(@NotNull String title) {
            this.title = MINI.deserialize(title);
            return this;
        }

        /**
         * Sets the GUI title.
         *
         * @param title the title component
         * @return this builder
         */
        @NotNull
        public Builder title(@NotNull Component title) {
            this.title = title;
            return this;
        }

        /**
         * Sets the confirmation message.
         *
         * @param message the message (supports MiniMessage)
         * @return this builder
         */
        @NotNull
        public Builder message(@NotNull String message) {
            this.message = MINI.deserialize(message);
            // Also update the info item with the message
            this.infoItem = IonItem.builder(Material.PAPER)
                    .name("<yellow><bold>Confirmation Required")
                    .lore("", message, "")
                    .build();
            return this;
        }

        /**
         * Sets the message displayed in the info item.
         *
         * @param message the message component
         * @return this builder
         */
        @NotNull
        public Builder message(@NotNull Component message) {
            this.message = message;
            return this;
        }

        /**
         * Sets the number of rows.
         *
         * @param rows the number of rows (3-6)
         * @return this builder
         */
        @NotNull
        public Builder rows(int rows) {
            this.rows = Math.max(3, Math.min(6, rows));
            return this;
        }

        /**
         * Sets a custom confirm button item.
         *
         * @param item the item
         * @return this builder
         */
        @NotNull
        public Builder confirmItem(@NotNull ItemStack item) {
            this.confirmItem = item;
            return this;
        }

        /**
         * Sets a custom cancel button item.
         *
         * @param item the item
         * @return this builder
         */
        @NotNull
        public Builder cancelItem(@NotNull ItemStack item) {
            this.cancelItem = item;
            return this;
        }

        /**
         * Sets a custom info item (displays the message).
         *
         * @param item the item
         * @return this builder
         */
        @NotNull
        public Builder infoItem(@NotNull ItemStack item) {
            this.infoItem = item;
            return this;
        }

        /**
         * Sets the filler item for empty slots.
         *
         * @param item the item, or null to disable
         * @return this builder
         */
        @NotNull
        public Builder fillerItem(@Nullable ItemStack item) {
            this.fillerItem = item;
            return this;
        }

        /**
         * Sets the action to run when confirmed.
         *
         * @param action the action
         * @return this builder
         */
        @NotNull
        public Builder onConfirm(@NotNull Consumer<Player> action) {
            this.onConfirm = action;
            return this;
        }

        /**
         * Sets the action to run when cancelled.
         *
         * @param action the action
         * @return this builder
         */
        @NotNull
        public Builder onCancel(@NotNull Consumer<Player> action) {
            this.onCancel = action;
            return this;
        }

        /**
         * Sets the sound to play on confirmation.
         *
         * @param sound the sound, or null to disable
         * @return this builder
         */
        @NotNull
        public Builder confirmSound(@Nullable GuiSound sound) {
            this.confirmSound = sound;
            return this;
        }

        /**
         * Sets the sound to play on cancellation.
         *
         * @param sound the sound, or null to disable
         * @return this builder
         */
        @NotNull
        public Builder cancelSound(@Nullable GuiSound sound) {
            this.cancelSound = sound;
            return this;
        }

        /**
         * Disables all sounds.
         *
         * @return this builder
         */
        @NotNull
        public Builder noSounds() {
            this.confirmSound = null;
            this.cancelSound = null;
            return this;
        }

        /**
         * Uses danger styling (red theme).
         *
         * @return this builder
         */
        @NotNull
        public Builder danger() {
            this.title = MINI.deserialize("<red><bold>⚠ Warning");
            this.confirmItem = IonItem.builder(Material.RED_CONCRETE)
                    .name("<red><bold>✓ Yes, I'm sure")
                    .lore("<gray>This action cannot be undone!")
                    .glow()
                    .build();
            return this;
        }

        /**
         * Uses success styling (green theme).
         *
         * @return this builder
         */
        @NotNull
        public Builder success() {
            this.title = MINI.deserialize("<green><bold>✓ Confirm");
            this.confirmItem = IonItem.builder(Material.LIME_CONCRETE)
                    .name("<green><bold>✓ Confirm")
                    .lore("<gray>Click to proceed")
                    .build();
            return this;
        }

        /**
         * Builds the confirmation GUI.
         *
         * @return the built confirmation GUI
         */
        @NotNull
        public ConfirmationGui build() {
            return new ConfirmationGui(this);
        }

        /**
         * Builds and opens the confirmation GUI for a player.
         *
         * @param player the player
         */
        public void open(@NotNull Player player) {
            build().open(player);
        }
    }
}
