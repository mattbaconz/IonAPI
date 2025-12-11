# 🚀 Upgrade Guide

How to upgrade between IonAPI versions.

---

## Current Version: 1.3.0

---

## Upgrading to v1.3.0 from v1.2.x

### What's New
- 📊 **Scoreboard Flashing Fixed** - Complete rewrite with per-line updates
- ✨ **Animated Scoreboard Lines** - Text cycling animations
- 📦 **ConfirmationGui** - Simple yes/no dialog class
- 💀 **Skull Textures** - Base64 texture support for player heads
- 🎨 **Leather Armor Colors** - Color support for leather items
- 🧪 **Potion Effects** - Fluent potion builder methods
- 🔒 **GUI Security** - Fixed item duplication exploits

### Breaking Changes

#### 1. IonScoreboard API Changes

The `IonScoreboard` has been completely rewritten for better performance:

```java
// Old (v1.2.x) - create() static method
IonScoreboard board = IonScoreboard.create(player)
    .title("Title")
    .show();

// New (v1.3.0) - builder() pattern
IonScoreboard board = IonScoreboard.builder()
    .title("Title")
    .line(15, "Line text")
    .placeholder("key", p -> "value")
    .updateInterval(20) // NEW: Auto-update
    .build();

board.show(player);
```

#### 2. New Features You Can Use

**Animated Scoreboard Lines:**
```java
IonScoreboard.builder()
    .title("<gold>Server")
    .animatedLine(15, 10, "Frame 1", "Frame 2", "Frame 3") // Cycles every 10 ticks
    .build();
```

**ConfirmationGui:**
```java
ConfirmationGui.create()
    .title("<red>⚠ Confirm")
    .message("Delete all data?")
    .onConfirm(player -> deleteData())
    .danger() // Red styling
    .open(player);
```

**Item Builder Enhancements:**
```java
// Skull textures
IonItem.builder(Material.PLAYER_HEAD)
    .skullTexture("eyJ0ZXh0dXJlcyI6Li4u")
    .build();

// Leather colors
IonItem.builder(Material.LEATHER_CHESTPLATE)
    .color(Color.RED)
    .build();

// Potion effects
IonItem.builder(Material.POTION)
    .potionType(PotionType.STRENGTH)
    .potionEffect(PotionEffectType.SPEED, 200, 1)
    .potionColor(Color.PURPLE)
    .build();
```

### Steps
1. Update dependency version:
```kotlin
implementation("com.github.mattbaconz:IonAPI:1.4.0")
```
2. If using `IonScoreboard.create()`, migrate to builder pattern
3. Rebuild: `./gradlew clean shadowJar`
4. Replace JAR on server

---

## Upgrading to v1.2.6 from v1.2.5

## Upgrading to v1.2.5 from v1.2.0

### What's New
- ⏱️ **CooldownManager** - Thread-safe player cooldowns
- 🚦 **RateLimiter** - Sliding window rate limiting
- 💬 **MessageBuilder** - Fluent MiniMessage builder with templates
- 📊 **IonScoreboard** improvements
- 📈 **IonBossBar** improvements
- 📉 **Metrics** - Lightweight performance monitoring
- ⚡ **BatchOperation** - 10-50x faster bulk database operations
- 🔄 **ReflectionCache** - Cached entity metadata for ORM

### Breaking Changes
None - all new features are additive.

### New Features You Can Use

#### CooldownManager
```java
CooldownManager cooldowns = CooldownManager.create("teleport");

if (cooldowns.isOnCooldown(player.getUniqueId())) {
    long remaining = cooldowns.getRemainingTime(player.getUniqueId(), TimeUnit.SECONDS);
    player.sendMessage("Wait " + remaining + " seconds!");
    return;
}

// Do action
player.teleport(destination);
cooldowns.setCooldown(player.getUniqueId(), 30, TimeUnit.SECONDS);
```

#### RateLimiter
```java
RateLimiter chatLimiter = RateLimiter.create("chat", 5, 10, TimeUnit.SECONDS);

if (!chatLimiter.tryAcquire(player.getUniqueId())) {
    player.sendMessage("You're sending messages too fast!");
    return;
}
```

#### MessageBuilder
```java
MessageBuilder.of("<green>Welcome, <player>!")
    .placeholder("player", player.getName())
    .send(player);

// With title
MessageBuilder.of("<gold><bold>LEVEL UP!")
    .subtitle("<gray>You reached level <level>")
    .placeholder("level", "10")
    .sendTitle(player);
```

#### Batch Operations
```java
database.batch(PlayerData.class)
    .insertAll(newPlayers)
    .batchSize(500)
    .execute();
```

### Steps
1. Update dependency version
2. Rebuild plugin
3. (Optional) Refactor to use new utilities

---

## Upgrading to v1.2.0 from v1.1.x

### What's New
- 🔒 Enhanced GUI security (anti-duping)
- 📊 IonScoreboard dynamic lines
- 🔗 TaskChain improvements
- 💾 Database caching with `@Cacheable`

### Breaking Changes

#### 1. Database Method Renames

```java
// Old (v1.1.x)
database.register(PlayerData.class);
database.close();

// New (v1.2.0+)
database.createTable(PlayerData.class);
database.disconnect();
```

#### 2. TransactionResult Changes

```java
// Old (v1.1.x)
TransactionResult result = economy.withdraw(uuid, 100);
if (result.getType() == TransactionType.SUCCESS) { }

// New (v1.2.0+)
TransactionResult result = economy.withdraw(uuid, 100);
if (result.isSuccess()) { }
```

#### 3. GUI Security (Behavior Change)

GUIs now cancel all clicks by default. If you need players to take items:

```java
IonGui.builder()
    .allowTake(true)  // Explicitly enable
    .build();
```

### Migration Steps

1. Update dependency version
2. Find and replace:
   - `database.register(` → `database.createTable(`
   - `database.close()` → `database.disconnect()`
   - `result.getType() == TransactionType.SUCCESS` → `result.isSuccess()`
3. Test all GUIs for expected click behavior
4. Rebuild and test thoroughly

---

## Upgrading to v1.1.0 from v1.0.x

### What's New
- 💰 Economy module with Vault integration
- 🔴 Redis pub/sub support
- 🔥 Hot-reload configuration
- 👻 Packet-based NPCs
- 💉 Dependency injection

### Breaking Changes
None - v1.1.0 was additive.

### Steps
1. Update dependency version
2. (Optional) Migrate from third-party economy to `ion-economy`
3. Rebuild plugin

---

## General Upgrade Checklist

When upgrading any version:

- [ ] Read the [CHANGELOG.md](../CHANGELOG.md) for the new version
- [ ] Back up your server and plugin data
- [ ] Update version in `build.gradle.kts`:
  ```kotlin
  implementation("com.github.mattbaconz:IonAPI:NEW_VERSION")
  ```
- [ ] Clean rebuild:
  ```bash
  ./gradlew clean shadowJar
  ```
- [ ] Test on a development server first
- [ ] Check for deprecation warnings in build output
- [ ] Verify all plugin features work correctly
- [ ] Deploy to production

---

## Version Compatibility Matrix

| IonAPI | Java | Paper | Folia | Vault |
|--------|------|-------|-------|-------|
| 1.3.0  | 17+  | 1.19+ | ✅    | 1.7+  |
| 1.2.6  | 17+  | 1.19+ | ✅    | 1.7+  |
| 1.2.5  | 17+  | 1.19+ | ✅    | 1.7+  |
| 1.2.0  | 17+  | 1.19+ | ✅    | 1.7+  |
| 1.1.0  | 17+  | 1.19+ | ✅    | 1.7+  |
| 1.0.0  | 17+  | 1.19+ | ⚠️    | 1.7+  |

---

## Rollback Procedure

If you need to rollback:

1. Stop the server
2. Replace plugin JAR with previous version
3. Check if database schema changes need reverting (usually not)
4. Restore any config backups if structure changed
5. Start server
6. Report the issue: https://github.com/mattbaconz/IonAPI/issues

---

## Staying Updated

### Watch for Updates

1. **Star the repo**: https://github.com/mattbaconz/IonAPI
2. **Watch releases**: Click "Watch" → "Custom" → "Releases"
3. **Join Discord**: https://discord.com/invite/VQjTVKjs46

### Check Current Version

In your plugin:
```java
getLogger().info("IonAPI is working!");
// Check JitPack for latest: https://jitpack.io/#mattbaconz/IonAPI
```

---

## Need Help?

- **Discord**: https://discord.com/invite/VQjTVKjs46
- **GitHub Issues**: https://github.com/mattbaconz/IonAPI/issues
- **Documentation**: https://github.com/mattbaconz/IonAPI/tree/main/docs

---

**Happy upgrading!** 🎉
