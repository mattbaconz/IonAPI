package examples;

import com.ionapi.api.IonPlugin;
import com.ionapi.api.util.*;
import com.ionapi.database.BatchOperation;
import com.ionapi.database.IonDatabase;
import com.ionapi.database.annotations.*;
import com.ionapi.ui.IonBossBar;
import com.ionapi.ui.IonScoreboard;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Examples of new features in IonAPI v1.2.0
 */
public class V120FeaturesExample {

    private IonPlugin plugin;
    private IonDatabase database;

    // ==================== Cooldown Manager ====================
    
    public void cooldownExample(Player player) {
        // Create a cooldown manager for teleportation
        CooldownManager teleportCooldown = CooldownManager.create("teleport");
        
        // Check if player is on cooldown
        if (teleportCooldown.isOnCooldown(player.getUniqueId())) {
            long remaining = teleportCooldown.getRemainingTime(player.getUniqueId(), TimeUnit.SECONDS);
            player.sendMessage("Wait " + remaining + "s before teleporting again!");
            return;
        }
        
        // Perform teleport...
        // player.teleport(destination);
        
        // Set 30 second cooldown
        teleportCooldown.setCooldown(player.getUniqueId(), 30, TimeUnit.SECONDS);
    }

    // ==================== Rate Limiter ====================
    
    public void rateLimiterExample(Player player, String message) {
        // Allow 5 messages per 10 seconds
        RateLimiter chatLimiter = RateLimiter.create("chat", 5, 10, TimeUnit.SECONDS);
        
        if (!chatLimiter.tryAcquire(player.getUniqueId())) {
            int remaining = chatLimiter.getRemainingPermits(player.getUniqueId());
            player.sendMessage("Slow down! You can send " + remaining + " more messages.");
            return;
        }
        
        // Process message...
        Bukkit.broadcastMessage(player.getName() + ": " + message);
    }

    // ==================== Message Builder ====================
    
    public void messageBuilderExample(Player player) {
        // Simple message with placeholders
        MessageBuilder.of("<green>Welcome back, <player>!")
            .placeholder("player", player.getName())
            .send(player);
        
        // Title with subtitle
        MessageBuilder.of("<gold><bold>LEVEL UP!")
            .subtitle("<gray>You are now level <level>")
            .placeholder("level", "10")
            .sendTitle(player);
        
        // Action bar
        MessageBuilder.of("<red>❤ <health>/<max_health>")
            .placeholder("health", "15")
            .placeholder("max_health", "20")
            .sendActionBar(player);
        
        // Register reusable templates
        MessageBuilder.registerTemplate("welcome", "<gradient:gold:yellow>Welcome to <server>!");
        MessageBuilder.template("welcome")
            .placeholder("server", "My Server")
            .broadcast();
    }

    // ==================== Scoreboard API ====================
    
    public void scoreboardExample(Player player) {
        IonScoreboard board = IonScoreboard.builder()
            .title("<gradient:gold:yellow><bold>My Server")
            .line(15, "<gray>Welcome, <white>{player}")
            .line(14, "")
            .line(13, "<gold>Coins: <yellow>{coins}")
            .line(12, "<green>Online: <white>{online}")
            .line(11, "")
            .line(10, "<aqua>Rank: <white>{rank}")
            .placeholder("player", p -> p.getName())
            .placeholder("coins", p -> "1,234") // Replace with actual coin lookup
            .placeholder("online", p -> String.valueOf(Bukkit.getOnlinePlayers().size()))
            .placeholder("rank", p -> "VIP") // Replace with actual rank lookup
            .build();
        
        board.show(player);
        
        // Update periodically
        // board.update(player);
        
        // Hide when done
        // board.hide(player);
    }

    // ==================== BossBar API ====================
    
    public void bossBarExample(Player player) {
        IonBossBar bar = IonBossBar.builder()
            .name("event-progress")
            .title("<gradient:red:orange>Event Progress: {progress}%")
            .color(BossBar.Color.RED)
            .style(BossBar.Overlay.PROGRESS)
            .progress(0.5f)
            .placeholder("progress", p -> "50")
            .build();
        
        bar.show(player);
        
        // Update progress
        bar.setProgress(0.75f);
        bar.setTitle("<gradient:green:yellow>Almost done! {progress}%");
        
        // Hide when done
        // bar.hide(player);
    }

    // ==================== Batch Operations ====================
    
    @Table("player_stats")
    public static class PlayerStats {
        @PrimaryKey(autoGenerate = true)
        private int id;
        
        @Column(name = "player_uuid")
        private UUID playerUuid;
        
        @Column(name = "kills")
        private int kills;
        
        @Column(name = "deaths")
        private int deaths;
        
        // Constructors, getters, setters...
        public PlayerStats() {}
        
        public PlayerStats(UUID playerUuid, int kills, int deaths) {
            this.playerUuid = playerUuid;
            this.kills = kills;
            this.deaths = deaths;
        }
    }
    
    public void batchOperationExample() {
        // Create many entities efficiently
        List<PlayerStats> newStats = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            newStats.add(new PlayerStats(UUID.randomUUID(), 0, 0));
        }
        
        // Batch insert - much faster than individual inserts!
        BatchOperation.BatchResult result = database.batch(PlayerStats.class)
            .insertAll(newStats)
            .batchSize(500) // Process in batches of 500
            .execute();
        
        System.out.println("Inserted " + result.insertedCount() + " records in " + result.executionTimeMs() + "ms");
        
        // Async batch operations
        database.batch(PlayerStats.class)
            .updateAll(newStats)
            .executeAsync()
            .thenAccept(r -> {
                System.out.println("Updated " + r.totalAffected() + " records");
            });
    }

    // ==================== Metrics ====================
    
    public void metricsExample() {
        // Count events
        Metrics.increment("player.join");
        Metrics.increment("commands.executed");
        Metrics.increment("blocks.broken", 5);
        
        // Set gauge values
        Metrics.gauge("players.online", Bukkit.getOnlinePlayers().size());
        
        // Time operations
        String result = Metrics.time("database.query", () -> {
            // Simulate database query
            return "query result";
        });
        
        // Get statistics
        long joins = Metrics.getCount("player.join");
        double avgQueryTime = Metrics.getAverageTime("database.query");
        
        System.out.println("Player joins: " + joins);
        System.out.println("Avg query time: " + avgQueryTime + "ms");
        
        // Get detailed timing stats
        Metrics.TimingStats stats = Metrics.getTimingStats("database.query");
        if (stats != null) {
            System.out.println("Query count: " + stats.getCount());
            System.out.println("Min time: " + stats.getMinMs() + "ms");
            System.out.println("Max time: " + stats.getMaxMs() + "ms");
        }
    }

    // ==================== Combined Example ====================
    
    public void combinedExample(Player player) {
        // Rate limit + Cooldown + Metrics for a /heal command
        RateLimiter healLimiter = RateLimiter.create("heal", 3, 60, TimeUnit.SECONDS);
        CooldownManager healCooldown = CooldownManager.create("heal");
        
        // Check rate limit first
        if (!healLimiter.tryAcquire(player.getUniqueId())) {
            MessageBuilder.of("<red>You've used /heal too many times!")
                .send(player);
            return;
        }
        
        // Check cooldown
        if (healCooldown.isOnCooldown(player.getUniqueId())) {
            long remaining = healCooldown.getRemainingTime(player.getUniqueId(), TimeUnit.SECONDS);
            MessageBuilder.of("<yellow>Heal on cooldown: <white>{time}s remaining")
                .placeholder("time", String.valueOf(remaining))
                .send(player);
            return;
        }
        
        // Perform heal with metrics
        Metrics.time("command.heal", () -> {
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
        });
        
        Metrics.increment("heal.used");
        healCooldown.setCooldown(player.getUniqueId(), 30, TimeUnit.SECONDS);
        
        MessageBuilder.of("<green>You have been healed!")
            .send(player);
    }
}
