package com.ionapi.gui;

import com.ionapi.item.IonItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Anvil-based text input GUI.
 * 
 * Example:
 * <pre>{@code
 * InputGui.create(plugin)
 *     .title("Enter player name")
 *     .placeholder("Type here...")
 *     .onComplete((player, input) -> {
 *         player.sendMessage("You entered: " + input);
 *     })
 *     .onCancel(player -> {
 *         player.sendMessage("Cancelled!");
 *     })
 *     .open(player);
 * }</pre>
 */
public class InputGui implements Listener {

    private final Plugin plugin;
    private final String title;
    private final String placeholder;
    private final ItemStack inputItem;
    private final BiConsumer<Player, String> onComplete;
    private final Consumer<Player> onCancel;
    private final Predicate<String> validator;
    private final String validationError;
    private final GuiSound completeSound;
    private final GuiSound errorSound;
    private final boolean closeOnComplete;

    private Player viewer;
    private InventoryView view;
    private boolean completed;

    private InputGui(Builder builder) {
        this.plugin = builder.plugin;
        this.title = builder.title;
        this.placeholder = builder.placeholder;
        this.inputItem = builder.inputItem;
        this.onComplete = builder.onComplete;
        this.onCancel = builder.onCancel;
        this.validator = builder.validator;
        this.validationError = builder.validationError;
        this.completeSound = builder.completeSound;
        this.errorSound = builder.errorSound;
        this.closeOnComplete = builder.closeOnComplete;
        this.completed = false;
    }

    public static Builder create(@NotNull Plugin plugin) {
        return new Builder(plugin);
    }

    public static Builder create() {
        return new Builder(JavaPlugin.getProvidingPlugin(InputGui.class));
    }

    public void open(@NotNull Player player) {
        this.viewer = player;
        this.completed = false;

        // Register listener
        Bukkit.getPluginManager().registerEvents(this, plugin);

        // Create anvil inventory
        view = player.openAnvil(player.getLocation(), true);
        if (view == null) {
            HandlerList.unregisterAll(this);
            return;
        }

        // Set title if possible
        if (title != null && !title.isEmpty()) {
            view.setTitle(title);
        }

        // Set input item
        Inventory inv = view.getTopInventory();
        ItemStack item = inputItem.clone();
        ItemMeta meta = item.getItemMeta();
        if (meta != null && placeholder != null) {
            meta.displayName(Component.text(placeholder));
            item.setItemMeta(meta);
        }
        inv.setItem(0, item);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (view == null || event.getView() != view) return;

        AnvilInventory anvil = event.getInventory();
        String text = anvil.getRenameText();

        // Create result item
        ItemStack result = inputItem.clone();
        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(text != null ? text : placeholder));
            result.setItemMeta(meta);
        }
        event.setResult(result);

        // Set repair cost to 0
        anvil.setRepairCost(0);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (view == null || event.getView() != view) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        event.setCancelled(true);

        // Check if clicked result slot (slot 2)
        if (event.getRawSlot() == 2) {
            AnvilInventory anvil = (AnvilInventory) event.getInventory();
            String input = anvil.getRenameText();

            if (input == null || input.isEmpty()) {
                input = placeholder;
            }

            // Validate input
            if (validator != null && !validator.test(input)) {
                if (errorSound != null) errorSound.play(player);
                if (validationError != null) {
                    player.sendMessage(validationError);
                }
                return;
            }

            completed = true;
            if (completeSound != null) completeSound.play(player);

            if (closeOnComplete) {
                player.closeInventory();
            }

            if (onComplete != null) {
                onComplete.accept(player, input);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (view == null || event.getView() != view) return;
        if (!(event.getPlayer() instanceof Player player)) return;

        HandlerList.unregisterAll(this);
        view = null;

        if (!completed && onCancel != null) {
            onCancel.accept(player);
        }
    }

    public void close() {
        if (viewer != null) {
            viewer.closeInventory();
        }
    }

    public static class Builder {
        private final Plugin plugin;
        private String title = "Enter text";
        private String placeholder = "";
        private ItemStack inputItem;
        private BiConsumer<Player, String> onComplete;
        private Consumer<Player> onCancel;
        private Predicate<String> validator;
        private String validationError = "Invalid input!";
        private GuiSound completeSound = GuiSound.SUCCESS;
        private GuiSound errorSound = GuiSound.ERROR;
        private boolean closeOnComplete = true;

        public Builder(@NotNull Plugin plugin) {
            this.plugin = plugin;
            this.inputItem = IonItem.builder(Material.PAPER)
                .name(" ")
                .build();
        }

        public Builder title(@NotNull String title) {
            this.title = title;
            return this;
        }

        public Builder placeholder(@NotNull String placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        public Builder inputItem(@NotNull ItemStack item) {
            this.inputItem = item;
            return this;
        }

        public Builder inputItem(@NotNull Material material) {
            this.inputItem = IonItem.builder(material).name(" ").build();
            return this;
        }

        public Builder onComplete(@NotNull BiConsumer<Player, String> handler) {
            this.onComplete = handler;
            return this;
        }

        public Builder onCancel(@NotNull Consumer<Player> handler) {
            this.onCancel = handler;
            return this;
        }

        public Builder validator(@NotNull Predicate<String> validator) {
            this.validator = validator;
            return this;
        }

        public Builder validator(@NotNull Predicate<String> validator, @NotNull String errorMessage) {
            this.validator = validator;
            this.validationError = errorMessage;
            return this;
        }

        public Builder minLength(int min) {
            this.validator = s -> s != null && s.length() >= min;
            this.validationError = "Input must be at least " + min + " characters!";
            return this;
        }

        public Builder maxLength(int max) {
            this.validator = s -> s != null && s.length() <= max;
            this.validationError = "Input must be at most " + max + " characters!";
            return this;
        }

        public Builder lengthBetween(int min, int max) {
            this.validator = s -> s != null && s.length() >= min && s.length() <= max;
            this.validationError = "Input must be between " + min + " and " + max + " characters!";
            return this;
        }

        public Builder numbersOnly() {
            this.validator = s -> s != null && s.matches("\\d+");
            this.validationError = "Input must contain only numbers!";
            return this;
        }

        public Builder alphanumericOnly() {
            this.validator = s -> s != null && s.matches("[a-zA-Z0-9]+");
            this.validationError = "Input must contain only letters and numbers!";
            return this;
        }

        public Builder completeSound(@Nullable GuiSound sound) {
            this.completeSound = sound;
            return this;
        }

        public Builder errorSound(@Nullable GuiSound sound) {
            this.errorSound = sound;
            return this;
        }

        public Builder noSounds() {
            this.completeSound = null;
            this.errorSound = null;
            return this;
        }

        public Builder closeOnComplete(boolean close) {
            this.closeOnComplete = close;
            return this;
        }

        public InputGui build() {
            return new InputGui(this);
        }

        public void open(@NotNull Player player) {
            build().open(player);
        }
    }
}
