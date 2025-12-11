package com.ionapi.examples;

import com.ionapi.api.IonPlugin;
import com.ionapi.database.DatabaseType;
import com.ionapi.database.IonDatabase;
import com.ionapi.database.annotations.Column;
import com.ionapi.database.annotations.PrimaryKey;
import com.ionapi.database.annotations.Table;
import com.ionapi.gui.IonGui;
import com.ionapi.item.IonItem;
import com.ionapi.tasks.TaskChain;
import com.ionapi.ui.IonBossBar;
import com.ionapi.ui.IonScoreboard;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Comprehensive example demonstrating all IonAPI features:
 * - Item Builder: Creating custom items with fluent API
 * - GUI System: Interactive inventory menus
 * - Scoreboards: Dynamic player scoreboards
 * - BossBars: Visual progress indicators
 * - Task Chains: Complex async/sync workflows
 * - Database: ORM with async support
 *
 * This example creates a complete player stats system with:
 * - Persistent database storage
 * - Real-time scoreboard updates
 * - Interactive profile GUI
 * - Rewards system with animations
 */
public class ComprehensiveExample implements IonPlugin, Listener {

    private IonDatabase database;
    private final Map<UUID, IonScoreboard> playerScoreboards = new HashMap<>();
    private final Map<UUID, PlayerStats> cachedStats = new HashMap<>();

    // ========== Plugin Lifecycle ==========

    @Override
    public void onEnable() {
        getLogger().info("Initializing Comprehensive Example Plugin...");

        // Initialize database
        initializeDatabase();

        // Register event listeners
        getServer().getPluginManager().registerEvents(this, this);

        // Start periodic save task
        getScheduler().runTimer(this::saveAllPlayerData, 0, 6000, TimeUnit.MILLISECONDS); // Every 5 minutes

        getLogger().info("Comprehensive Example Plugin enabled!");
    }

    @Override
    public void onDisable() {
        // Save all player data
        saveAllPlayerData();

        // Clean up scoreboards
        playerScoreboards.values().forEach(IonScoreboard::destroy);
        playerScoreboards.clear();

        // Disconnect database
        if (database != null) {
            database.disconnect();
        }

        getLogger().info("Comprehensive Example Plugin disabled!");
    }

    @Override
    public @NotNull String getName() {
        return "ComprehensiveExample";
    }

    @Override
    public @NotNull Logger getLogger() {
        return Logger.getLogger(getName());
    }

    // ========== Database Setup ==========

    private void initializeDatabase() {
        database = IonDatabase.builder()
                .type(DatabaseType.SQLITE)
                .database("plugins/ComprehensiveExample/data.db")
                .build();

        try {
            database.connect();
            database.createTable(PlayerStats.class);
            getLogger().info("Database connected successfully!");
        } catch (Exception e) {
            getLogger().severe("Failed to connect to database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== Event Handlers ==========

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Load player data with Task Chain
        TaskChain.create(this)
                .async(() -> {
                    // Step 1: Load from database (async)
                    PlayerStats stats = database.find(PlayerStats.class, player.getUniqueId());

                    if (stats == null) {
                        // Create new player stats
                        stats = new PlayerStats(player.getUniqueId(), player.getName());
                        database.insert(stats);
                        getLogger().info("Created new player stats for " + player.getName());
                    } else {
                        // Update last login
                        stats.setLastLogin(System.currentTimeMillis());
                        stats.setLogins(stats.getLogins() + 1);
                        database.update(stats);
                    }

                    return stats;
                })
                .syncAt(player, stats -> {
                    // Step 2: Apply data to player (on player's thread)
                    cachedStats.put(player.getUniqueId(), stats);

                    // Show welcome message
                    player.sendMessage("§6§l✦ §eWelcome back, §f" + player.getName() + "§e!");
                    player.sendMessage("§7You have joined §a" + stats.getLogins() + "§7 times.");

                    // Create and show scoreboard
                    createScoreboard(player, stats);

                    // Show welcome BossBar
                    showWelcomeBossBar(player);
                })
                .delay(2, TimeUnit.SECONDS)
                .syncAt(player, stats -> {
                    // Step 3: Open profile GUI after delay
                    openProfileGui(player, stats);
                })
                .exceptionally(ex -> {
                    player.sendMessage("§cFailed to load your data! Please contact an administrator.");
                    getLogger().severe("Error loading player data: " + ex.getMessage());
                    ex.printStackTrace();
                })
                .execute();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Save player data asynchronously
        PlayerStats stats = cachedStats.get(uuid);
        if (stats != null) {
            database.saveAsync(stats)
                    .thenRun(() -> getLogger().info("Saved data for " + player.getName()))
                    .exceptionally(ex -> {
                        getLogger().severe("Failed to save data for " + player.getName() + ": " + ex.getMessage());
                        return null;
                    });
        }

        // Clean up scoreboard
        IonScoreboard board = playerScoreboards.remove(uuid);
        if (board != null) {
            board.destroy();
        }

        // Remove from cache
        cachedStats.remove(uuid);
    }

    // ========== Scoreboard ==========

    private void createScoreboard(Player player, PlayerStats stats) {
        IonScoreboard board = IonScoreboard.builder()
                .title("<gold><bold>✦ <yellow>Your Stats <gold><bold>✦")
                .line(15, "<gray>━━━━━━━━━━━━━━")
                .line(14, "<yellow>Level: <white>{level}")
                .line(13, "<green>Coins: <white>{coins}")
                .line(12, "<aqua>Kills: <white>{kills}")
                .line(11, "<red>Deaths: <white>{deaths}")
                .line(10, "")
                .line(9, "<gray>Rank: <white>{rank}")
                .line(8, "<gray>━━━━━━━━━━━━━━")
                .placeholder("level", p -> String.valueOf(cachedStats.getOrDefault(p.getUniqueId(), stats).getLevel()))
                .placeholder("coins", p -> String.valueOf(cachedStats.getOrDefault(p.getUniqueId(), stats).getCoins()))
                .placeholder("kills", p -> String.valueOf(cachedStats.getOrDefault(p.getUniqueId(), stats).getKills()))
                .placeholder("deaths",
                        p -> String.valueOf(cachedStats.getOrDefault(p.getUniqueId(), stats).getDeaths()))
                .placeholder("rank", p -> getRankDisplay(cachedStats.getOrDefault(p.getUniqueId(), stats).getLevel()))
                .updateInterval(20) // Update every second
                .fixedWidth(20) // Prevent width changes
                .build();

        board.show(player);
        playerScoreboards.put(player.getUniqueId(), board);
    }

    // ========== BossBar ==========

    private void showWelcomeBossBar(Player player) {
        IonBossBar bar = IonBossBar.create()
                .title("§6§l✦ Welcome to the Server! ✦")
                .progress(1.0f)
                .color(BossBar.Color.YELLOW)
                .overlay(BossBar.Overlay.PROGRESS)
                .show(player);

        // Fade out after 5 seconds
        int[] countdown = { 5 };
        getScheduler().runTimer(() -> {
            countdown[0]--;
            if (countdown[0] <= 0) {
                bar.hide(player);
            } else {
                bar.progress(countdown[0] / 5.0f);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    // ========== GUI System ==========

    private void openProfileGui(Player player, PlayerStats stats) {
        // Create profile item using Item Builder
        ItemStack profileHead = IonItem.builder(Material.PLAYER_HEAD)
                .name("§e§l" + player.getName())
                .lore(
                        "§7Your Profile",
                        "",
                        "§eLevel: §f" + stats.getLevel(),
                        "§aCoins: §f" + stats.getCoins(),
                        "§bKills: §f" + stats.getKills(),
                        "§cDeaths: §f" + stats.getDeaths(),
                        "",
                        "§7Total Logins: §f" + stats.getLogins())
                .glow()
                .build();

        // Create reward items
        ItemStack dailyReward = IonItem.builder(Material.GOLD_INGOT)
                .name("§6§lDaily Reward")
                .lore(
                        "§7Claim your daily bonus!",
                        "",
                        "§a+ 100 Coins",
                        "§a+ 50 XP",
                        "",
                        "§e§lClick to claim!")
                .enchant(Enchantment.LURE, 1)
                .flags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS)
                .build();

        ItemStack levelUp = IonItem.builder(Material.EXPERIENCE_BOTTLE)
                .name("§b§lLevel Up!")
                .lore(
                        "§7Spend coins to level up",
                        "",
                        "§cCost: §f" + (stats.getLevel() * 100) + " coins",
                        "§aReward: §f+1 Level",
                        "",
                        "§e§lClick to level up!")
                .glow()
                .build();

        ItemStack statsItem = IonItem.builder(Material.BOOK)
                .name("§a§lYour Statistics")
                .lore(
                        "§7Detailed stats",
                        "",
                        "§eK/D Ratio: §f" + String.format("%.2f", stats.getKDRatio()),
                        "§eAverage XP: §f" + (stats.getLevel() * 100),
                        "§eMember Since: §f" + formatTime(stats.getFirstJoin()))
                .build();

        // Create GUI
        IonGui gui = IonGui.builder()
                .title("§6§l✦ §eYour Profile §6§l✦")
                .rows(4)
                // Profile in center
                .item(13, profileHead)
                // Reward items
                .item(20, dailyReward, click -> {
                    handleDailyReward(player, stats);
                    click.close();
                })
                .item(22, levelUp, click -> {
                    handleLevelUp(player, stats);
                    click.update();
                })
                .item(24, statsItem)
                // Border decoration
                .fillBorderBuilder(IonItem.of(Material.YELLOW_STAINED_GLASS_PANE, " "))
                // Close button
                .item(31, IonItem.builder(Material.BARRIER)
                        .name("§c§lClose")
                        .build(), click -> click.close())
                .onCloseHandler(close -> {
                    player.sendMessage("§7Thanks for visiting your profile!");
                })
                .build();

        gui.open(player);
    }

    // ========== Reward Handlers ==========

    private void handleDailyReward(Player player, PlayerStats stats) {
        // Check if already claimed today
        long lastClaim = stats.getLastDailyClaim();
        long timeSinceLastClaim = System.currentTimeMillis() - lastClaim;
        long oneDay = 24 * 60 * 60 * 1000;

        if (timeSinceLastClaim < oneDay) {
            long timeLeft = oneDay - timeSinceLastClaim;
            int hoursLeft = (int) (timeLeft / (60 * 60 * 1000));
            player.sendMessage("§cYou already claimed your daily reward!");
            player.sendMessage("§7Come back in §e" + hoursLeft + " hours§7.");
            return;
        }

        // Award reward with Task Chain
        TaskChain.create(this)
                .syncAt(player, () -> {
                    stats.setCoins(stats.getCoins() + 100);
                    stats.setLastDailyClaim(System.currentTimeMillis());
                    player.sendMessage("§a§l✓ §aClaimed daily reward!");
                    player.sendMessage("§7Received: §6+100 Coins §7and §b+50 XP");

                    // Show BossBar animation
                    IonBossBar rewardBar = IonBossBar.create("§6§l✦ Daily Reward Claimed! ✦")
                            .progress(1.0f)
                            .color(BossBar.Color.GREEN)
                            .show(player);

                    getScheduler().runLater(() -> rewardBar.hide(player), 3, TimeUnit.SECONDS);
                })
                .async(() -> {
                    // Save to database
                    database.save(stats);
                })
                .execute();
    }

    private void handleLevelUp(Player player, PlayerStats stats) {
        int cost = stats.getLevel() * 100;

        if (stats.getCoins() < cost) {
            player.sendMessage("§cNot enough coins! Need §6" + cost + " coins§c.");
            return;
        }

        // Level up with animation
        TaskChain.create(this)
                .syncAt(player, () -> {
                    stats.setCoins(stats.getCoins() - cost);
                    stats.setLevel(stats.getLevel() + 1);

                    player.sendMessage("§a§l✓ §aLEVEL UP!");
                    player.sendMessage("§7You are now level §e" + stats.getLevel() + "§7!");

                    // Play effect
                    player.getWorld().spawnParticle(
                            org.bukkit.Particle.VILLAGER_HAPPY,
                            player.getLocation().add(0, 1, 0),
                            50, 0.5, 0.5, 0.5, 0.1);

                    // Show level up BossBar
                    IonBossBar levelBar = IonBossBar.create()
                            .title("§6§l✦ LEVEL " + stats.getLevel() + " ✦")
                            .progress(1.0f)
                            .color(BossBar.Color.YELLOW)
                            .overlay(BossBar.Overlay.NOTCHED_10)
                            .show(player);

                    getScheduler().runLater(() -> levelBar.hide(player), 5, TimeUnit.SECONDS);
                })
                .async(() -> {
                    // Save to database
                    database.save(stats);
                })
                .execute();
    }

    // ========== Utility Methods ==========

    private void saveAllPlayerData() {
        for (PlayerStats stats : cachedStats.values()) {
            database.saveAsync(stats).exceptionally(ex -> {
                getLogger().severe("Failed to save player data: " + ex.getMessage());
                return null;
            });
        }
    }

    private String getRankDisplay(int level) {
        if (level >= 50)
            return "§6§lLegend";
        if (level >= 30)
            return "§5§lMaster";
        if (level >= 20)
            return "§b§lExpert";
        if (level >= 10)
            return "§a§lVeteran";
        return "§7Novice";
    }

    private String formatTime(long timestamp) {
        long days = (System.currentTimeMillis() - timestamp) / (24 * 60 * 60 * 1000);
        if (days > 365)
            return (days / 365) + " years ago";
        if (days > 30)
            return (days / 30) + " months ago";
        if (days > 0)
            return days + " days ago";
        return "Today";
    }

    // ========== Database Entity ==========

    @Table("player_stats")
    public static class PlayerStats {

        @PrimaryKey
        private UUID uuid;

        @Column(nullable = false)
        private String name;

        @Column(defaultValue = "1")
        private int level;

        @Column(defaultValue = "0")
        private int coins;

        @Column(defaultValue = "0")
        private int kills;

        @Column(defaultValue = "0")
        private int deaths;

        @Column(defaultValue = "0")
        private int logins;

        @Column(name = "first_join")
        private long firstJoin;

        @Column(name = "last_login")
        private long lastLogin;

        @Column(name = "last_daily_claim", defaultValue = "0")
        private long lastDailyClaim;

        // Required no-arg constructor for ORM
        public PlayerStats() {
        }

        public PlayerStats(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;
            this.level = 1;
            this.coins = 100; // Starting coins
            this.kills = 0;
            this.deaths = 0;
            this.logins = 1;
            this.firstJoin = System.currentTimeMillis();
            this.lastLogin = System.currentTimeMillis();
            this.lastDailyClaim = 0;
        }

        // Getters and Setters
        public UUID getUuid() {
            return uuid;
        }

        public void setUuid(UUID uuid) {
            this.uuid = uuid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public int getCoins() {
            return coins;
        }

        public void setCoins(int coins) {
            this.coins = coins;
        }

        public int getKills() {
            return kills;
        }

        public void setKills(int kills) {
            this.kills = kills;
        }

        public int getDeaths() {
            return deaths;
        }

        public void setDeaths(int deaths) {
            this.deaths = deaths;
        }

        public int getLogins() {
            return logins;
        }

        public void setLogins(int logins) {
            this.logins = logins;
        }

        public long getFirstJoin() {
            return firstJoin;
        }

        public void setFirstJoin(long firstJoin) {
            this.firstJoin = firstJoin;
        }

        public long getLastLogin() {
            return lastLogin;
        }

        public void setLastLogin(long lastLogin) {
            this.lastLogin = lastLogin;
        }

        public long getLastDailyClaim() {
            return lastDailyClaim;
        }

        public void setLastDailyClaim(long lastDailyClaim) {
            this.lastDailyClaim = lastDailyClaim;
        }

        public double getKDRatio() {
            if (deaths == 0)
                return kills;
            return (double) kills / deaths;
        }
    }
}
