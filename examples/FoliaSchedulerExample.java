package com.ionapi.examples;

import com.ionapi.api.IonPlugin;
import com.ionapi.api.scheduler.IonScheduler;
import com.ionapi.api.scheduler.IonTask;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Comprehensive example demonstrating Folia-aware scheduling with IonAPI.
 * <p>
 * This example showcases:
 * - Entity-based scheduling for thread-safe entity operations
 * - Location-based scheduling for world modifications
 * - Async tasks for database/I/O operations
 * - Proper cross-region teleportation
 * - Boss fight mechanics with region-aware abilities
 * <p>
 * All examples work on both Paper (single-threaded) and Folia (multi-threaded).
 */
public class FoliaSchedulerExample implements IonPlugin, Listener {

    private final Map<UUID, PlayerData> playerDataCache = new HashMap<>();

    /**
     * Example 1: Entity-Based Scheduling
     * Use runAt(entity) when modifying entities to ensure thread safety on Folia.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // ❌ WRONG on Folia - uses global scheduler, may cause threading issues
        // getScheduler().run(() -> player.sendMessage("Welcome!"));

        // ✅ CORRECT - runs on player's region thread
        getScheduler().runAt(player, () -> {
            player.sendMessage("§aWelcome to the server!");
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.SPEED, 200, 1));
        });

        // Load player data asynchronously, then apply on correct thread
        loadPlayerDataAsync(player);
    }

    /**
     * Example 2: Async Database + Sync Application
     * Heavy I/O should run async, then switch to entity's region thread.
     */
    private void loadPlayerDataAsync(Player player) {
        UUID uuid = player.getUniqueId();

        // Step 1: Load data asynchronously (off main/region threads)
        getScheduler().runAsync(() -> {
            getLogger().info("Loading data for " + player.getName() + "...");

            // Simulate database query
            PlayerData data = fetchFromDatabase(uuid);

            // Step 2: Apply data on player's region thread
            getScheduler().runAt(player, () -> {
                if (!player.isOnline())
                    return;

                playerDataCache.put(uuid, data);
                player.setHealth(data.health);
                player.setLevel(data.level);
                player.sendMessage("§aYour data has been loaded!");
            });
        });
    }

    /**
     * Example 3: Location-Based Scheduling
     * Use runAt(location) for world modifications at specific locations.
     */
    public void createParticleEffect(Location location) {
        // ✅ Spawns particles on the region owning this location
        getScheduler().runAt(location, () -> {
            location.getWorld().spawnParticle(
                    Particle.FLAME,
                    location,
                    50, // count
                    0.5, 0.5, 0.5, // offset
                    0.1); // speed
        });

        // ✅ Delayed explosion at location
        getScheduler().runAtLater(location, () -> {
            location.getWorld().createExplosion(location, 4.0f, false, false);
        }, 3, TimeUnit.SECONDS);
    }

    /**
     * Example 4: Cross-Region Teleportation
     * Properly handles teleporting players across regions.
     */
    public void teleportWithEffects(Player player, Location destination) {
        // Step 1: Run on player's current region thread
        getScheduler().runAt(player, () -> {
            player.sendMessage("§6Teleporting...");

            // Step 2: Perform teleport (moves player to new region)
            player.teleport(destination);

            // Step 3: Spawn effects on destination region
            getScheduler().runAt(destination, () -> {
                destination.getWorld().spawnParticle(
                        Particle.PORTAL, destination,
                        100, 1, 1, 1, 0.5);
            });

            // Step 4: Message player on their new region thread (after teleport)
            getScheduler().runAtLater(player, () -> {
                player.sendMessage("§aYou have arrived!");
            }, 500, TimeUnit.MILLISECONDS);
        });
    }

    /**
     * Example 5: Delayed Entity Tasks
     * Schedule delayed actions on entities with proper thread safety.
     */
    public void applyDelayedEffect(LivingEntity entity, PotionEffectType effectType) {
        getScheduler().runAtLater(entity, () -> {
            if (!entity.isValid())
                return; // Entity was removed

            entity.addPotionEffect(new PotionEffect(effectType, 600, 1));

            if (entity instanceof Player player) {
                player.sendMessage("§eYou have been afflicted!");
            }
        }, 5, TimeUnit.SECONDS);
    }

    /**
     * Example 6: Repeating Entity Tasks
     * Periodic tasks that run on entity's region thread.
     */
    public void trackPlayerScore(Player player) {
        // Updates action bar every second on player's region thread
        IonTask task = getScheduler().runAtTimer(player, () -> {
            if (!player.isOnline()) {
                return; // Task will auto-cleanup
            }

            int score = calculateScore(player);
            player.sendActionBar("§6Score: §e" + score);
        }, 0, 1, TimeUnit.SECONDS);

        // Store task for later cancellation if needed
        // tasks.put(player.getUniqueId(), task);
    }

    /**
     * Example 7: Boss Fight with Region-Aware Mechanics
     * Complex example showing boss abilities distributed across region threads.
     */
    public void spawnBoss(Location location) {
        Entity boss = location.getWorld().spawnEntity(location, EntityType.WITHER);
        getLogger().info("Spawned boss at " + location);

        startBossAbilities(boss);
    }

    private void startBossAbilities(Entity boss) {
        IonScheduler scheduler = getScheduler();

        // Ability 1: Area damage every 5 seconds (runs on boss's region thread)
        IonTask damageTask = scheduler.runAtTimer(boss, () -> {
            if (!boss.isValid())
                return;

            // Find nearby entities
            boss.getNearbyEntities(10, 10, 10).forEach(nearby -> {
                if (nearby instanceof LivingEntity living && living != boss) {
                    // Damage each entity on their own region thread
                    scheduler.runAt(living, () -> {
                        if (living.isValid()) {
                            living.damage(4.0);
                        }
                    });
                }
            });
        }, 5, 5, TimeUnit.SECONDS);

        // Ability 2: Spawn minions every 15 seconds
        IonTask spawnTask = scheduler.runAtTimer(boss, () -> {
            if (!boss.isValid())
                return;

            Location bossLoc = boss.getLocation();

            for (int i = 0; i < 3; i++) {
                Location spawnLoc = bossLoc.clone().add(
                        Math.random() * 10 - 5,
                        0,
                        Math.random() * 10 - 5);

                // Spawn minion on spawn location's region thread
                scheduler.runAt(spawnLoc, () -> {
                    spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
                });
            }
        }, 15, 15, TimeUnit.SECONDS);

        // Cleanup: Cancel tasks when boss dies
        scheduler.runAtTimer(boss, () -> {
            if (!boss.isValid()) {
                damageTask.cancel();
                spawnTask.cancel();
                getLogger().info("Boss defeated! Abilities stopped.");
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * Example 8: Multi-Player Task Distribution
     * Efficiently distribute tasks across region threads.
     */
    public void broadcastActionBar() {
        getScheduler().runTimer(() -> {
            String message = "§6TPS: §a20.0";

            // ❌ WRONG - sends all on global scheduler (bottleneck on Folia)
            // for (Player p : Bukkit.getOnlinePlayers()) {
            // p.sendActionBar(message);
            // }

            // ✅ CORRECT - distributes work across region threads
            for (Player player : getServer().getOnlinePlayers()) {
                getScheduler().runAt(player, () -> {
                    if (player.isOnline()) {
                        player.sendActionBar(message);
                    }
                });
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * Example 9: Saving Player Data
     * Capture data on region thread, then save asynchronously.
     */
    public void savePlayerData(Player player) {
        // Step 1: Capture data on player's region thread
        getScheduler().runAt(player, () -> {
            PlayerData data = new PlayerData(
                    player.getHealth(),
                    player.getLevel());

            // Step 2: Save asynchronously
            getScheduler().runAsync(() -> {
                saveToDatabase(player.getUniqueId(), data);
                getLogger().info("Saved data for " + player.getName());
            });
        });
    }

    // ========== Helper Methods ==========

    private int calculateScore(Player player) {
        return player.getLevel() * 100;
    }

    private PlayerData fetchFromDatabase(UUID uuid) {
        // Simulate database fetch
        try {
            Thread.sleep(100); // Simulated I/O delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return new PlayerData(20.0, 1);
    }

    private void saveToDatabase(UUID uuid, PlayerData data) {
        // Simulate database save
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ========== Plugin Lifecycle ==========

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("FoliaSchedulerExample enabled!");

        // Example: Start global broadcast
        broadcastActionBar();
    }

    @Override
    public void onDisable() {
        // Save all player data
        for (Player player : getServer().getOnlinePlayers()) {
            savePlayerData(player);
        }

        getScheduler().cancelAll();
        getLogger().info("FoliaSchedulerExample disabled!");
    }

    @Override
    public @NotNull String getName() {
        return "FoliaSchedulerExample";
    }

    @Override
    public @NotNull Logger getLogger() {
        return Logger.getLogger(getName());
    }

    // ========== Data Classes ==========

    private record PlayerData(double health, int level) {
    }
}
