package examples;

import com.ionapi.redis.IonRedis;
import com.ionapi.redis.IonRedisBuilder;
import com.ionapi.redis.RedisMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Example showing Redis pub/sub for cross-server communication.
 * Perfect for multi-server networks!
 */
public class RedisExample extends JavaPlugin implements Listener {

    private IonRedis redis;

    @Override
    public void onEnable() {
        // Connect to Redis
        redis = IonRedisBuilder.create()
            .host("localhost")
            .port(6379)
            .password("your-password") // Optional
            .database(0)
            .timeout(5000)
            .build();

        // Subscribe to channels
        setupSubscriptions();

        // Register events
        Bukkit.getPluginManager().registerEvents(this, this);

        getLogger().info("Redis example enabled!");
    }

    @Override
    public void onDisable() {
        if (redis != null) {
            redis.close();
        }
    }

    private void setupSubscriptions() {
        // Global announcements
        redis.subscribe("global-announcements", this::handleAnnouncement);

        // Player join notifications
        redis.subscribe("player-join", this::handlePlayerJoin);

        // Server status updates
        redis.subscribe("server-status", this::handleServerStatus);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Publish join event to all servers
        redis.publish("player-join", player.getName());

        // Store player data in Redis with 1 hour TTL
        String key = "player:" + player.getUniqueId();
        String data = player.getName() + ":" + System.currentTimeMillis();
        redis.set(key, data, 3600);
    }

    /**
     * Example: Global announcement across all servers
     */
    public void broadcastGlobal(String message) {
        redis.publish("global-announcements", message);
    }

    /**
     * Example: Check if player is online on any server
     */
    public void checkPlayerOnline(String playerName) {
        redis.get("player:" + playerName).thenAccept(data -> {
            if (data != null) {
                getLogger().info(playerName + " is online on the network!");
            } else {
                getLogger().info(playerName + " is offline.");
            }
        });
    }

    /**
     * Example: Cross-server player count
     */
    public void updatePlayerCount() {
        String serverName = "lobby-1"; // Your server name
        int count = Bukkit.getOnlinePlayers().size();
        
        redis.set("server:" + serverName + ":players", String.valueOf(count), 60);
    }

    /**
     * Example: Get total network player count
     */
    public void getTotalPlayers() {
        // In a real implementation, you'd query all server keys
        redis.get("server:lobby-1:players").thenAccept(count1 -> {
            redis.get("server:lobby-2:players").thenAccept(count2 -> {
                int total = Integer.parseInt(count1 != null ? count1 : "0") +
                           Integer.parseInt(count2 != null ? count2 : "0");
                getLogger().info("Total network players: " + total);
            });
        });
    }

    private void handleAnnouncement(RedisMessage message) {
        String announcement = message.data();
        Bukkit.broadcastMessage("§6[Network] §f" + announcement);
    }

    private void handlePlayerJoin(RedisMessage message) {
        String playerName = message.data();
        getLogger().info(playerName + " joined the network!");
    }

    private void handleServerStatus(RedisMessage message) {
        String status = message.data();
        getLogger().info("Server status update: " + status);
    }

    /**
     * Example: Redis health check
     */
    public void checkRedisHealth() {
        if (redis.isConnected()) {
            var stats = redis.getStats();
            getLogger().info("Redis Stats:");
            getLogger().info("  Published: " + stats.messagesPublished());
            getLogger().info("  Received: " + stats.messagesReceived());
            getLogger().info("  Subscriptions: " + stats.activeSubscriptions());
            getLogger().info("  Uptime: " + (stats.connectionUptime() / 1000) + "s");
        } else {
            getLogger().warning("Redis is disconnected!");
        }
    }
}
