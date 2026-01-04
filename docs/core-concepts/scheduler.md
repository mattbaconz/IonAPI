# Scheduler (Thread-Safe, Folia-Compatible)

The IonAPI scheduler provides a unified interface that works on both Paper and Folia.

### Synchronous Tasks

```java
// Run immediately on main thread
getScheduler().run(() -> {
    player.sendMessage("<green>This runs on main thread!");
});

// Run after delay
getScheduler().runLater(() -> {
    player.sendMessage("<green>5 seconds have passed!");
}, 5, TimeUnit.SECONDS);

// Run repeatedly
IonTask task = getScheduler().runTimer(() -> {
    player.sendMessage("<green>This runs every second!");
}, 0, 1, TimeUnit.SECONDS);

// Cancel task later
task.cancel();
```

### Asynchronous Tasks

```java
// Run async (for database, API calls, file I/O)
getScheduler().runAsync(() -> {
    // This runs off the main thread
    String data = fetchFromDatabase();
    
    // Switch back to main thread for Bukkit API
    getScheduler().run(() -> {
        player.sendMessage("<green>Data: " + data);
    });
});
```

### Folia-Aware Scheduling

For optimal performance on Folia, use context-aware scheduling:

```java
// Entity-specific tasks (runs on entity's region thread)
Player player = event.getPlayer();
getScheduler().runAt(player, () -> {
    player.damage(5.0);
    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
});

// Location-specific tasks (runs on location's region thread)
Location spawn = world.getSpawnLocation();
getScheduler().runAt(spawn, () -> {
    world.spawnParticle(Particle.FLAME, spawn, 10);
    world.setBlockData(spawn, Material.DIAMOND_BLOCK.createBlockData());
});

// Delayed context-aware tasks
getScheduler().runAtLater(player, () -> {
    player.sendMessage("<yellow>5 seconds have passed!");
}, 5, TimeUnit.SECONDS);

// Repeating context-aware tasks
IonTask task = getScheduler().runAtTimer(player, () -> {
    player.sendActionBar("<gold>Score: " + getScore(player));
}, 0, 1, TimeUnit.SECONDS);
```
