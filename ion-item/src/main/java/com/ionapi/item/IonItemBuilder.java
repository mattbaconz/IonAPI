package com.ionapi.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Implementation of the IonItem builder interface.
 * Provides a fluent API for creating and modifying ItemStacks.
 */
public class IonItemBuilder implements IonItem.Builder {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final ItemStack itemStack;
    private final ItemMeta itemMeta;

    /**
     * Creates a new item builder for the specified material.
     *
     * @param material the material type
     */
    public IonItemBuilder(@NotNull Material material) {
        this(material, 1);
    }

    /**
     * Creates a new item builder for the specified material with amount.
     *
     * @param material the material type
     * @param amount   the stack size
     */
    public IonItemBuilder(@NotNull Material material, int amount) {
        this.itemStack = new ItemStack(material, amount);
        this.itemMeta = itemStack.getItemMeta();
    }

    /**
     * Creates a new item builder from an existing ItemStack.
     *
     * @param itemStack the item stack to copy
     */
    public IonItemBuilder(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack.clone();
        this.itemMeta = this.itemStack.getItemMeta();
    }

    @Override
    public @NotNull IonItem.Builder name(@NotNull String name) {
        if (itemMeta != null) {
            itemMeta.displayName(MINI_MESSAGE.deserialize(name));
        }
        return this;
    }

    @Override
    public @NotNull IonItem.Builder name(@NotNull Component name) {
        if (itemMeta != null) {
            itemMeta.displayName(name);
        }
        return this;
    }

    @Override
    public @NotNull IonItem.Builder lore(@NotNull String... lore) {
        return lore(Arrays.asList(lore));
    }

    @Override
    public @NotNull IonItem.Builder lore(@NotNull List<String> lore) {
        if (itemMeta != null) {
            List<Component> components = lore.stream()
                    .map(MINI_MESSAGE::deserialize)
                    .collect(Collectors.toList());
            itemMeta.lore(components);
        }
        return this;
    }

    @Override
    public @NotNull IonItem.Builder loreComponents(@NotNull List<Component> lore) {
        if (itemMeta != null) {
            itemMeta.lore(new ArrayList<>(lore));
        }
        return this;
    }

    @Override
    public @NotNull IonItem.Builder addLore(@NotNull String line) {
        if (itemMeta != null) {
            List<Component> lore = itemMeta.lore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            lore.add(MINI_MESSAGE.deserialize(line));
            itemMeta.lore(lore);
        }
        return this;
    }

    @Override
    public @NotNull IonItem.Builder addLore(@NotNull String... lines) {
        if (itemMeta != null) {
            List<Component> lore = itemMeta.lore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            for (String line : lines) {
                lore.add(MINI_MESSAGE.deserialize(line));
            }
            itemMeta.lore(lore);
        }
        return this;
    }

    @Override
    public @NotNull IonItem.Builder amount(int amount) {
        itemStack.setAmount(Math.max(1, Math.min(64, amount)));
        return this;
    }

    @Override
    public @NotNull IonItem.Builder enchant(@NotNull Enchantment enchantment, int level) {
        if (itemMeta != null) {
            itemMeta.addEnchant(enchantment, level, false);
        }
        return this;
    }

    @Override
    public @NotNull IonItem.Builder enchantUnsafe(@NotNull Enchantment enchantment, int level) {
        if (itemMeta != null) {
            itemMeta.addEnchant(enchantment, level, true);
        }
        return this;
    }

    @Override
    public @NotNull IonItem.Builder removeEnchant(@NotNull Enchantment enchantment) {
        if (itemMeta != null) {
            itemMeta.removeEnchant(enchantment);
        }
        return this;
    }

    @Override
    public @NotNull IonItem.Builder clearEnchants() {
        if (itemMeta != null) {
            itemMeta.getEnchants().keySet().forEach(itemMeta::removeEnchant);
        }
        return this;
    }

    @Override
    public @NotNull IonItem.Builder flag(@NotNull ItemFlag flag) {
        if (itemMeta != null) {
            itemMeta.addItemFlags(flag);
        }
        return this;
    }

    @Override
    public @NotNull IonItem.Builder flags(@NotNull ItemFlag... flags) {
        if (itemMeta != null) {
            itemMeta.addItemFlags(flags);
        }
        return this;
    }

    @Override
    public @NotNull IonItem.Builder removeFlag(@NotNull ItemFlag flag) {
        if (itemMeta != null) {
            itemMeta.removeItemFlags(flag);
        }
        return this;
    }

    @Override
    public @NotNull IonItem.Builder unbreakable() {
        return unbreakable(true);
    }

    @Override
    public @NotNull IonItem.Builder unbreakable(boolean unbreakable) {
        if (itemMeta != null) {
            itemMeta.setUnbreakable(unbreakable);
        }
        return this;
    }

    @Override
    public @NotNull IonItem.Builder glow() {
        return glow(true);
    }

    @Override
    public @NotNull IonItem.Builder glow(boolean glow) {
        if (glow) {
            // Add a harmless enchantment and hide it to create glow effect
            enchantUnsafe(Enchantment.LURE, 1);
            flag(ItemFlag.HIDE_ENCHANTS);
        } else {
            removeEnchant(Enchantment.LURE);
            removeFlag(ItemFlag.HIDE_ENCHANTS);
        }
        return this;
    }

    @Override
    public @NotNull IonItem.Builder hideAll() {
        if (itemMeta != null) {
            itemMeta.addItemFlags(ItemFlag.values());
        }
        return this;
    }

    @Override
    public @NotNull IonItem.Builder customModelData(int data) {
        if (itemMeta != null) {
            itemMeta.setCustomModelData(data);
        }
        return this;
    }

    @Override
    public @NotNull IonItem.Builder damage(int damage) {
        if (itemMeta instanceof org.bukkit.inventory.meta.Damageable damageable) {
            damageable.setDamage(Math.max(0, damage));
        }
        return this;
    }

    @Override
    public @NotNull IonItem.Builder material(@NotNull Material material) {
        itemStack.setType(material);
        return this;
    }

    @Override
    public @NotNull IonItem.Builder customData(@NotNull Consumer<ItemStack> modifier) {
        // Apply meta first, then allow custom modifications
        itemStack.setItemMeta(itemMeta);
        modifier.accept(itemStack);
        return this;
    }

    @Override
    public @NotNull IonItem.Builder skullTexture(@NotNull String base64Texture) {
        if (itemMeta instanceof org.bukkit.inventory.meta.SkullMeta skullMeta) {
            try {
                java.util.UUID uuid = java.util.UUID.randomUUID();
                org.bukkit.profile.PlayerProfile profile = org.bukkit.Bukkit.createPlayerProfile(uuid);
                org.bukkit.profile.PlayerTextures textures = profile.getTextures();

                // Decode base64 to get the URL
                String decoded = new String(java.util.Base64.getDecoder().decode(base64Texture));
                // Extract URL from JSON
                String url = decoded.replaceAll(".*\"url\"\\s*:\\s*\"([^\"]+)\".*", "$1");
                if (url.startsWith("http")) {
                    textures.setSkin(java.net.URI.create(url).toURL());
                    profile.setTextures(textures);
                    skullMeta.setOwnerProfile(profile);
                }
            } catch (Exception e) {
                // Silently fail if texture parsing fails
            }
        }
        return this;
    }

    @Override
    public @NotNull IonItem.Builder color(@NotNull org.bukkit.Color color) {
        if (itemMeta instanceof org.bukkit.inventory.meta.LeatherArmorMeta leatherMeta) {
            leatherMeta.setColor(color);
        }
        return this;
    }

    @Override
    public @NotNull IonItem.Builder color(int red, int green, int blue) {
        return color(org.bukkit.Color.fromRGB(red, green, blue));
    }

    @Override
    public @NotNull IonItem.Builder potionEffect(@NotNull org.bukkit.potion.PotionEffect effect) {
        if (itemMeta instanceof org.bukkit.inventory.meta.PotionMeta potionMeta) {
            potionMeta.addCustomEffect(effect, true);
        }
        return this;
    }

    @Override
    public @NotNull IonItem.Builder potionEffect(@NotNull org.bukkit.potion.PotionEffectType type, int durationTicks,
            int amplifier) {
        return potionEffect(new org.bukkit.potion.PotionEffect(type, durationTicks, amplifier));
    }

    @Override
    public @NotNull IonItem.Builder potionType(@NotNull org.bukkit.potion.PotionType type) {
        if (itemMeta instanceof org.bukkit.inventory.meta.PotionMeta potionMeta) {
            potionMeta.setBasePotionType(type);
        }
        return this;
    }

    @Override
    public @NotNull IonItem.Builder potionColor(@NotNull org.bukkit.Color color) {
        if (itemMeta instanceof org.bukkit.inventory.meta.PotionMeta potionMeta) {
            potionMeta.setColor(color);
        }
        return this;
    }

    @Override
    public @NotNull IonItem.Builder clone() {
        return new IonItemBuilder(build());
    }

    @Override
    public @NotNull ItemStack build() {
        if (itemMeta != null) {
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

    @Override
    public @Nullable ItemMeta buildMeta() {
        return itemMeta;
    }
}
