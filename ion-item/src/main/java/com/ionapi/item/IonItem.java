package com.ionapi.item;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * Fluent builder for creating ItemStacks with a clean, readable API.
 * <p>
 * Example usage:
 * 
 * <pre>{@code
 * ItemStack sword = IonItem.builder(Material.DIAMOND_SWORD)
 *         .name("<gradient:red:blue>Legendary Sword")
 *         .lore("Deals massive damage", "", "§7Rarity: §6Legendary")
 *         .enchant(Enchantment.SHARPNESS, 5)
 *         .unbreakable()
 *         .glow()
 *         .build();
 * }</pre>
 */
public interface IonItem {

    /**
     * Creates a new item builder for the specified material.
     *
     * @param material the material type
     * @return a new item builder
     */
    @NotNull
    static IonItemBuilder builder(@NotNull Material material) {
        return new IonItemBuilder(material);
    }

    /**
     * Creates a new item builder from an existing ItemStack.
     *
     * @param itemStack the item stack to copy
     * @return a new item builder
     */
    @NotNull
    static IonItemBuilder builder(@NotNull ItemStack itemStack) {
        return new IonItemBuilder(itemStack);
    }

    /**
     * Creates a new item builder for the specified material with amount.
     *
     * @param material the material type
     * @param amount   the stack size
     * @return a new item builder
     */
    @NotNull
    static IonItemBuilder builder(@NotNull Material material, int amount) {
        return new IonItemBuilder(material, amount);
    }

    /**
     * Quick method to create a simple named item.
     *
     * @param material the material type
     * @param name     the display name
     * @return the built item stack
     */
    @NotNull
    static ItemStack of(@NotNull Material material, @NotNull String name) {
        return builder(material).name(name).build();
    }

    /**
     * Quick method to create a simple named item with lore.
     *
     * @param material the material type
     * @param name     the display name
     * @param lore     the lore lines
     * @return the built item stack
     */
    @NotNull
    static ItemStack of(@NotNull Material material, @NotNull String name, @NotNull String... lore) {
        return builder(material).name(name).lore(lore).build();
    }

    /**
     * Modifies an existing ItemStack using a builder pattern.
     *
     * @param itemStack the item stack to modify
     * @param modifier  the modification function
     * @return the modified item stack
     */
    @NotNull
    static ItemStack modify(@NotNull ItemStack itemStack, @NotNull Consumer<IonItemBuilder> modifier) {
        IonItemBuilder builder = builder(itemStack);
        modifier.accept(builder);
        return builder.build();
    }

    /**
     * Builder interface for creating ItemStacks.
     */
    interface Builder {

        /**
         * Sets the display name using MiniMessage format.
         *
         * @param name the display name (supports MiniMessage tags)
         * @return this builder
         */
        @NotNull
        Builder name(@NotNull String name);

        /**
         * Sets the display name using Adventure Component.
         *
         * @param name the display name component
         * @return this builder
         */
        @NotNull
        Builder name(@NotNull Component name);

        /**
         * Sets the lore lines using MiniMessage format.
         *
         * @param lore the lore lines (supports MiniMessage tags)
         * @return this builder
         */
        @NotNull
        Builder lore(@NotNull String... lore);

        /**
         * Sets the lore lines using MiniMessage format.
         *
         * @param lore the lore lines (supports MiniMessage tags)
         * @return this builder
         */
        @NotNull
        Builder lore(@NotNull List<String> lore);

        /**
         * Sets the lore lines using Adventure Components.
         *
         * @param lore the lore component lines
         * @return this builder
         */
        @NotNull
        Builder loreComponents(@NotNull List<Component> lore);

        /**
         * Adds a single lore line.
         *
         * @param line the lore line to add
         * @return this builder
         */
        @NotNull
        Builder addLore(@NotNull String line);

        /**
         * Adds multiple lore lines.
         *
         * @param lines the lore lines to add
         * @return this builder
         */
        @NotNull
        Builder addLore(@NotNull String... lines);

        /**
         * Sets the stack amount.
         *
         * @param amount the stack size (1-64)
         * @return this builder
         */
        @NotNull
        Builder amount(int amount);

        /**
         * Adds an enchantment to the item.
         *
         * @param enchantment the enchantment type
         * @param level       the enchantment level
         * @return this builder
         */
        @NotNull
        Builder enchant(@NotNull Enchantment enchantment, int level);

        /**
         * Adds an enchantment to the item, ignoring level restrictions.
         *
         * @param enchantment the enchantment type
         * @param level       the enchantment level
         * @return this builder
         */
        @NotNull
        Builder enchantUnsafe(@NotNull Enchantment enchantment, int level);

        /**
         * Removes an enchantment from the item.
         *
         * @param enchantment the enchantment to remove
         * @return this builder
         */
        @NotNull
        Builder removeEnchant(@NotNull Enchantment enchantment);

        /**
         * Clears all enchantments from the item.
         *
         * @return this builder
         */
        @NotNull
        Builder clearEnchants();

        /**
         * Adds an item flag.
         *
         * @param flag the item flag to add
         * @return this builder
         */
        @NotNull
        Builder flag(@NotNull ItemFlag flag);

        /**
         * Adds multiple item flags.
         *
         * @param flags the item flags to add
         * @return this builder
         */
        @NotNull
        Builder flags(@NotNull ItemFlag... flags);

        /**
         * Removes an item flag.
         *
         * @param flag the item flag to remove
         * @return this builder
         */
        @NotNull
        Builder removeFlag(@NotNull ItemFlag flag);

        /**
         * Makes the item unbreakable.
         *
         * @return this builder
         */
        @NotNull
        Builder unbreakable();

        /**
         * Sets whether the item is unbreakable.
         *
         * @param unbreakable true if unbreakable
         * @return this builder
         */
        @NotNull
        Builder unbreakable(boolean unbreakable);

        /**
         * Makes the item glow (adds enchantment glint without enchantments).
         *
         * @return this builder
         */
        @NotNull
        Builder glow();

        /**
         * Sets whether the item should glow.
         *
         * @param glow true to add glow effect
         * @return this builder
         */
        @NotNull
        Builder glow(boolean glow);

        /**
         * Hides all item attributes (enchantments, unbreakable, etc).
         *
         * @return this builder
         */
        @NotNull
        Builder hideAll();

        /**
         * Sets custom model data for resource packs.
         *
         * @param data the custom model data value
         * @return this builder
         */
        @NotNull
        Builder customModelData(int data);

        /**
         * Sets the item's durability/damage.
         *
         * @param damage the damage value
         * @return this builder
         */
        @NotNull
        Builder damage(int damage);

        /**
         * Sets the item's material type.
         *
         * @param material the new material
         * @return this builder
         */
        @NotNull
        Builder material(@NotNull Material material);

        /**
         * Applies custom NBT data to the item.
         *
         * @param modifier consumer to modify the item's PDC
         * @return this builder
         */
        @NotNull
        Builder customData(@NotNull Consumer<ItemStack> modifier);

        /**
         * Sets a custom skull texture using base64 encoded texture data.
         * Only works for PLAYER_HEAD items.
         *
         * @param base64Texture the base64 encoded texture value
         * @return this builder
         * @since 1.3.0
         */
        @NotNull
        Builder skullTexture(@NotNull String base64Texture);

        /**
         * Sets the color of a leather armor piece.
         * Only works for leather armor items.
         *
         * @param color the color to apply
         * @return this builder
         * @since 1.3.0
         */
        @NotNull
        Builder color(@NotNull org.bukkit.Color color);

        /**
         * Sets the color of a leather armor piece using RGB values.
         * Only works for leather armor items.
         *
         * @param red   the red component (0-255)
         * @param green the green component (0-255)
         * @param blue  the blue component (0-255)
         * @return this builder
         * @since 1.3.0
         */
        @NotNull
        Builder color(int red, int green, int blue);

        /**
         * Adds a potion effect to the item.
         * Only works for POTION, SPLASH_POTION, LINGERING_POTION, and TIPPED_ARROW.
         *
         * @param effect the potion effect to add
         * @return this builder
         * @since 1.3.0
         */
        @NotNull
        Builder potionEffect(@NotNull org.bukkit.potion.PotionEffect effect);

        /**
         * Adds a potion effect to the item.
         * Only works for POTION, SPLASH_POTION, LINGERING_POTION, and TIPPED_ARROW.
         *
         * @param type          the potion effect type
         * @param durationTicks the duration in ticks
         * @param amplifier     the effect amplifier (0 = level 1)
         * @return this builder
         * @since 1.3.0
         */
        @NotNull
        Builder potionEffect(@NotNull org.bukkit.potion.PotionEffectType type, int durationTicks, int amplifier);

        /**
         * Sets the main potion type.
         * Only works for POTION, SPLASH_POTION, LINGERING_POTION, and TIPPED_ARROW.
         *
         * @param type the potion type
         * @return this builder
         * @since 1.3.0
         */
        @NotNull
        Builder potionType(@NotNull org.bukkit.potion.PotionType type);

        /**
         * Sets the potion color.
         * Only works for POTION, SPLASH_POTION, LINGERING_POTION, and TIPPED_ARROW.
         *
         * @param color the color
         * @return this builder
         * @since 1.3.0
         */
        @NotNull
        Builder potionColor(@NotNull org.bukkit.Color color);

        /**
         * Creates a clone of this builder.
         *
         * @return a new builder with the same properties
         */
        @NotNull
        Builder clone();

        /**
         * Builds the final ItemStack.
         *
         * @return the constructed item stack
         */
        @NotNull
        ItemStack build();

        /**
         * Builds and returns the ItemStack's meta for inspection.
         *
         * @return the item meta
         */
        @Nullable
        org.bukkit.inventory.meta.ItemMeta buildMeta();
    }
}
