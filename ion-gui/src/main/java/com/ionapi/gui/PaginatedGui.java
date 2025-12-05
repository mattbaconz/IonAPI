package com.ionapi.gui;

import com.ionapi.item.IonItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A paginated GUI that supports multiple pages of items.
 * 
 * Example:
 * <pre>{@code
 * PaginatedGui gui = PaginatedGui.create()
 *     .title("<gold>Shop - Page {page}/{pages}")
 *     .rows(6)
 *     .items(shopItems)
 *     .itemSlots(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25)
 *     .previousButton(48, prevItem)
 *     .nextButton(50, nextItem)
 *     .onItemClick((item, click) -> buyItem(click.getPlayer(), item))
 *     .build();
 * gui.open(player);
 * }</pre>
 */
public class PaginatedGui {

    private final String titleTemplate;
    private final int rows;
    private final List<ItemStack> items;
    private final int[] itemSlots;
    private final int prevSlot;
    private final int nextSlot;
    private final ItemStack prevButton;
    private final ItemStack nextButton;
    private final ItemStack prevButtonDisabled;
    private final ItemStack nextButtonDisabled;
    private final ItemStack filler;
    private final BiConsumer<ItemStack, GuiClickEvent> onItemClick;
    private final Consumer<GuiOpenEvent> onOpen;
    private final Consumer<GuiCloseEvent> onClose;
    private final GuiSound clickSound;
    private final GuiSound pageSound;

    private final List<IonGui> pages = new ArrayList<>();
    private int currentPage = 0;

    private PaginatedGui(Builder builder) {
        this.titleTemplate = builder.titleTemplate;
        this.rows = builder.rows;
        this.items = new ArrayList<>(builder.items);
        this.itemSlots = builder.itemSlots;
        this.prevSlot = builder.prevSlot;
        this.nextSlot = builder.nextSlot;
        this.prevButton = builder.prevButton;
        this.nextButton = builder.nextButton;
        this.prevButtonDisabled = builder.prevButtonDisabled;
        this.nextButtonDisabled = builder.nextButtonDisabled;
        this.filler = builder.filler;
        this.onItemClick = builder.onItemClick;
        this.onOpen = builder.onOpen;
        this.onClose = builder.onClose;
        this.clickSound = builder.clickSound;
        this.pageSound = builder.pageSound;
        
        buildPages();
    }

    public static Builder create() {
        return new Builder();
    }

    private void buildPages() {
        pages.clear();
        int itemsPerPage = itemSlots.length;
        int totalPages = Math.max(1, (int) Math.ceil((double) items.size() / itemsPerPage));

        for (int pageNum = 0; pageNum < totalPages; pageNum++) {
            final int page = pageNum;
            String title = titleTemplate
                .replace("{page}", String.valueOf(page + 1))
                .replace("{pages}", String.valueOf(totalPages));

            IonGuiBuilder guiBuilder = IonGui.builder()
                .title(title)
                .rows(rows);

            if (clickSound != null) {
                guiBuilder.clickSound(clickSound);
            }

            IonGui gui = guiBuilder.build();

            // Fill background
            if (filler != null) {
                gui.fill(filler);
            }

            // Add items for this page
            int startIndex = page * itemsPerPage;
            for (int i = 0; i < itemSlots.length; i++) {
                int itemIndex = startIndex + i;
                if (itemIndex < items.size()) {
                    ItemStack item = items.get(itemIndex);
                    final int idx = itemIndex;
                    gui.setItem(itemSlots[i], item, click -> {
                        if (onItemClick != null) {
                            onItemClick.accept(items.get(idx), click);
                        }
                    });
                }
            }

            // Previous button
            if (prevSlot >= 0) {
                if (page > 0) {
                    gui.setItem(prevSlot, prevButton, click -> {
                        if (pageSound != null) pageSound.play(click.getPlayer());
                        openPage(click.getPlayer(), page - 1);
                    });
                } else if (prevButtonDisabled != null) {
                    gui.setItem(prevSlot, prevButtonDisabled);
                }
            }

            // Next button
            if (nextSlot >= 0) {
                if (page < totalPages - 1) {
                    gui.setItem(nextSlot, nextButton, click -> {
                        if (pageSound != null) pageSound.play(click.getPlayer());
                        openPage(click.getPlayer(), page + 1);
                    });
                } else if (nextButtonDisabled != null) {
                    gui.setItem(nextSlot, nextButtonDisabled);
                }
            }

            // Events
            if (onOpen != null) gui.onOpen(onOpen);
            if (onClose != null) gui.onClose(onClose);

            pages.add(gui);
        }
    }

    public void open(@NotNull Player player) {
        openPage(player, 0);
    }

    public void openPage(@NotNull Player player, int page) {
        if (page >= 0 && page < pages.size()) {
            currentPage = page;
            pages.get(page).open(player);
        }
    }

    public void refresh() {
        buildPages();
    }

    public void setItems(@NotNull List<ItemStack> newItems) {
        items.clear();
        items.addAll(newItems);
        buildPages();
    }

    public void addItem(@NotNull ItemStack item) {
        items.add(item);
        buildPages();
    }

    public void removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            buildPages();
        }
    }

    public int getCurrentPage() { return currentPage; }
    public int getTotalPages() { return pages.size(); }
    public int getItemCount() { return items.size(); }

    public void destroy() {
        pages.forEach(IonGui::destroy);
        pages.clear();
    }

    public static class Builder {
        private String titleTemplate = "Page {page}/{pages}";
        private int rows = 6;
        private List<ItemStack> items = new ArrayList<>();
        private int[] itemSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        private int prevSlot = 48;
        private int nextSlot = 50;
        private ItemStack prevButton;
        private ItemStack nextButton;
        private ItemStack prevButtonDisabled;
        private ItemStack nextButtonDisabled;
        private ItemStack filler;
        private BiConsumer<ItemStack, GuiClickEvent> onItemClick;
        private Consumer<GuiOpenEvent> onOpen;
        private Consumer<GuiCloseEvent> onClose;
        private GuiSound clickSound = GuiSound.CLICK;
        private GuiSound pageSound = GuiSound.PAGE_TURN;

        public Builder() {
            // Default navigation buttons
            prevButton = IonItem.builder(Material.ARROW)
                .name("<yellow>← Previous Page")
                .build();
            nextButton = IonItem.builder(Material.ARROW)
                .name("<yellow>Next Page →")
                .build();
            prevButtonDisabled = IonItem.builder(Material.GRAY_DYE)
                .name("<gray>← Previous Page")
                .build();
            nextButtonDisabled = IonItem.builder(Material.GRAY_DYE)
                .name("<gray>Next Page →")
                .build();
        }

        public Builder title(@NotNull String template) {
            this.titleTemplate = template;
            return this;
        }

        public Builder rows(int rows) {
            this.rows = Math.max(1, Math.min(6, rows));
            return this;
        }

        public Builder items(@NotNull List<ItemStack> items) {
            this.items = new ArrayList<>(items);
            return this;
        }

        public Builder items(@NotNull ItemStack... items) {
            this.items = new ArrayList<>(List.of(items));
            return this;
        }

        public Builder itemSlots(int... slots) {
            this.itemSlots = slots;
            return this;
        }

        public Builder previousButton(int slot, @NotNull ItemStack button) {
            this.prevSlot = slot;
            this.prevButton = button;
            return this;
        }

        public Builder previousButtonDisabled(@NotNull ItemStack button) {
            this.prevButtonDisabled = button;
            return this;
        }

        public Builder nextButton(int slot, @NotNull ItemStack button) {
            this.nextSlot = slot;
            this.nextButton = button;
            return this;
        }

        public Builder nextButtonDisabled(@NotNull ItemStack button) {
            this.nextButtonDisabled = button;
            return this;
        }

        public Builder navigationButtons(int prevSlot, int nextSlot) {
            this.prevSlot = prevSlot;
            this.nextSlot = nextSlot;
            return this;
        }

        public Builder filler(@Nullable ItemStack filler) {
            this.filler = filler;
            return this;
        }

        public Builder onItemClick(@NotNull BiConsumer<ItemStack, GuiClickEvent> handler) {
            this.onItemClick = handler;
            return this;
        }

        public Builder onOpen(@NotNull Consumer<GuiOpenEvent> handler) {
            this.onOpen = handler;
            return this;
        }

        public Builder onClose(@NotNull Consumer<GuiCloseEvent> handler) {
            this.onClose = handler;
            return this;
        }

        public Builder clickSound(@Nullable GuiSound sound) {
            this.clickSound = sound;
            return this;
        }

        public Builder pageSound(@Nullable GuiSound sound) {
            this.pageSound = sound;
            return this;
        }

        public Builder noSounds() {
            this.clickSound = null;
            this.pageSound = null;
            return this;
        }

        public PaginatedGui build() {
            return new PaginatedGui(this);
        }
    }
}
