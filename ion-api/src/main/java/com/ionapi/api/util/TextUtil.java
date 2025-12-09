package com.ionapi.api.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for text and chat formatting using Adventure API.
 */
public final class TextUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private TextUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Parses a MiniMessage string into a Component.
     *
     * @param message the message string with MiniMessage tags
     * @return the parsed component
     */
    @NotNull
    public static Component parse(@NotNull String message) {
        return MINI_MESSAGE.deserialize(message);
    }

    /**
     * Creates a colored component from legacy color codes.
     *
     * @param message the message with color codes (e.g., "&amp;aHello")
     * @return the colored component
     */
    @NotNull
    public static Component legacyColor(@NotNull String message) {
        return Component.text(message.replace('&', '§'));
    }

    /**
     * Creates a simple colored text component.
     *
     * @param message the message
     * @param color   the color
     * @return the colored component
     */
    @NotNull
    public static Component colored(@NotNull String message, @NotNull NamedTextColor color) {
        return Component.text(message, color);
    }

    /**
     * Creates a bold component.
     *
     * @param message the message
     * @return the bold component
     */
    @NotNull
    public static Component bold(@NotNull String message) {
        return Component.text(message).decorate(TextDecoration.BOLD);
    }

    /**
     * Creates an italic component.
     *
     * @param message the message
     * @return the italic component
     */
    @NotNull
    public static Component italic(@NotNull String message) {
        return Component.text(message).decorate(TextDecoration.ITALIC);
    }

    /**
     * Creates an underlined component.
     *
     * @param message the message
     * @return the underlined component
     */
    @NotNull
    public static Component underlined(@NotNull String message) {
        return Component.text(message).decorate(TextDecoration.UNDERLINED);
    }

    /**
     * Creates a strikethrough component.
     *
     * @param message the message
     * @return the strikethrough component
     */
    @NotNull
    public static Component strikethrough(@NotNull String message) {
        return Component.text(message).decorate(TextDecoration.STRIKETHROUGH);
    }

    /**
     * Strips all color codes from a string.
     *
     * @param message the message
     * @return the message without color codes
     */
    @NotNull
    public static String stripColor(@NotNull String message) {
        return message.replaceAll("§[0-9a-fk-or]", "").replaceAll("&[0-9a-fk-or]", "");
    }
}
