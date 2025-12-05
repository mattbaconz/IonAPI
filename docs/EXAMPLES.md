# IonAPI Examples

## Table of Contents

1. [Economy Plugin](#economy-plugin)
2. [Teleportation System](#teleportation-system)
3. [Custom Enchantments](#custom-enchantments)
4. [Player Statistics](#player-statistics)
5. [GUI Menus](#gui-menus)

---

## Economy Plugin

A simple economy system with balance management.

```java
package com.example.economy;

import com.ionapi.api.IonPlugin;
import com.ionapi.api.command.CommandContext;
import com.ionapi.api.command.IonCommand;
import com.ionapi.api.config.IonConfig;
import com.ionapi.api.event.IonEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyPlugin implements IonPlugin {
    
    private final Map<UUID, Double> balances = new HashMap<>();
    private double startingBalance;
    
    @Override
    public void onEnable() {
        // Load config
        IonConfig config = getConfigProvider().getConfig();
        startingBalance = config.getDouble("starting-balance", 100.0);
        
        // Register commands
        getCommandRegistry().register(new BalanceCommand());
        getCommandRegistry().register(new PayCommand());
        
        getLogger().info("Economy system enabled!");
    }
    
    public double getBalance(UUID playerId) {
        return balances.getOrDefault(playerId, startingBalance);
    }
    
    public void setBalance(UUID playerId, double amount) {
        double oldBalance = getBalance(playerId);
        balances.put(playerId, amount);
        
        // Fire event
        BalanceChangeEvent event = new BalanceChangeEvent(playerId, oldBalance, amount);
        getEventBus().fire(event);
    }
    
    public boolean withdraw(UUID playerId, double amount) {
        double balance = getBalance(playerId);
        if (balance >= amount) {
            setBalance(playerId, balance - amount);
            return true;
        }
        return false;
    }
    
    public void deposit(UUID playerId, double amount) {
        setBalance(playerId, getBalance(playerId) + amount);
    }
    
    // Balance Command
    private class BalanceCommand implements IonCommand {
        @Override
        public boolean execute(@NotNull CommandContext ctx) {
            // In real implementation, get player from sender
            // UUID playerId = ((Player) ctx.getSender()).getUniqueId();
            // double balance = getBalance(playerId);
            // ctx.reply("§aYour balance: $" + String.format("%.2f", balance));
            ctx.reply("§aBalance: $100.00");
            return true;
        }
        
        @Override
        public @NotNull String getName() { return "balance"; }
        
        @Override
        public @NotNull String getDescription() { return "Check your balance"; }
        
        @Override
        public @NotNull String getUsage() { return "/balance"; }
        
        @Override
        public @NotNull String getPermission() { return "economy.balance"; }
    }
    
    // Pay Command
    private class PayCommand implements IonCommand {
        @Override
        public boolean execute(@NotNull CommandContext ctx) {
            if (ctx.getArgCount() < 2) {
                ctx.reply("§cUsage: /pay <player> <amount>");
                return false;
            }
            
            String targetName = ctx.getArg(0);
            double amount;
            
            try {
                amount = Double.parseDouble(ctx.getArg(1));
            } catch (NumberFormatException e) {
                ctx.reply("§cInvalid amount!");
                return false;
            }
            
            if (amount <= 0) {
                ctx.reply("§cAmount must be positive!");
                return false;
            }
            
            // In real implementation:
            // UUID senderId = ((Player) ctx.getSender()).getUniqueId();
            // UUID targetId = getPlayerUUID(targetName);
            // if (withdraw(senderId, amount)) {
            //     deposit(targetId, amount);
            //     ctx.reply("§aPaid $" + amount + " to " + targetName);
            // } else {
            //     ctx.reply("§cInsufficient funds!");
            // }
            
            ctx.reply("§aPaid $" + amount + " to " + targetName);
            return true;
        }
        
        @Override
        public @NotNull String getName() { return "pay"; }
        
        @Override
        public @NotNull String getDescription() { return "Pay another player"; }
        
        @Override
        public @NotNull String getUsage() { return "/pay <player> <amount>"; }
        
        @Override
        public @NotNull String getPermission() { return "economy.pay"; }
    }
    
    // Custom Event
    public static class BalanceChangeEvent implements IonEvent {
        private final UUID playerId;
        private final double oldBalance;
        private final double newBalance;
        
        public BalanceChangeEvent(UUID playerId, double oldBalance, double newBalance) {
            this.playerId = playerId;
            this.oldBalance = oldBalance;
            this.newBalance = newBalance;
        }
        
        public UUID getPlayerId() { return playerId; }
        public double getOldBalance() { return oldBalance; }
        public double getNewBalance() { return newBalance; }
        
        @Override
        public @NotNull String getEventName() { return "BalanceChange"; }
        
        @Override
        public boolean isCancelled() { return false; }
        
        @Override
        public void setCancelled(boolean cancelled) {}
        
        @Override
        public boolean isCancellable() { return false; }
    }
}
```

---

## Teleportation System

Delayed teleportation with cooldowns.

```java
package com.example.teleport;

import com.ionapi.api.IonPlugin;
import com.ionapi.api.command.CommandContext;
import com.ionapi.api.command.IonCommand;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TeleportPlugin implements IonPlugin {
    
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final int COOLDOWN_SECONDS = 30;
    private final int DELAY_SECONDS = 3;
    
    @Override
    public void onEnable() {
        getCommandRegistry().register(new TeleportCommand());
        
        // Clean up old cooldowns every minute
        getScheduler().runTimer(() -> {
            long now = System.currentTimeMillis();
            cooldowns.entrySet().removeIf(entry -> 
                now - entry.getValue() > COOLDOWN_SECONDS * 1000);
        }, 1, 1, TimeUnit.MINUTES);
    }
    
    private class TeleportCommand implements IonCommand {
        @Override
        public boolean execute(@NotNull CommandContext ctx) {
            if (ctx.getArgCount() < 1) {
                ctx.reply("§cUsage: /tp <player>");
                return false;
            }
            
            // In real implementation, get player
            // Player player = (Player) ctx.getSender();
            // UUID playerId = player.getUniqueId();
            UUID playerId = UUID.randomUUID(); // Placeholder
            
            // Check cooldown
            if (cooldowns.containsKey(playerId)) {
                long timeLeft = (cooldowns.get(playerId) + COOLDOWN_SECONDS * 1000 - System.currentTimeMillis()) / 1000;
                ctx.reply("§cCooldown: " + timeLeft + " seconds remaining");
                return false;
            }
            
            ctx.reply("§aTeleporting in " + DELAY_SECONDS + " seconds... Don't move!");
            
            // Delayed teleport
            getScheduler().runLater(() -> {
                // In real implementation:
                // if (player hasn't moved) {
                //     player.teleport(target);
                //     ctx.reply("§aTeleported!");
                //     cooldowns.put(playerId, System.currentTimeMillis());
                // } else {
                //     ctx.reply("§cTeleport cancelled - you moved!");
                // }
                ctx.reply("§aTeleported!");
                cooldowns.put(playerId, System.currentTimeMillis());
            }, DELAY_SECONDS, TimeUnit.SECONDS);
            
            return true;
        }
        
        @Override
        public @NotNull String getName() { return "tp"; }
        
        @Override
        public @NotNull String getDescription() { return "Teleport to a player"; }
        
        @Override
        public @NotNull String getUsage() { return "/tp <player>"; }
        
        @Override
        public @NotNull String getPermission() { return "teleport.use"; }
    }
}
```

---

## Custom Enchantments

Register custom enchantment effects.

```java
package com.example.enchants;

import com.ionapi.api.IonPlugin;
import com.ionapi.api.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class EnchantmentsPlugin implements IonPlugin {
    
    @Override
    public void onEnable() {
        registerEnchantments();
        getLogger().info("Custom enchantments loaded!");
    }
    
    private void registerEnchantments() {
        // Listen for damage events with HIGH priority
        // In real implementation, subscribe to PlayerDamageEvent
        
        getEventBus().subscribe(CustomDamageEvent.class, EventPriority.HIGH, event -> {
            // Check for "Lightning" enchantment
            if (hasEnchantment(event.getWeapon(), "lightning")) {
                int level = getEnchantmentLevel(event.getWeapon(), "lightning");
                
                // Strike lightning
                strikeLightning(event.getVictim());
                
                // Add extra damage
                event.setDamage(event.getDamage() + (level * 2.0));
            }
        });
        
        // Vampire enchantment - heal on hit
        getEventBus().subscribe(CustomDamageEvent.class, event -> {
            if (hasEnchantment(event.getWeapon(), "vampire")) {
                int level = getEnchantmentLevel(event.getWeapon(), "vampire");
                double healAmount = event.getDamage() * (level * 0.1);
                
                // Heal attacker
                healPlayer(event.getAttacker(), healAmount);
            }
        });
        
        // Poison enchantment - apply poison effect
        getEventBus().subscribe(CustomDamageEvent.class, event -> {
            if (hasEnchantment(event.getWeapon(), "poison")) {
                int level = getEnchantmentLevel(event.getWeapon(), "poison");
                
                // Apply poison for 3 seconds per level
                applyPoison(event.getVictim(), level * 3);
            }
        });
    }
    
    // Placeholder methods
    private boolean hasEnchantment(Object weapon, String name) { return true; }
    private int getEnchantmentLevel(Object weapon, String name) { return 1; }
    private void strikeLightning(Object entity) {}
    private void healPlayer(Object player, double amount) {}
    private void applyPoison(Object entity, int duration) {}
    
    // Placeholder event
    public static class CustomDamageEvent implements com.ionapi.api.event.IonEvent {
        private double damage;
        
        public Object getWeapon() { return null; }
        public Object getVictim() { return null; }
        public Object getAttacker() { return null; }
        public double getDamage() { return damage; }
        public void setDamage(double damage) { this.damage = damage; }
        
        @Override
        public @NotNull String getEventName() { return "CustomDamage"; }
        
        @Override
        public boolean isCancelled() { return false; }
        
        @Override
        public void setCancelled(boolean cancelled) {}
        
        @Override
        public boolean isCancellable() { return true; }
    }
}
```

---

## Player Statistics

Track player statistics with database-like storage.

```java
package com.example.stats;

import com.ionapi.api.IonPlugin;
import com.ionapi.api.command.CommandContext;
import com.ionapi.api.command.IonCommand;
import com.ionapi.api.config.IonConfig;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class StatsPlugin implements IonPlugin {
    
    private final Map<UUID, PlayerStats> statsCache = new HashMap<>();
    
    @Override
    public void onEnable() {
        getCommandRegistry().register(new StatsCommand());
        
        // Auto-save stats every 5 minutes
        getScheduler().runTimer(() -> {
            saveAllStats();
        }, 5, 5, TimeUnit.MINUTES);
        
        getLogger().info("Stats system enabled!");
    }
    
    @Override
    public void onDisable() {
        saveAllStats();
    }
    
    public PlayerStats getStats(UUID playerId) {
        return statsCache.computeIfAbsent(playerId, id -> loadStats(id));
    }
    
    private PlayerStats loadStats(UUID playerId) {
        // Load from config or database
        IonConfig config = getConfigProvider().loadConfig("stats/" + playerId + ".yml");
        
        PlayerStats stats = new PlayerStats();
        stats.kills = config.getInt("kills", 0);
        stats.deaths = config.getInt("deaths", 0);
        stats.playTime = config.getLong("play-time", 0);
        stats.blocksPlaced = config.getInt("blocks-placed", 0);
        stats.blocksBroken = config.getInt("blocks-broken", 0);
        
        return stats;
    }
    
    private void saveAllStats() {
        getScheduler().runAsync(() -> {
            for (Map.Entry<UUID, PlayerStats> entry : statsCache.entrySet()) {
                saveStats(entry.getKey(), entry.getValue());
            }
            getLogger().info("Saved stats for " + statsCache.size() + " players");
        });
    }
    
    private void saveStats(UUID playerId, PlayerStats stats) {
        IonConfig config = getConfigProvider().loadConfig("stats/" + playerId + ".yml");
        config.set("kills", stats.kills);
        config.set("deaths", stats.deaths);
        config.set("play-time", stats.playTime);
        config.set("blocks-placed", stats.blocksPlaced);
        config.set("blocks-broken", stats.blocksBroken);
        config.save();
    }
    
    // Stats Command
    private class StatsCommand implements IonCommand {
        @Override
        public boolean execute(@NotNull CommandContext ctx) {
            // In real implementation:
            // UUID playerId = ((Player) ctx.getSender()).getUniqueId();
            // PlayerStats stats = getStats(playerId);
            
            ctx.reply("§6=== Your Statistics ===");
            ctx.reply("§aKills: §f100");
            ctx.reply("§aDeaths: §f50");
            ctx.reply("§aK/D Ratio: §f2.0");
            ctx.reply("§aBlocks Placed: §f1,234");
            ctx.reply("§aBlocks Broken: §f5,678");
            ctx.reply("§aPlay Time: §f12h 34m");
            
            return true;
        }
        
        @Override
        public @NotNull String getName() { return "stats"; }
        
        @Override
        public @NotNull String getDescription() { return "View your statistics"; }
        
        @Override
        public @NotNull String getUsage() { return "/stats"; }
        
        @Override
        public @NotNull String getPermission() { return "stats.view"; }
    }
    
    // Stats data class
    public static class PlayerStats {
        public int kills;
        public int deaths;
        public long playTime;
        public int blocksPlaced;
        public int blocksBroken;
        
        public double getKDRatio() {
            return deaths == 0 ? kills : (double) kills / deaths;
        }
    }
}
```
---

## Folia-Aware Scheduling

Examples showing how to write Folia-compatible code using context-aware scheduling.

### Entity-Based Damage System

```java
package com.example.combat;

import com.ionapi.api.IonPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.concurrent.TimeUnit;

public class CombatPlugin implements IonPlugin implements Listener {
    
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }
    
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        
        // ❌ WRONG on Folia - may run on wrong thread
        // getScheduler().runLater(() -> {
        //     victim.damage(2.0); // UNSAFE!
        // }, 2, TimeUnit.SECONDS);
        
        // ✅ CORRECT - runs on victim's region thread
        getScheduler().runAtLater(victim, () -> {
            if (victim.isValid()) {
                victim.damage(2.0); // Bleed damage
                victim.sendMessage("§cYou're bleeding!");
            }
        }, 2, TimeUnit.SECONDS);
        
        // ✅ CORRECT - runs on attacker's region thread
        getScheduler().runAt(attacker, () -> {
            attacker.sendMessage("§aYou dealt damage!");
        });
    }
}
```

### Location-Based Particle Effects

```java
package com.example.effects;

import com.ionapi.api.IonPlugin;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class EffectsPlugin implements IonPlugin implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;
        
        Location target = player.getTargetBlock(null, 100).getLocation();
        
        // ✅ Spawn particles at specific location (region-aware)
        getScheduler().runAt(target, () -> {
            target.getWorld().spawnParticle(
                Particle.FLAME,
                target.add(0, 1, 0),
                50, 0.5, 0.5, 0.5, 0.1
            );
        });
        
        // ✅ Delayed explosion at location
        getScheduler().runAtLater(target, () -> {
            target.getWorld().createExplosion(target, 4.0f, false, false);
        }, 3, TimeUnit.SECONDS);
        
        return true;
    }
}
```

### Cross-Region Player Teleportation

```java
package com.example.warp;

import com.ionapi.api.IonPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class WarpPlugin implements IonPlugin {
    
    public void teleportWithEffects(Player player, Location destination) {
        // Step 1: Run on player's current region
        getScheduler().runAt(player, () -> {
            player.sendMessage("§aTeleporting...");
            
            // Step 2: Teleport (this moves player to new region)
            player.teleport(destination);
            
            // Step 3: Run effects on destination region
            getScheduler().runAt(destination, () -> {
                destination.getWorld().spawnParticle(
                    Particle.PORTAL, 
                    destination, 
                    100, 1, 1, 1, 0.5
                );
            });
            
            // Step 4: Follow-up on player's new region (after teleport)
            getScheduler().runAtLater(player, () -> {
                player.sendMessage("§aYou have arrived!");
            }, 500, TimeUnit.MILLISECONDS);
        });
    }
}
```

### Async Database with Sync Updates

```java
package com.example.data;

import com.ionapi.api.IonPlugin;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DataPlugin implements IonPlugin {
    
    public void loadAndApplyPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        
        // ❌ WRONG - blocking main thread
        // PlayerData data = database.load(uuid);
        // applyData(player, data);
        
        // ✅ CORRECT - async load, sync apply
        getScheduler().runAsync(() -> {
            // Heavy I/O operation off main thread
            PlayerData data = fetchFromDatabase(uuid);
            
            // Apply changes on player's region thread
            getScheduler().runAt(player, () -> {
                if (!player.isOnline()) return;
                
                player.setHealth(data.health);
                player.setFoodLevel(data.hunger);
                player.setLevel(data.level);
                player.sendMessage("§aData loaded successfully!");
            });
        });
    }
    
    public void savePlayerData(Player player) {
        // Capture data on player's region thread
        getScheduler().runAt(player, () -> {
            PlayerData data = new PlayerData(
                player.getHealth(),
                player.getFoodLevel(),
                player.getLevel()
            );
            
            // Save async
            getScheduler().runAsync(() -> {
                saveToDatabase(player.getUniqueId(), data);
                getLogger().info("Saved data for " + player.getName());
            });
        });
    }
    
    private PlayerData fetchFromDatabase(UUID uuid) { return new PlayerData(20, 20, 1); }
    private void saveToDatabase(UUID uuid, PlayerData data) {}
    
    private record PlayerData(double health, int hunger, int level) {}
}
```

### Boss Fight Mechanics

```java
package com.example.boss;

import com.ionapi.api.IonPlugin;
import com.ionapi.api.scheduler.IonTask;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.concurrent.TimeUnit;

public class BossPlugin implements IonPlugin {
    
    public void spawnBoss(Location location) {
        Entity boss = location.getWorld().spawnEntity(location, EntityType.WITHER);
        
        // ✅ All boss mechanics run on boss's region thread
        startBossAbilities(boss);
    }
    
    private void startBossAbilities(Entity boss) {
        // Ability 1: Area damage every 5 seconds
        IonTask damageTask = getScheduler().runAtTimer(boss, () -> {
            if (!boss.isValid()) return;
            
            boss.getNearbyEntities(10, 10, 10).forEach(nearby -> {
                if (nearby instanceof LivingEntity living && living != boss) {
                    // Damage entities on their own region threads
                    getScheduler().runAt(living, () -> {
                        if (living.isValid()) {
                            living.damage(4.0);
                        }
                    });
                }
            });
        }, 5, 5, TimeUnit.SECONDS);
        
        // Ability 2: Spawn minions every 15 seconds
        IonTask spawnTask = getScheduler().runAtTimer(boss, () -> {
            if (!boss.isValid()) {
                return;
            }
            
            Location loc = boss.getLocation();
            for (int i = 0; i < 3; i++) {
                Location spawnLoc = loc.clone().add(
                    Math.random() * 10 - 5,
                    0,
                    Math.random() * 10 - 5
                );
                
                // Spawn on the spawn location's region
                getScheduler().runAt(spawnLoc, () -> {
                    spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
                });
            }
        }, 15, 15, TimeUnit.SECONDS);
        
        // Cleanup: Cancel tasks when boss dies
        getScheduler().runAtTimer(boss, () -> {
            if (!boss.isValid()) {
                damageTask.cancel();
                spawnTask.cancel();
                getLogger().info("Boss defeated! Stopped all abilities.");
            }
        }, 1, 1, TimeUnit.SECONDS);
    }
}
```

### Multi-Player Task Distribution

```java
package com.example.broadcast;

import com.ionapi.api.IonPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class BroadcastPlugin implements IonPlugin {
    
    @Override
    public void onEnable() {
        // ❌ WRONG - sends all messages on global scheduler (bottleneck)
        // getScheduler().runTimer(() -> {
        //     for (Player player : Bukkit.getOnlinePlayers()) {
        //         player.sendActionBar("Server TPS: 20.0");
        //     }
        // }, 0, 1, TimeUnit.SECONDS);
        
        // ✅ CORRECT - distributes work across region threads
        getScheduler().runTimer(() -> {
            String message = "§6Server TPS: §a20.0";
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                // Each player gets updated on their own region thread
                getScheduler().runAt(player, () -> {
                    if (player.isOnline()) {
                        player.sendActionBar(message);
                    }
                });
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
}
```

---

## Tips for Production

1. **Error Handling**: Always wrap risky operations in try-catch
2. **Async Operations**: Use `runAsync()` for I/O operations
3. **Resource Cleanup**: Cancel tasks and save data in `onDisable()`
4. **Configuration**: Store settings in config files, not hardcoded
5. **Events**: Use appropriate priorities (MONITOR for logging, HIGH for modifications)

## More Examples

Check the [API Reference](API_REFERENCE.md) for detailed documentation on each feature.
