package com.ionapi.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Sign-based text input GUI.
 * Opens a virtual sign editor for the player to input text.
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * // Simple callback style
 * SignInput.create(plugin)
 *     .lines("", "Enter amount", "above", "")
 *     .onComplete((player, lines) -> {
 *         String input = lines.get(0);
 *         player.sendMessage("You entered: " + input);
 *     })
 *     .open(player);
 * 
 * // CompletableFuture style
 * SignInput.create(plugin)
 *     .lines("", "Enter name", "", "")
 *     .openAsync(player)
 *     .thenAccept(lines -> {
 *         String name = lines.get(0);
 *         // Process input
 *     });
 * }</pre>
 */
public class SignInput implements Listener {

    private final Plugin plugin;
    private final String[] lines;
    private final BiConsumer<Player, List<String>> onComplete;
    private final Consumer<Player> onCancel;
    private final Predicate<List<String>> validator;
    private final String validationError;

    private Player viewer;
    private Location signLocation;
    private Block originalBlock;
    private Material originalMaterial;
    private boolean completed;
    private CompletableFuture<List<String>> future;

    private SignInput(Builder builder) {
        this.plugin = builder.plugin;
        this.lines = builder.lines;
        this.onComplete = builder.onComplete;
        this.onCancel = builder.onCancel;
        this.validator = builder.validator;
        this.validationError = builder.validationError;
        this.completed = false;
    }

    /**
     * Creates a new SignInput builder.
     */
    @NotNull
    public static Builder create(@NotNull Plugin plugin) {
        return new Builder(plugin);
    }

    /**
     * Creates a new SignInput builder using the calling plugin.
     */
    @NotNull
    public static Builder create() {
        return new Builder(JavaPlugin.getProvidingPlugin(SignInput.class));
    }

    /**
     * Opens the sign input for the player.
     */
    public void open(@NotNull Player player) {
        this.viewer = player;
        this.completed = false;

        // Register listener
        Bukkit.getPluginManager().registerEvents(this, plugin);

        // Find a safe location for the fake sign (below the player)
        signLocation = player.getLocation().clone();
        signLocation.setY(Math.max(player.getWorld().getMinHeight(), signLocation.getBlockY() - 4));

        // Store original block
        originalBlock = signLocation.getBlock();
        originalMaterial = originalBlock.getType();

        // Place temporary sign
        originalBlock.setType(Material.OAK_SIGN);

        // Set sign lines
        if (originalBlock.getState() instanceof Sign sign) {
            for (int i = 0; i < 4 && i < lines.length; i++) {
                sign.getSide(Side.FRONT).line(i, Component.text(lines[i]));
            }
            sign.update();

            // Open sign editor
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.openSign(sign, Side.FRONT);
            }, 2L);
        }
    }

    /**
     * Opens the sign input and returns a CompletableFuture with the result.
     */
    @NotNull
    public CompletableFuture<List<String>> openAsync(@NotNull Player player) {
        this.future = new CompletableFuture<>();
        open(player);
        return future;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSignChange(SignChangeEvent event) {
        if (!event.getPlayer().equals(viewer)) return;
        if (!event.getBlock().getLocation().equals(signLocation)) return;

        event.setCancelled(true);

        // Get input lines
        List<String> inputLines = Arrays.asList(
            getLine(event, 0),
            getLine(event, 1),
            getLine(event, 2),
            getLine(event, 3)
        );

        // Validate
        if (validator != null && !validator.test(inputLines)) {
            if (validationError != null) {
                viewer.sendMessage(validationError);
            }
            // Reopen sign
            Bukkit.getScheduler().runTaskLater(plugin, () -> open(viewer), 1L);
            return;
        }

        completed = true;
        cleanup();

        if (onComplete != null) {
            onComplete.accept(viewer, inputLines);
        }

        if (future != null) {
            future.complete(inputLines);
        }
    }

    private String getLine(SignChangeEvent event, int index) {
        Component line = event.line(index);
        if (line == null) return "";
        return PlainTextComponentSerializer.plainText().serialize(line);
    }

    private void cleanup() {
        HandlerList.unregisterAll(this);

        // Restore original block
        if (originalBlock != null && signLocation != null) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                originalBlock.setType(originalMaterial);
            });
        }
    }

    /**
     * Closes the sign input.
     */
    public void close() {
        if (!completed && viewer != null) {
            cleanup();
            if (onCancel != null) {
                onCancel.accept(viewer);
            }
            if (future != null && !future.isDone()) {
                future.completeExceptionally(new RuntimeException("Sign input cancelled"));
            }
        }
    }

    /**
     * Builder for SignInput.
     */
    public static class Builder {
        private final Plugin plugin;
        private String[] lines = {"", "", "", ""};
        private BiConsumer<Player, List<String>> onComplete;
        private Consumer<Player> onCancel;
        private Predicate<List<String>> validator;
        private String validationError = "Invalid input!";
        private int inputLine = 0;

        public Builder(@NotNull Plugin plugin) {
            this.plugin = plugin;
        }

        /**
         * Sets all 4 lines of the sign.
         */
        @NotNull
        public Builder lines(@NotNull String line1, @NotNull String line2, 
                           @NotNull String line3, @NotNull String line4) {
            this.lines = new String[]{line1, line2, line3, line4};
            return this;
        }

        /**
         * Sets the sign lines from an array.
         */
        @NotNull
        public Builder lines(@NotNull String[] lines) {
            this.lines = Arrays.copyOf(lines, 4);
            return this;
        }

        /**
         * Sets a specific line.
         */
        @NotNull
        public Builder line(int index, @NotNull String text) {
            if (index >= 0 && index < 4) {
                this.lines[index] = text;
            }
            return this;
        }

        /**
         * Sets which line is the primary input line (for single-line validation).
         */
        @NotNull
        public Builder inputLine(int line) {
            this.inputLine = Math.max(0, Math.min(3, line));
            return this;
        }

        /**
         * Handler called when the player completes the sign.
         */
        @NotNull
        public Builder onComplete(@NotNull BiConsumer<Player, List<String>> handler) {
            this.onComplete = handler;
            return this;
        }

        /**
         * Simplified handler that only receives the primary input line.
         */
        @NotNull
        public Builder onInput(@NotNull BiConsumer<Player, String> handler) {
            final int line = this.inputLine;
            this.onComplete = (player, lines) -> handler.accept(player, lines.get(line));
            return this;
        }

        /**
         * Handler called when the player cancels.
         */
        @NotNull
        public Builder onCancel(@NotNull Consumer<Player> handler) {
            this.onCancel = handler;
            return this;
        }

        /**
         * Validates all lines.
         */
        @NotNull
        public Builder validator(@NotNull Predicate<List<String>> validator) {
            this.validator = validator;
            return this;
        }

        /**
         * Validates the primary input line.
         */
        @NotNull
        public Builder validateInput(@NotNull Predicate<String> validator) {
            final int line = this.inputLine;
            this.validator = lines -> validator.test(lines.get(line));
            return this;
        }

        /**
         * Validates the primary input line with custom error message.
         */
        @NotNull
        public Builder validateInput(@NotNull Predicate<String> validator, @NotNull String errorMessage) {
            validateInput(validator);
            this.validationError = errorMessage;
            return this;
        }

        /**
         * Requires the primary input line to not be empty.
         */
        @NotNull
        public Builder required() {
            return validateInput(s -> s != null && !s.trim().isEmpty(), "Input is required!");
        }

        /**
         * Requires the primary input line to be a number.
         */
        @NotNull
        public Builder numbersOnly() {
            return validateInput(s -> s != null && s.matches("-?\\d+"), "Please enter a number!");
        }

        /**
         * Sets the validation error message.
         */
        @NotNull
        public Builder validationError(@NotNull String message) {
            this.validationError = message;
            return this;
        }

        /**
         * Builds the SignInput.
         */
        @NotNull
        public SignInput build() {
            return new SignInput(this);
        }

        /**
         * Builds and opens the sign input for the player.
         */
        public void open(@NotNull Player player) {
            build().open(player);
        }

        /**
         * Builds and opens the sign input, returning a CompletableFuture.
         */
        @NotNull
        public CompletableFuture<List<String>> openAsync(@NotNull Player player) {
            return build().openAsync(player);
        }

        /**
         * Opens and returns a future for just the primary input line.
         */
        @NotNull
        public CompletableFuture<String> openForInput(@NotNull Player player) {
            final int line = this.inputLine;
            return build().openAsync(player).thenApply(lines -> lines.get(line));
        }
    }
}
