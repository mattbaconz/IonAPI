# 🌟 Folia Compatibility Guide

IonAPI makes Folia development easy with automatic region-aware scheduling.

---

## 🎯 What is Folia?

**Folia** is Paper's multi-threaded fork that divides the world into **regions**, with each region running on its own thread for better performance.

**Key Difference:**
- **Paper**: Single main thread
- **Folia**: Multiple region threads (parallel processing)

---

## ⚡ The Easy Way (IonAPI)

IonAPI handles Folia complexity automatically!

### ✅ Entity-Specific Tasks

```java
// ✅ Automatically runs on player's region thread
getScheduler().runAt(player, () -> {
    player.damage(5.0);
    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
});

// ✅ Delayed
getScheduler().runAtLater(player, () -> {
    player.sendMessage("5 seconds passed!");
}, 5, TimeUnit.SECONDS);

// ✅ Repeating
getScheduler().runAtTimer(player, () -> {
    player.sendActionBar("Score: " + getScore(player));
}, 0, 1, TimeUnit.SECONDS);
```

### ✅ Location-Specific Tasks

```java
// ✅ Automatically runs on location's region thread
Location spawn = world.getSpawnLocation();
getScheduler().runAt(spawn, () -> {
    world.spawnParticle(Particle.FLAME, spawn, 10);
    world.setBlockData(spawn, Material.DIAMOND_BLOCK.createBlockData());
});
```

---

## 🔗 Task Chains (Folia-Optimized)

```java
TaskChain.create(plugin)
    .async(() -> database.loadPlayerData(uuid))
    .syncAt(player, data -> {
        // Automatically runs on player's region thread
        player.setLevel(data.level);
        player.setHealth(data.health);
    })
    .delay(2, TimeUnit.SECONDS)
    .syncAt(player, () -> {
        player.sendMessage("Welcome back!");
    })
    .execute();
```

---

## ⚠️ Common Mistakes

### ❌ Wrong (May crash on Folia)

```java
// Global scheduler - may run on wrong thread
getScheduler().run(() -> {
    player.damage(5.0); // ❌ Unsafe!
});

// Accessing entity from wrong thread
getScheduler().runAsync(() -> {
    player.sendMessage("Hello"); // ❌ Crash!
});
```

### ✅ Correct (Folia-safe)

```java
// Entity-specific scheduler
getScheduler().runAt(player, () -> {
    player.damage(5.0); // ✅ Safe!
});

// Async then sync
getScheduler().runAsync(() -> {
    String data = fetchFromDatabase();
    
    getScheduler().runAt(player, () -> {
        player.sendMessage(data); // ✅ Safe!
    });
});
```

---

## 🎯 Best Practices

### 1. Use Context-Aware Methods

```java
// ✅ Good - Folia-optimized
getScheduler().runAt(player, () -> modifyPlayer(player));
getScheduler().runAt(location, () -> modifyWorld(location));

// ⚠️ Okay - Works but not optimized
getScheduler().run(() -> doGlobalTask());
```

### 2. Use TaskChains for Complex Workflows

```java
// ✅ Clean and Folia-safe
TaskChain.create(plugin)
    .async(() -> loadData())
    .syncAt(player, data -> applyData(player, data))
    .execute();
```

### 3. Batch Operations Per Region

```java
// ✅ Efficient - Each player on their own thread
for (Player player : Bukkit.getOnlinePlayers()) {
    getScheduler().runAt(player, () -> {
        updatePlayer(player);
    });
}
```

---

## 📊 Performance Comparison

| Operation | Paper | Folia (IonAPI) |
|-----------|-------|----------------|
| Single player update | Fast | Fast |
| 100 players update | Slow (sequential) | Fast (parallel) |
| World modifications | Slow (sequential) | Fast (parallel) |
| Global tasks | Fast | Moderate |

**Tip:** Use `runAt()` methods to leverage Folia's parallelism!

---

## 🧪 Testing on Folia

### 1. Download Folia

Get it from [PaperMC](https://papermc.io/software/folia)

### 2. Test Your Plugin

```bash
# Start Folia server
java -jar folia.jar

# Test with multiple players in different regions
# Verify no threading errors in console
```

### 3. Common Issues

**Issue:** "Cannot access entity from this thread"
**Fix:** Use `runAt(entity, ...)` instead of `run(...)`

**Issue:** "Slow performance on Folia"
**Fix:** Use context-aware methods instead of global scheduler

---

## 💡 Quick Tips

1. ✅ **Always use `runAt()`** when modifying entities/locations
2. ✅ **Use TaskChains** for async/sync workflows
3. ✅ **Test on both Paper and Folia**
4. ✅ **Avoid global scheduler** when possible
5. ✅ **Batch operations** per region for best performance

---

## 📚 More Resources

- [Getting Started](GETTING_STARTED.md) - Complete tutorial
- [API Reference](API_REFERENCE.md) - Full API docs
- [Examples](EXAMPLES.md) - Code examples

---

**Need help?** Join our [Discord](https://discord.com/invite/VQjTVKjs46)!
