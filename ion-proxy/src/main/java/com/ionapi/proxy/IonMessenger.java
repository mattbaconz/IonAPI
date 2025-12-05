package com.ionapi.proxy;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Cross-server messaging interface.
 * Supports both Plugin Messaging channels and Redis pub/sub.
 * 
 * <p>Example usage:
 * <pre>{@code
 * IonMessenger messenger = IonProxy.messenger(plugin);
 * 
 * // Subscribe to a channel
 * messenger.subscribe("my:channel", (player, message) -> {
 *     player.sendMessage("Received: " + message);
 * });
 * 
 * // Send to a specific player's server
 * messenger.send(player, "my:channel", "Hello from another server!");
 * 
 * // Broadcast to all servers
 * messenger.broadcast("my:channel", "Server restarting in 5 minutes!");
 * }</pre>
 */
public interface IonMessenger {

    /**
     * Subscribes to a messaging channel.
     *
     * @param channel the channel name
     * @param handler handler receiving (player, message) - player may be null for broadcasts
     * @return this messenger for chaining
     */
    @NotNull
    IonMessenger subscribe(@NotNull String channel, @NotNull BiConsumer<@Nullable Player, String> handler);

    /**
     * Subscribes to a messaging channel (message only).
     *
     * @param channel the channel name
     * @param handler handler receiving the message
     * @return this messenger for chaining
     */
    @NotNull
    default IonMessenger subscribe(@NotNull String channel, @NotNull Consumer<String> handler) {
        return subscribe(channel, (player, message) -> handler.accept(message));
    }

    /**
     * Unsubscribes from a messaging channel.
     *
     * @param channel the channel name
     * @return this messenger for chaining
     */
    @NotNull
    IonMessenger unsubscribe(@NotNull String channel);

    /**
     * Sends a message through a specific player's connection.
     *
     * @param player the player to send through
     * @param channel the channel name
     * @param message the message to send
     */
    void send(@NotNull Player player, @NotNull String channel, @NotNull String message);

    /**
     * Sends a message to a specific server.
     *
     * @param server the target server name
     * @param channel the channel name
     * @param message the message to send
     */
    void sendToServer(@NotNull String server, @NotNull String channel, @NotNull String message);

    /**
     * Broadcasts a message to all servers.
     *
     * @param channel the channel name
     * @param message the message to broadcast
     */
    void broadcast(@NotNull String channel, @NotNull String message);

    /**
     * Broadcasts a simple message to all servers on the default channel.
     *
     * @param message the message to broadcast
     */
    default void broadcast(@NotNull String message) {
        broadcast("ion:broadcast", message);
    }

    /**
     * Sends raw bytes through a player's connection.
     *
     * @param player the player to send through
     * @param channel the channel name
     * @param data the raw data
     */
    void sendRaw(@NotNull Player player, @NotNull String channel, byte[] data);

    /**
     * Subscribes to raw byte messages.
     *
     * @param channel the channel name
     * @param handler handler receiving (player, data)
     * @return this messenger for chaining
     */
    @NotNull
    IonMessenger subscribeRaw(@NotNull String channel, @NotNull BiConsumer<@Nullable Player, byte[]> handler);

    /**
     * Checks if this messenger is connected and operational.
     *
     * @return true if connected
     */
    boolean isConnected();

    /**
     * Closes this messenger and releases resources.
     */
    void close();
}
