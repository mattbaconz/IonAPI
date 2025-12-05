# Folia Development Guide

This guide helps you develop Folia-compatible plugins using IonAPI's scheduler system.

---

## Contents

1. [Understanding Folia](#understanding-folia)
2. [Scheduler Selection Guide](#scheduler-selection-guide)
3. [Context-Aware Scheduling](#context-aware-scheduling)
4. [Migration from Paper](#migration-from-paper)
5. [Common Patterns](#common-patterns)
6. [Troubleshooting](#troubleshooting)

---

## Understanding Folia

### What is Folia?

**Folia** is Paper's experimental multi-threaded fork that divides the world into **regions**, with each region running on its own thread. This enables true parallel processing of different world areas.

### Key Differences from Paper

| Aspect | Paper | Folia |
|--------|-------|-------|
| **Threading** | Single main thread | Multiple region threads |
| **Scheduler** | One main scheduler | Global, Entity, and Region schedulers |
| **Entity Access** | Always thread-safe | Only safe on owning thread |
| **Performance** | Limited by single thread | Scales with CPU cores |

### Thread Safety Rules

> [!CAUTION]
> **Critical Rule**: On Folia, you can only access entities/blocks from their **owning region thread**. Accessing them from the wrong thread will cause crashes.

```java
// ❌ DANGEROUS on Folia - may run on wrong thread
plugin.getScheduler().run(() -> {
    player.damage(5.0); // May crash if player is in different region!
});

// ✅ SAFE on Folia - runs on player's region thread
plugin.getScheduler().runAt(player, () -> {
    player.damage(5.0); // Always safe
});
```

---

## Scheduler Selection Guide

### Decision Tree

```
Are you working with a specific entity (Player, Mob, etc.)?
│
├─ YES → Use runAt(Entity entity, Runnable task)
│
└─ NO → Are you working with a specific location/block?
    │
    ├─ YES → Use runAt(Location location, Runnable task)
    │
    └─ NO → Is this global server logic (no entities/locations)?
        │
        ├─ YES → Use run(Runnable task) [Global Scheduler]
        │
        └─ Is this I/O or heavy computation?
            │
            └─ YES → Use runAsync(Runnable task)
```

### Scheduler Types

#### 1. Entity Scheduler (Recommended for entity operations)

```java
// Use when: Modifying entities, checking entity state
plugin.getScheduler().runAt(entity, () -> {
    entity.setHealth(20.0);
    entity.teleport(newLocation);
});
```

**On Folia**: Runs on the thread owning the entity  
**On Paper**: Runs on main thread

#### 2. Region/Location Scheduler (Recommended for block/world operations)

```java
// Use when: Breaking blocks, spawning particles at a location
Location loc = player.getLocation();
plugin.getScheduler().runAt(loc, () -> {
    world.spawnParticle(Particle.FLAME, loc, 10);
    world.setBlockData(loc, Material.AIR.createBlockData());
});
```

**On Folia**: Runs on the thread owning that region  
**On Paper**: Runs on main thread

#### 3. Global Scheduler (Use sparingly)

```java
// Use when: Global server operations, no specific entity/location
plugin.getScheduler().run(() -> {
    getLogger().info("Server uptime: " + getServer().getUptime());
});
```

**On Folia**: Runs on global region scheduler (limited concurrency)  
**On Paper**: Runs on main thread

> [!WARNING]
> Overusing the global scheduler defeats Folia's parallelization benefits. Always prefer context-aware methods when possible.

#### 4. Async Scheduler (For non-game-logic tasks)

```java
// Use when: Database queries, HTTP requests, file I/O
plugin.getScheduler().runAsync(() -> {
    String data = database.fetchPlayerData(uuid);
    
    // Switch back to appropriate region thread to apply changes
    plugin.getScheduler().runAt(player, () -> {
        applyPlayerData(player, data);
    });
});
```

---

## Context-Aware Scheduling

### Basic Pattern

```java
// Entity-based task
Player player = event.getPlayer();
plugin.getScheduler().runAt(player, () -> {
    // Safe to access player here
    player.sendMessage("Hello!");
    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
});
```

### Delayed Tasks

```java
// Delayed entity task
plugin.getScheduler().runAtLater(player, () -> {
    player.sendMessage("5 seconds have passed!");
}, 5, TimeUnit.SECONDS);

// Delayed location task
Location spawn = world.getSpawnLocation();
plugin.getScheduler().runAtLater(spawn, () -> {
    world.createExplosion(spawn, 4.0f, false, false);
}, 10, TimeUnit.SECONDS);
```

### Repeating Tasks

```java
// Repeating entity task (e.g., boss fight mechanics)
Entity boss = world.spawnEntity(location, EntityType.ENDER_DRAGON);
IonTask task = plugin.getScheduler().runAtTimer(boss, () -> {
    if (!boss.isValid()) {
        return; // Boss died, task will continue but do nothing
    }
    
    // Boss attack pattern
    boss.getWorld().spawnParticle(Particle.DRAGON_BREATH, 
        boss.getLocation(), 50);
}, 0, 5, TimeUnit.SECONDS);

// Cancel later
task.cancel();
```

---

## Migration from Paper

### Pattern 1: Direct Entity Access

#### Before (Paper-only)
```java
Bukkit.getScheduler().runTask(plugin, () -> {
    player.setHealth(20.0);
});
```

#### After (Folia-compatible)
```java
plugin.getScheduler().runAt(player, () -> {
    player.setHealth(20.0);
});
```

### Pattern 2: Location-Based Operations

#### Before (Paper-only)
```java
Bukkit.getScheduler().runTaskLater(plugin, () -> {
    world.setBlockData(location, Material.AIR.createBlockData());
}, 20L);
```

#### After (Folia-compatible)
```java
plugin.getScheduler().runAtLater(location, () -> {
    world.setBlockData(location, Material.AIR.createBlockData());
}, 1, TimeUnit.SECONDS);
```

### Pattern 3: Looping Through Players

#### Before (Paper-only)
```java
Bukkit.getScheduler().runTaskTimer(plugin, () -> {
    for (Player player : Bukkit.getOnlinePlayers()) {
        player.sendActionBar("Score: " + getScore(player));
    }
}, 0L, 20L);
```

#### After (Folia-compatible)
```java
// Schedule individual tasks per player
Bukkit.getScheduler().runTaskTimer(plugin, () -> {
    for (Player player : Bukkit.getOnlinePlayers()) {
        // Each player gets their own region-aware task
        plugin.getScheduler().runAt(player, () -> {
            player.sendActionBar("Score: " + getScore(player));
        });
    }
}, 0L, 20L);
```

> [!TIP]
> This pattern schedules a global task to iterate players, then creates individual region-aware tasks for each player. This ensures thread safety while maintaining compatibility.

---

## Common Patterns

### Pattern: Cross-Region Teleportation

```java
public void teleportPlayer(Player player, Location destination) {
    // Step 1: Run on player's current region thread
    plugin.getScheduler().runAt(player, () -> {
        
        // Step 2: Teleport (moves player to destination region)
        player.teleport(destination);
        
        // Step 3: Continue on destination region thread
        plugin.getScheduler().runAt(destination, () -> {
            destination.getWorld().spawnParticle(
                Particle.PORTAL, destination, 50
            );
        });
    });
}
```

### Pattern: Async Database + Sync Application

```java
public void loadPlayerData(Player player) {
    UUID uuid = player.getUniqueId();
    
    // Step 1: Fetch data asynchronously
    plugin.getScheduler().runAsync(() -> {
        PlayerData data = database.load(uuid);
        
        // Step 2: Apply on player's region thread
        plugin.getScheduler().runAt(player, () -> {
            applyData(player, data);
            player.sendMessage("§aData loaded!");
        });
    });
}
```

### Pattern: Multi-Location Effects

```java
public void createEffectLine(Location start, Location end) {
    List<Location> points = getLinePoints(start, end, 10);
    
    // Schedule effect at each point
    for (Location point : points) {
        plugin.getScheduler().runAt(point, () -> {
            point.getWorld().spawnParticle(
                Particle.FLAME, point, 1
            );
        });
    }
}
```

### Pattern: Entity Following

```java
public class FollowTask {
    private final IonTask task;
    
    public FollowTask(Entity follower, Entity target) {
        // Run on follower's thread repeatedly
        this.task = plugin.getScheduler().runAtTimer(follower, () -> {
            if (!follower.isValid() || !target.isValid()) {
                task.cancel();
                return;
            }
            
            // Move toward target
            Vector direction = target.getLocation()
                .subtract(follower.getLocation())
                .toVector()
                .normalize()
                .multiply(0.3);
            
            follower.setVelocity(direction);
        }, 0, 1, TimeUnit.SECONDS);
    }
    
    public void cancel() {
        task.cancel();
    }
}
```

---

## Troubleshooting

### Problem: ConcurrentModificationException

**Cause**: Accessing entity/block from wrong thread

**Solution**: Use context-aware scheduler
```java
// ❌ Wrong
plugin.getScheduler().run(() -> entity.remove());

// ✅ Correct
plugin.getScheduler().runAt(entity, () -> entity.remove());
```

### Problem: Task Not Running

**Cause**: Entity or location might be invalid/unloaded

**Solution**: Add validity checks
```java
plugin.getScheduler().runAt(entity, () -> {
    if (!entity.isValid()) {
        return; // Entity was removed
    }
    // Safe to use entity here
});
```

### Problem: Poor Performance on Folia

**Cause**: Too many tasks on global scheduler

**Solution**: Audit scheduler usage
```java
// ❌ Bottleneck - all on global scheduler
for (Player p : players) {
    plugin.getScheduler().run(() -> doSomething(p));
}

// ✅ Parallel - distributed across region threads
for (Player p : players) {
    plugin.getScheduler().runAt(p, () -> doSomething(p));
}
```

### Problem: Development Testing

**Question**: How do I test Folia-specific code?

**Answer**: 
1. Download Folia from [PaperMC](https://papermc.io/software/folia)
2. Use small world borders to create multiple regions:
   ```
   /worldborder set 1000
   /worldborder center 0 0
   ```
3. Spread players across regions to test concurrency
4. Monitor console for threading errors

---

## Best Practices

### ✅ Do

- Use `runAt(entity/location)` whenever working with game objects
- Run database/network operations with `runAsync()`
- Check entity validity before modifying
- Keep task code small and focused
- Cancel repeating tasks when no longer needed

### ❌ Don't

- Don't access entities from global scheduler on Folia
- Don't perform heavy computation on game threads
- Don't store entity references across ticks without validity checks
- Don't assume tasks execute in specific order on Folia
- Don't use deprecated Bukkit scheduler directly

---

## Examples

For complete working examples, see:
- [EXAMPLES.md](EXAMPLES.md) - Code examples
- [GETTING_STARTED.md](GETTING_STARTED.md) - Tutorial

---

## Further Reading

- [Folia GitHub](https://github.com/PaperMC/Folia)
- [PaperMC Documentation](https://docs.papermc.io/)
- [IonAPI Technical Analysis](TECHNICAL_ANALYSIS.md)
