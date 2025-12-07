package com.ionapi.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Builder for creating IonGui instances with full feature support.
 */
public class IonGuiBuilder implements IonGui {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final Plugin PLUGIN = JavaPlugin.getProvidingPlugin(IonGuiBuilder.class);

    private Component title;
    private int rows;
    private Inventory inventory;
    private final Map<Integer, Consumer<GuiClickEvent>> clickHandlers;
    private final Map<Integer, AnimatedItem> animatedItems;
    private Consumer<GuiOpenEvent> openHandler;
    private Consumer<GuiCloseEvent> closeHandler;
    private Consumer<GuiClickEvent> globalClickHandler;
    private Consumer<GuiDragEvent> dragHandler;
    private boolean allowTake;
    private boolean allowPlace;
    private boolean allowDrag;
    private boolean autoUpdate;
    private long updateInterval;
    private GuiListener listener;
    private BukkitTask animationTask;
    private boolean built;

    // Sound settings
    private GuiSound openSound;
    private GuiSound closeSound;
    private GuiSound clickSound;


    public IonGuiBuilder() {
        this.title = Component.text("Inventory");
        this.rows = 3;
        this.clickHandlers = new HashMap<>();
        this.animatedItems = new HashMap<>();
        this.allowTake = false;
        this.allowPlace = false;
        this.allowDrag = false;
        this.autoUpdate = false;
        this.updateInterval = 20L;
        this.built = false;
    }

    @NotNull
    public IonGuiBuilder title(@NotNull String title) {
        this.title = MINI_MESSAGE.deserialize(title);
        if (built) rebuildInventory();
        return this;
    }

    @NotNull
    public IonGuiBuilder title(@NotNull Component title) {
        this.title = title;
        if (built) rebuildInventory();
        return this;
    }

    @NotNull
    public IonGuiBuilder rows(int rows) {
        this.rows = Math.max(1, Math.min(6, rows));
        if (built) rebuildInventory();
        return this;
    }

    @NotNull
    public IonGuiBuilder item(int slot, @Nullable ItemStack item) {
        ensureBuilt();
        inventory.setItem(slot, item);
        return this;
    }

    @NotNull
    public IonGuiBuilder item(int slot, @Nullable ItemStack item, @Nullable Consumer<GuiClickEvent> clickHandler) {
        ensureBuilt();
        inventory.setItem(slot, item);
        if (clickHandler != null) clickHandlers.put(slot, clickHandler);
        return this;
    }

    @NotNull
    public IonGuiBuilder item(int row, int col, @Nullable ItemStack item) {
        return item(row * 9 + col, item);
    }

    @NotNull
    public IonGuiBuilder item(int row, int col, @Nullable ItemStack item, @Nullable Consumer<GuiClickEvent> clickHandler) {
        return item(row * 9 + col, item, clickHandler);
    }

    @NotNull
    public IonGuiBuilder fillBuilder(@Nullable ItemStack item) {
        ensureBuilt();
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, item);
        }
        return this;
    }

    @NotNull
    public IonGuiBuilder fillBorderBuilder(@Nullable ItemStack item) {
        ensureBuilt();
        int size = inventory.getSize();
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, item);
            inventory.setItem(size - 9 + i, item);
        }
        for (int i = 1; i < rows - 1; i++) {
            inventory.setItem(i * 9, item);
            inventory.setItem(i * 9 + 8, item);
        }
        return this;
    }

    @NotNull
    public IonGuiBuilder fillRectBuilder(int startSlot, int endSlot, @Nullable ItemStack item) {
        ensureBuilt();
        for (int i = startSlot; i <= endSlot && i < inventory.getSize(); i++) {
            inventory.setItem(i, item);
        }
        return this;
    }

    @NotNull
    public IonGuiBuilder fillSlots(@Nullable ItemStack item, int... slots) {
        ensureBuilt();
        for (int slot : slots) {
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, item);
            }
        }
        return this;
    }

    // Sound builder methods
    @NotNull
    public IonGuiBuilder openSound(@Nullable GuiSound sound) {
        this.openSound = sound;
        return this;
    }

    @NotNull
    public IonGuiBuilder closeSound(@Nullable GuiSound sound) {
        this.closeSound = sound;
        return this;
    }

    @NotNull
    public IonGuiBuilder clickSound(@Nullable GuiSound sound) {
        this.clickSound = sound;
        return this;
    }

    @NotNull
    public IonGuiBuilder noSounds() {
        this.openSound = null;
        this.closeSound = null;
        this.clickSound = null;
        return this;
    }

    // Animated item builder method
    @NotNull
    public IonGuiBuilder animatedItem(int slot, @NotNull AnimatedItem animatedItem) {
        ensureBuilt();
        animatedItems.put(slot, animatedItem);
        inventory.setItem(slot, animatedItem.getCurrentFrame());
        return this;
    }

    @NotNull
    public IonGuiBuilder animatedItem(int slot, @NotNull AnimatedItem animatedItem, @Nullable Consumer<GuiClickEvent> clickHandler) {
        animatedItem(slot, animatedItem);
        if (clickHandler != null) clickHandlers.put(slot, clickHandler);
        return this;
    }

    // Handler builder methods
    @NotNull
    public IonGuiBuilder onOpenHandler(@Nullable Consumer<GuiOpenEvent> handler) {
        this.openHandler = handler;
        return this;
    }

    @NotNull
    public IonGuiBuilder onCloseHandler(@Nullable Consumer<GuiCloseEvent> handler) {
        this.closeHandler = handler;
        return this;
    }

    @NotNull
    public IonGuiBuilder onClickHandler(@Nullable Consumer<GuiClickEvent> handler) {
        this.globalClickHandler = handler;
        return this;
    }

    @NotNull
    public IonGuiBuilder onDragHandler(@Nullable Consumer<GuiDragEvent> handler) {
        this.dragHandler = handler;
        return this;
    }

    @NotNull
    public IonGuiBuilder allowTake(boolean allowTake) {
        this.allowTake = allowTake;
        return this;
    }

    @NotNull
    public IonGuiBuilder allowPlace(boolean allowPlace) {
        this.allowPlace = allowPlace;
        return this;
    }

    @NotNull
    public IonGuiBuilder allowDrag(boolean allowDrag) {
        this.allowDrag = allowDrag;
        return this;
    }

    @NotNull
    public IonGuiBuilder autoUpdate(boolean autoUpdate, long intervalTicks) {
        this.autoUpdate = autoUpdate;
        this.updateInterval = intervalTicks;
        return this;
    }

    @NotNull
    public IonGui build() {
        ensureBuilt();
        registerListener();
        startAnimations();
        return this;
    }


    // ==================== IonGui Implementation ====================

    @Override
    public void open(@NotNull Player player) {
        ensureBuilt();
        GuiOpenEvent event = new GuiOpenEvent(this, player);
        if (openHandler != null) openHandler.accept(event);
        if (!event.isCancelled()) {
            player.openInventory(inventory);
            if (openSound != null) openSound.play(player);
        }
    }

    @Override
    public void close(@NotNull Player player) {
        if (closeSound != null) closeSound.play(player);
        player.closeInventory();
    }

    @Override
    public void closeAll() {
        inventory.getViewers().forEach(viewer -> {
            if (viewer instanceof Player p && closeSound != null) closeSound.play(p);
            viewer.closeInventory();
        });
    }

    @Override
    public void update() {
        // Update animated items
        for (Map.Entry<Integer, AnimatedItem> entry : animatedItems.entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue().getCurrentFrame());
        }
        inventory.getViewers().forEach(viewer -> {
            if (viewer instanceof Player player) player.updateInventory();
        });
    }

    @Override
    public void setItem(int slot, @Nullable ItemStack item) {
        ensureBuilt();
        inventory.setItem(slot, item);
    }

    @Override
    public void setItem(int slot, @Nullable ItemStack item, @Nullable Consumer<GuiClickEvent> clickHandler) {
        setItem(slot, item);
        if (clickHandler != null) clickHandlers.put(slot, clickHandler);
        else clickHandlers.remove(slot);
    }

    @Override
    public void setItem(int row, int col, @Nullable ItemStack item) {
        setItem(row * 9 + col, item);
    }

    @Override
    public void setItem(int row, int col, @Nullable ItemStack item, @Nullable Consumer<GuiClickEvent> clickHandler) {
        setItem(row * 9 + col, item, clickHandler);
    }

    @Override
    public @Nullable ItemStack getItem(int slot) {
        ensureBuilt();
        return inventory.getItem(slot);
    }

    @Override
    public void removeItem(int slot) {
        setItem(slot, null);
        clickHandlers.remove(slot);
        animatedItems.remove(slot);
    }

    @Override
    public void clear() {
        ensureBuilt();
        inventory.clear();
        clickHandlers.clear();
        animatedItems.clear();
    }

    @Override
    public void fill(@Nullable ItemStack item) { fillBuilder(item); }

    @Override
    public void fillBorder(@Nullable ItemStack item) { fillBorderBuilder(item); }

    @Override
    public void fillRect(int startSlot, int endSlot, @Nullable ItemStack item) { fillRectBuilder(startSlot, endSlot, item); }

    @Override
    public @NotNull Component getTitle() { return title; }

    @Override
    public int getRows() { return rows; }

    @Override
    public int getSize() { return rows * 9; }

    @Override
    public @NotNull Inventory getInventory() {
        ensureBuilt();
        return inventory;
    }

    @Override
    public boolean isOpen() { return inventory != null && !inventory.getViewers().isEmpty(); }

    @Override
    public int getViewerCount() { return inventory == null ? 0 : inventory.getViewers().size(); }

    @Override
    public void onOpen(@Nullable Consumer<GuiOpenEvent> handler) { this.openHandler = handler; }

    @Override
    public void onClose(@Nullable Consumer<GuiCloseEvent> handler) { this.closeHandler = handler; }

    @Override
    public void onClick(@Nullable Consumer<GuiClickEvent> handler) { this.globalClickHandler = handler; }

    @Override
    public void onDrag(@Nullable Consumer<GuiDragEvent> handler) { this.dragHandler = handler; }

    @Override
    public void setAllowTake(boolean allowTake) { this.allowTake = allowTake; }

    @Override
    public void setAllowPlace(boolean allowPlace) { this.allowPlace = allowPlace; }

    @Override
    public void setAllowDrag(boolean allowDrag) { this.allowDrag = allowDrag; }

    @Override
    public void setAutoUpdate(boolean autoUpdate, long intervalTicks) {
        this.autoUpdate = autoUpdate;
        this.updateInterval = intervalTicks;
        if (autoUpdate && built) startAutoUpdate();
    }

    @Override
    public void setOpenSound(@Nullable GuiSound sound) { this.openSound = sound; }

    @Override
    public void setCloseSound(@Nullable GuiSound sound) { this.closeSound = sound; }

    @Override
    public void setClickSound(@Nullable GuiSound sound) { this.clickSound = sound; }

    @Override
    public void setAnimatedItem(int slot, @NotNull AnimatedItem animatedItem) {
        ensureBuilt();
        animatedItems.put(slot, animatedItem);
        inventory.setItem(slot, animatedItem.getCurrentFrame());
    }

    @Override
    public void setAnimatedItem(int slot, @NotNull AnimatedItem animatedItem, @Nullable Consumer<GuiClickEvent> clickHandler) {
        setAnimatedItem(slot, animatedItem);
        if (clickHandler != null) clickHandlers.put(slot, clickHandler);
    }

    @Override
    public void removeAnimatedItem(int slot) {
        animatedItems.remove(slot);
    }

    @Override
    public @NotNull Map<Integer, AnimatedItem> getAnimatedItems() {
        return new HashMap<>(animatedItems);
    }

    @Override
    public void destroy() {
        closeAll();
        if (listener != null) {
            HandlerList.unregisterAll(listener);
            listener = null;
        }
        if (animationTask != null) {
            animationTask.cancel();
            animationTask = null;
        }
        clickHandlers.clear();
        animatedItems.clear();
        openHandler = null;
        closeHandler = null;
        globalClickHandler = null;
        dragHandler = null;
    }


    // ==================== Private Helper Methods ====================

    private void ensureBuilt() {
        if (!built) {
            inventory = Bukkit.createInventory(new GuiHolder(this), rows * 9, title);
            built = true;
        }
    }

    private void rebuildInventory() {
        if (inventory == null) return;
        ItemStack[] contents = inventory.getContents();
        inventory = Bukkit.createInventory(new GuiHolder(this), rows * 9, title);
        int copySize = Math.min(contents.length, inventory.getSize());
        for (int i = 0; i < copySize; i++) {
            inventory.setItem(i, contents[i]);
        }
        update();
    }

    private void registerListener() {
        if (listener == null) {
            listener = new GuiListener();
            Bukkit.getPluginManager().registerEvents(listener, PLUGIN);
        }
    }

    private void startAnimations() {
        if (!animatedItems.isEmpty() && animationTask == null) {
            animationTask = Bukkit.getScheduler().runTaskTimer(PLUGIN, () -> {
                boolean needsUpdate = false;
                for (Map.Entry<Integer, AnimatedItem> entry : animatedItems.entrySet()) {
                    if (entry.getValue().tick()) {
                        inventory.setItem(entry.getKey(), entry.getValue().getCurrentFrame());
                        needsUpdate = true;
                    }
                }
                if (needsUpdate) update();
            }, 1L, 1L);
        }
    }

    private void startAutoUpdate() {
        if (autoUpdate) {
            Bukkit.getScheduler().runTaskTimer(PLUGIN, this::update, updateInterval, updateInterval);
        }
    }

    private void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        int slot = event.getRawSlot();

        // Clicking in player's own inventory (bottom inventory)
        if (slot < 0 || slot >= inventory.getSize()) {
            // Always cancel to prevent shift-click duping exploits
            event.setCancelled(true);
            return;
        }

        GuiClickEvent clickEvent = new GuiClickEvent(
            this, player, slot, event.getClick(), event.getAction(),
            event.getCurrentItem(), event.getCursor()
        );

        // Play click sound
        if (clickSound != null) clickSound.play(player);

        // Call handlers
        if (globalClickHandler != null) globalClickHandler.accept(clickEvent);
        Consumer<GuiClickEvent> handler = clickHandlers.get(slot);
        if (handler != null) handler.accept(clickEvent);

        // Cancel event based on permissions and handlers
        if (clickEvent.isCancelled()) {
            event.setCancelled(true);
        } else {
            // Prevent item manipulation unless explicitly allowed
            boolean isShiftClick = event.isShiftClick();
            boolean isNumberKey = event.getHotbarButton() >= 0;
            
            // Always cancel shift-click and number key presses to prevent duping
            if (isShiftClick || isNumberKey) {
                event.setCancelled(true);
            } else if (!allowTake && !allowPlace) {
                event.setCancelled(true);
            }
        }
    }

    private void handleDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Check if any slots are in our GUI
        boolean affectsGui = event.getRawSlots().stream().anyMatch(slot -> slot < inventory.getSize());
        if (!affectsGui) return;

        if (!allowDrag) {
            event.setCancelled(true);
            return;
        }

        if (dragHandler != null) {
            GuiDragEvent dragEvent = new GuiDragEvent(
                this, player, event.getType(), event.getNewItems(),
                event.getRawSlots(), event.getOldCursor(), event.getCursor()
            );
            dragHandler.accept(dragEvent);
            if (dragEvent.isCancelled()) event.setCancelled(true);
        }
    }

    private void handleOpen(InventoryOpenEvent event) {
        if (openHandler != null && event.getPlayer() instanceof Player player) {
            GuiOpenEvent openEvent = new GuiOpenEvent(this, player);
            openHandler.accept(openEvent);
            if (openEvent.isCancelled()) event.setCancelled(true);
        }
    }

    private void handleClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            if (closeSound != null) closeSound.play(player);
            if (closeHandler != null) {
                GuiCloseEvent closeEvent = new GuiCloseEvent(this, player, GuiCloseEvent.CloseReason.PLAYER);
                closeHandler.accept(closeEvent);
            }
        }
    }

    // ==================== Inner Classes ====================

    private record GuiHolder(IonGui gui) implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            return gui.getInventory();
        }
    }

    private class GuiListener implements Listener {
        @EventHandler(priority = EventPriority.NORMAL)
        public void onInventoryClick(InventoryClickEvent event) {
            if (event.getInventory().getHolder() instanceof GuiHolder holder && holder.gui() == IonGuiBuilder.this) {
                handleClick(event);
            }
        }

        @EventHandler(priority = EventPriority.NORMAL)
        public void onInventoryDrag(InventoryDragEvent event) {
            if (event.getInventory().getHolder() instanceof GuiHolder holder && holder.gui() == IonGuiBuilder.this) {
                handleDrag(event);
            }
        }

        @EventHandler(priority = EventPriority.NORMAL)
        public void onInventoryOpen(InventoryOpenEvent event) {
            if (event.getInventory().getHolder() instanceof GuiHolder holder && holder.gui() == IonGuiBuilder.this) {
                handleOpen(event);
            }
        }

        @EventHandler(priority = EventPriority.NORMAL)
        public void onInventoryClose(InventoryCloseEvent event) {
            if (event.getInventory().getHolder() instanceof GuiHolder holder && holder.gui() == IonGuiBuilder.this) {
                handleClose(event);
            }
        }

        @EventHandler(priority = EventPriority.NORMAL)
        public void onPlayerQuit(PlayerQuitEvent event) {
            Player player = event.getPlayer();
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof GuiHolder holder && holder.gui() == IonGuiBuilder.this) {
                if (closeHandler != null) {
                    closeHandler.accept(new GuiCloseEvent(IonGuiBuilder.this, player, GuiCloseEvent.CloseReason.DISCONNECT));
                }
            }
        }
    }
}
