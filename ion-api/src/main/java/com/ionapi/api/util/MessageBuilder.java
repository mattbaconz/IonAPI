package com.ionapi.api.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fluent message builder with MiniMessage support and templates.
 *
 * <p>Example usage:
 * <pre>{@code
 * // Simple message
 * MessageBuilder.of("<green>Hello, <player>!")
 *     .placeholder("player", player.getName())
 *     .send(player);
 *
 * // Title
 * MessageBuilder.of("<gold><bold>LEVEL UP!")
 *     .subtitle("<gray>You are now level <level>")
 *     .placeholder("level", "10")
 *     .sendTitle(player);
 *
 * // Action bar
 * MessageBuilder.of("<red>❤ <health>/<max_health>")
 *     .placeholder("health", "15")
 *     .placeholder("max_health", "20")
 *     .sendActionBar(player);
 * }</pre>
 *
 * @since 1.2.0
 */
public final class MessageBuilder {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final Map<String, String> TEMPLATES = new HashMap<>();

    private final String message;
    private String subtitle;
    private final Map<String, String> placeholders = new HashMap<>();
    private Duration fadeIn = Duration.ofMillis(500);
    private Duration stay = Duration.ofSeconds(3);
    private Duration fadeOut = Duration.ofMillis(500);

    private MessageBuilder(String message) {
        this.message = message;
    }

    /**
     * Creates a new message builder.
     *
     * @param message the message (supports MiniMessage)
     * @return a new builder
     */
    @NotNull
    public static MessageBuilder of(@NotNull String message) {
        return new MessageBuilder(message);
    }

    /**
     * Creates a message builder from a registered template.
     *
     * @param templateName the template name
     * @return a new builder
     */
    @NotNull
    public static MessageBuilder template(@NotNull String templateName) {
        String template = TEMPLATES.getOrDefault(templateName, templateName);
        return new MessageBuilder(template);
    }

    /**
     * Registers a reusable message template.
     *
     * @param name the template name
     * @param message the message template
     */
    public static void registerTemplate(@NotNull String name, @NotNull String message) {
        TEMPLATES.put(name, message);
    }

    /**
     * Adds a placeholder replacement.
     *
     * @param key the placeholder key (without angle brackets)
     * @param value the replacement value
     * @return this builder
     */
    @NotNull
    public MessageBuilder placeholder(@NotNull String key, @NotNull String value) {
        placeholders.put(key, value);
        return this;
    }

    /**
     * Adds a placeholder replacement with a number.
     *
     * @param key the placeholder key
     * @param value the number value
     * @return this builder
     */
    @NotNull
    public MessageBuilder placeholder(@NotNull String key, int value) {
        return placeholder(key, String.valueOf(value));
    }

    /**
     * Adds a placeholder replacement with a number.
     *
     * @param key the placeholder key
     * @param value the number value
     * @return this builder
     */
    @NotNull
    public MessageBuilder placeholder(@NotNull String key, double value) {
        return placeholder(key, String.format("%.2f", value));
    }

    /**
     * Sets the subtitle (for title messages).
     *
     * @param subtitle the subtitle
     * @return this builder
     */
    @NotNull
    public MessageBuilder subtitle(@NotNull String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    /**
     * Sets title timing.
     *
     * @param fadeIn fade in duration
     * @param stay stay duration
     * @param fadeOut fade out duration
     * @return this builder
     */
    @NotNull
    public MessageBuilder timing(@NotNull Duration fadeIn, @NotNull Duration stay, @NotNull Duration fadeOut) {
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
        return this;
    }

    /**
     * Builds the component.
     *
     * @return the built component
     */
    @NotNull
    public Component build() {
        return MINI.deserialize(message, buildResolvers());
    }

    /**
     * Sends the message to a player.
     *
     * @param player the player
     */
    public void send(@NotNull Player player) {
        player.sendMessage(build());
    }

    /**
     * Sends the message to a command sender.
     *
     * @param sender the sender
     */
    public void send(@NotNull CommandSender sender) {
        if (sender instanceof Audience audience) {
            audience.sendMessage(build());
        }
    }

    /**
     * Sends the message to multiple players.
     *
     * @param players the players
     */
    public void send(@NotNull Iterable<? extends Player> players) {
        Component component = build();
        for (Player player : players) {
            player.sendMessage(component);
        }
    }

    /**
     * Sends as a title.
     *
     * @param player the player
     */
    public void sendTitle(@NotNull Player player) {
        Component titleComponent = build();
        Component subtitleComponent = subtitle != null 
            ? MINI.deserialize(subtitle, buildResolvers()) 
            : Component.empty();
        
        Title.Times times = Title.Times.times(fadeIn, stay, fadeOut);
        Title title = Title.title(titleComponent, subtitleComponent, times);
        player.showTitle(title);
    }

    /**
     * Sends as an action bar.
     *
     * @param player the player
     */
    public void sendActionBar(@NotNull Player player) {
        player.sendActionBar(build());
    }

    /**
     * Broadcasts to all online players.
     */
    public void broadcast() {
        Component component = build();
        for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            player.sendMessage(component);
        }
    }

    private TagResolver buildResolvers() {
        List<TagResolver> resolvers = new ArrayList<>();
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            resolvers.add(Placeholder.parsed(entry.getKey(), entry.getValue()));
        }
        return TagResolver.resolver(resolvers);
    }
}
