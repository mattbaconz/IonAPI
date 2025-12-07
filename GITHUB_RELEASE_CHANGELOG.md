# 🚀 IonAPI v1.2.0 - Performance & Utilities Release

A feature-packed update focused on **performance optimization** and **developer utilities** to make plugin development faster and easier.

---

## ⚡ Performance Improvements

### Reflection Caching (10-50x faster ORM)
- Automatic entity metadata caching eliminates repeated reflection calls
- Zero configuration required - works automatically
- Massive performance boost for database operations

### Batch Operations
- Bulk insert/update/delete operations
- 10-50x faster than individual operations
- Configurable batch sizes (default: 1000)
- Async execution support

```java
BatchOperation.BatchResult result = database.batch(PlayerStats.class)
    .insertAll(stats)
    .batchSize(500)
    .execute();
// Inserted 1000 records in 50ms vs 2000ms individually!
```

---

## ✨ New Utilities

### CooldownManager
Thread-safe player cooldown management with automatic cleanup.

```java
CooldownManager cooldowns = CooldownManager.create("teleport");
if (cooldowns.isOnCooldown(player.getUniqueId())) {
    long remaining = cooldowns.getRemainingTime(player.getUniqueId(), TimeUnit.SECONDS);
    player.sendMessage("Wait " + remaining + "s!");
    return;
}
cooldowns.setCooldown(player.getUniqueId(), 30, TimeUnit.SECONDS);
```

### RateLimiter
Sliding window rate limiting for spam prevention.

```java
RateLimiter limiter = RateLimiter.create("chat", 5, 10, TimeUnit.SECONDS);
if (!limiter.tryAcquire(player.getUniqueId())) {
    player.sendMessage("Slow down!");
    return;
}
```

### MessageBuilder
Fluent MiniMessage builder with templates.

```java
MessageBuilder.of("<green>Hello, <player>!")
    .placeholder("player", player.getName())
    .send(player);

MessageBuilder.of("<gold><bold>LEVEL UP!")
    .subtitle("<gray>Level <level>")
    .sendTitle(player);
```

### IonScoreboard
Easy scoreboard creation with MiniMessage.

```java
IonScoreboard board = IonScoreboard.builder()
    .title("<gradient:gold:yellow><bold>My Server")
    .line(15, "<gray>Welcome, <white>{player}")
    .line(13, "<gold>Coins: <yellow>{coins}")
    .placeholder("player", p -> p.getName())
    .placeholder("coins", p -> String.valueOf(getCoins(p)))
    .build();
board.show(player);
```

### IonBossBar
Boss bar management with dynamic updates.

```java
IonBossBar bar = IonBossBar.builder()
    .title("<gradient:red:orange>Event: {progress}%")
    .color(BossBar.Color.RED)
    .progress(0.5f)
    .build();
bar.show(player);
bar.setProgress(0.75f);
```

### Metrics
Lightweight performance monitoring.

```java
Metrics.increment("player.join");
Metrics.time("database.query", () -> db.findAll(PlayerData.class));
double avgTime = Metrics.getAverageTime("database.query");
```

---

## 📊 Module Sizes

Still ultra-lightweight! Only +21 KB for all new features:

| Module | Size |
|--------|------|
| ion-api | 23.8 KB |
| ion-database | 52.7 KB |
| ion-ui | 10.5 KB |
| **Total** | **273 KB** |

---

## 📚 Documentation

- ✅ Complete API Reference updated
- ✅ New examples for all v1.2.0 features
- ✅ Comprehensive Javadocs
- ✅ Quick Reference cheat sheet
- ✅ `V120FeaturesExample.java` with working code

---

## 🔧 Installation

### Gradle (Kotlin DSL)
```kotlin
dependencies {
    implementation("com.github.mattbaconz:IonAPI:1.2.0")
}

tasks.shadowJar {
    relocate("com.ionapi", "your.plugin.libs.ionapi")
}
```

### Maven
```xml
<dependency>
    <groupId>com.github.mattbaconz</groupId>
    <artifactId>IonAPI</artifactId>
    <version>1.2.0</version>
</dependency>
```

---

## 🔄 Migration from v1.1.0

**No breaking changes!** v1.2.0 is fully backward compatible.

Simply update your dependency version and start using the new features.

---

## 📦 Release Artifacts

Download these JARs for manual installation:
- `ion-api-1.2.0.jar` (23.8 KB)
- `ion-paper-1.2.0-all.jar` (13.2 KB)
- `ion-folia-1.2.0-all.jar` (12.9 KB)

Or use JitPack (recommended):
```kotlin
implementation("com.github.mattbaconz:IonAPI:1.2.0")
```

---

## 🐛 Bug Fixes

- Fixed BossBar API compatibility with Adventure API
- Improved error handling in batch operations
- Enhanced thread safety in cooldown and rate limiter

---

## 💬 Support

- **Discord**: https://discord.com/invite/VQjTVKjs46
- **GitHub**: https://github.com/mattbaconz/IonAPI
- **Ko-fi**: https://ko-fi.com/mbczishim/tip
- **PayPal**: https://www.paypal.com/paypalme/MatthewWatuna

---

## 🙏 Thank You!

Thank you to everyone who provided feedback and helped test v1.2.0!

**Full Changelog**: https://github.com/mattbaconz/IonAPI/blob/main/CHANGELOG.md
