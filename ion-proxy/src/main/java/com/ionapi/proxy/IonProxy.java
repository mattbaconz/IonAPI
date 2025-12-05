package com.ionapi.proxy;

import com.ionapi.proxy.impl.PluginMessageMessenger;
import com.ionapi.proxy.impl.RedisMessenger;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Factory for creating cross-server messengers.
 * 
 * <p>Example usage:
 * <pre>{@code
 * // Plugin messaging (Velocity/BungeeCord)
 * IonMessenger messenger = IonProxy.pluginMessage(plugin);
 * 
 * // Redis pub/sub (requires Jedis)
 * IonMessenger redis = IonProxy.redis(plugin, "localhost", 6379);
 * 
 * // Subscribe and broadcast
 * messenger.subscribe("alerts", msg -> Bukkit.broadcast(Component.text(msg)));
 * messenger.broadcast("alerts", "Server restarting!");
 * }</pre>
 */
public final class IonProxy {

    private IonProxy() {}

    /**
     * Creates a Plugin Messaging based messenger.
     * Works with Velocity and BungeeCord proxies.
     *
     * @param plugin the owning plugin
     * @return a new plugin message messenger
     */
    @NotNull
    public static IonMessenger pluginMessage(@NotNull Plugin plugin) {
        return new PluginMessageMessenger(plugin);
    }

    /**
     * Alias for {@link #pluginMessage(Plugin)}.
     *
     * @param plugin the owning plugin
     * @return a new plugin message messenger
     */
    @NotNull
    public static IonMessenger messenger(@NotNull Plugin plugin) {
        return pluginMessage(plugin);
    }

    /**
     * Creates a Redis pub/sub based messenger.
     * Requires Jedis library to be available.
     *
     * @param plugin the owning plugin
     * @param host Redis host
     * @param port Redis port
     * @return a new Redis messenger
     * @throws IllegalStateException if Jedis is not available
     */
    @NotNull
    public static IonMessenger redis(@NotNull Plugin plugin, @NotNull String host, int port) {
        return new RedisMessenger(plugin, host, port, null);
    }

    /**
     * Creates a Redis pub/sub based messenger with authentication.
     *
     * @param plugin the owning plugin
     * @param host Redis host
     * @param port Redis port
     * @param password Redis password (null for no auth)
     * @return a new Redis messenger
     * @throws IllegalStateException if Jedis is not available
     */
    @NotNull
    public static IonMessenger redis(@NotNull Plugin plugin, @NotNull String host, int port, String password) {
        return new RedisMessenger(plugin, host, port, password);
    }
}
