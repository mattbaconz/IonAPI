# 🔧 Troubleshooting Guide

**Version**: 1.3.0 | Common issues and solutions when using IonAPI.

---

## 📦 Dependency Issues

### NoClassDefFoundError: com/ionapi/...

**Problem:** Plugin throws `NoClassDefFoundError` or `ClassNotFoundException` for IonAPI classes.

**Solution:** IonAPI must be shaded into your plugin JAR.

```kotlin
plugins {
    id("com.gradleup.shadow") version "8.3.0"
}

dependencies {
    implementation("com.github.mattbaconz:IonAPI:1.3.0")
}

tasks.shadowJar {
    relocate("com.ionapi", "your.plugin.libs.ionapi")
}
```

**Verify:** Check your JAR contains `com/ionapi/` classes:
```bash
jar tf your-plugin.jar | grep ionapi
```

---

### NoClassDefFoundError: com/zaxxer/hikari/HikariConfig

**Problem:** Database module throws HikariCP-related errors.

**Solution:** HikariCP is included in IonAPI. Make sure you're using `shadowJar` task:

```bash
./gradlew shadowJar
# NOT: ./gradlew build jar
```

---

### Dependency Resolution Failed

**Problem:** Gradle can't resolve `com.github.mattbaconz:IonAPI`.

**Solution:** Add JitPack repository:

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}
```

**Also check:**
- Internet connection
- JitPack status: https://jitpack.io/#mattbaconz/IonAPI
- Correct version tag exists

---

## 🎮 Runtime Issues

### Plugin Fails to Enable

**Problem:** Plugin throws exception during `onEnable()`.

**Checklist:**
1. ✅ Check server console for full stack trace
2. ✅ Verify all required dependencies are shaded
3. ✅ Ensure config.yml exists and is valid YAML
4. ✅ Check database connection settings

**Common causes:**
- Missing `config.yml` in resources folder
- Invalid YAML syntax (tabs instead of spaces)
- Database not running or wrong credentials

---

### GUI Items Not Clickable

**Problem:** GUI opens but clicking items does nothing.

**Solution:** Make sure you're passing a click handler:

```java
// ❌ Wrong - No handler
.item(10, diamondItem)

// ✅ Correct - With handler
.item(10, diamondItem, click -> {
    click.getPlayer().sendMessage("Clicked!");
})
```

---

### GUI Item Duping

**Problem:** Players can duplicate items in GUIs.

**Solution:** IonAPI prevents this by default. If you're seeing dupes:

1. Don't use `.allowTake(true)` or `.allowPlace(true)` unless necessary
2. Don't call `event.setCancelled(false)` in handlers
3. Test all shift-click and number key interactions

See [SECURITY.md](../SECURITY.md) for details.

---

### Scoreboard Flickering

**Problem:** Scoreboard flickers when updating.

**Solution (v1.3.0):** IonScoreboard now uses per-line updates that eliminate flashing:

```java
// ✅ v1.3.0 - No flashing! Uses builder pattern
IonScoreboard board = IonScoreboard.builder()
    .title("<gold>Stats")
    .line(15, "<yellow>Level: {level}")
    .placeholder("level", p -> String.valueOf(p.getLevel()))
    .updateInterval(20) // Auto-updates smoothly
    .build();

board.show(player);

// Update a single line (no flicker)
board.setLine(player, 15, "<yellow>Level: 10");
```

---

### AsyncCatcher: Async Entity Modify

**Problem:** `java.lang.IllegalStateException: Asynchronous entity modify!`

**Solution:** Use platform-aware scheduling:

```java
// ❌ Wrong - Bukkit API from async thread
getScheduler().runAsync(() -> {
    player.teleport(location);  // CRASH!
});

// ✅ Correct - Switch to sync for Bukkit API
getScheduler().runAsync(() -> {
    Data data = loadData();
    getScheduler().run(() -> {
        player.teleport(location);  // Safe!
    });
});

// ✅ Even better - Use TaskChain
TaskChain.create(plugin)
    .async(() -> loadData())
    .syncAt(player, data -> player.teleport(location))
    .execute();
```

---

### Folia: RegionizedTaskQueue Error

**Problem:** `Cannot schedule for entity/location` on Folia.

**Solution:** Use entity/location-aware scheduling:

```java
// ❌ Wrong - Global scheduler
getScheduler().run(() -> player.damage(5.0));

// ✅ Correct - Entity-specific
getScheduler().runAt(player, () -> player.damage(5.0));

// ✅ Correct - Location-specific
getScheduler().runAt(location, () -> world.setBlockData(...));
```

---

## 💾 Database Issues

### Connection Timeout

**Problem:** Database operations time out or hang.

**Solutions:**

1. **Check connection settings:**
```java
IonDatabase db = IonDatabase.builder()
    .host("localhost")  // Verify host
    .port(3306)         // Verify port
    .build();
```

2. **Increase pool size for high traffic:**
```java
.poolSize(20)  // Default is 10
```

3. **Check MySQL/PostgreSQL is running:**
```bash
# MySQL
mysql -u user -p -e "SELECT 1"

# PostgreSQL
psql -U user -c "SELECT 1"
```

---

### Table Already Exists

**Problem:** `Table 'players' already exists` error.

**Solution:** This is safe to ignore - `createTable()` is idempotent. The table won't be overwritten.

To suppress the message, check first:
```java
// Only create if needed (IonAPI handles this internally)
db.createTable(PlayerData.class);
```

---

### Entity Not Found

**Problem:** `db.find()` returns `null` unexpectedly.

**Checklist:**
1. ✅ Verify the entity exists in database
2. ✅ Check primary key value is correct
3. ✅ Ensure entity class has `@Table` annotation
4. ✅ Verify column names match database

```java
PlayerData data = db.find(PlayerData.class, uuid);
if (data == null) {
    // Entity doesn't exist - create new one
    data = new PlayerData(uuid, player.getName());
    db.save(data);
}
```

---

## ⚙️ Configuration Issues

### Config Not Loading

**Problem:** Config values return defaults or null.

**Checklist:**
1. ✅ File exists: `plugins/YourPlugin/config.yml`
2. ✅ Valid YAML syntax (use online YAML validator)
3. ✅ Correct path in code: `config.getString("section.key")`
4. ✅ No tabs (use spaces only in YAML)

**Debug:**
```java
IonConfig config = getConfigProvider().getConfig();
getLogger().info("Keys: " + config.getKeys(false));
```

---

### Hot-Reload Not Working

**Problem:** Config changes not detected automatically.

**Solution:** Ensure `HotReloadConfig` is started:

```java
HotReloadConfig config = HotReloadConfig.create(this, "config.yml")
    .onReload(cfg -> {
        getLogger().info("Config reloaded!");
        loadSettings(cfg);
    })
    .start();  // Don't forget this!
```

**Also check:**
- File is being saved (not just edited in memory)
- WatchService is supported on your OS

---

## 🔌 Integration Issues

### Vault Not Found

**Problem:** Economy operations fail with "Vault not found".

**Solution:** Ensure Vault is installed and an economy plugin is loaded:

1. Install Vault: https://www.spigotmc.org/resources/vault.34315/
2. Install economy provider (EssentialsX, CMI, etc.)
3. Make `Vault` a dependency in `plugin.yml`:

```yaml
depend:
  - Vault
# or softdepend if optional
softdepend:
  - Vault
```

---

### PlaceholderAPI Not Registering

**Problem:** Custom placeholders not showing up.

**Solution:**
```java
// Ensure PAPI is loaded first
if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
    IonPlaceholder.register(this, "myplugin", (player, params) -> {
        return switch (params) {
            case "level" -> String.valueOf(getLevel(player));
            default -> null;
        };
    });
}
```

---

## 🛠️ Build Issues

### Shadow Plugin Not Found

**Problem:** `Plugin [id: 'com.gradleup.shadow'] was not found`

**Solution:** Add the plugin to `build.gradle.kts`:

```kotlin
plugins {
    java
    id("com.gradleup.shadow") version "8.3.0"
}
```

---

### JAR Too Large

**Problem:** Shaded JAR is unexpectedly large (>10MB).

**Solutions:**

1. **Enable minimize:**
```kotlin
tasks.shadowJar {
    minimize()
}
```

2. **Exclude test dependencies:**
```kotlin
configurations {
    implementation {
        exclude(group = "org.mockito")
        exclude(group = "org.junit")
    }
}
```

3. **Check what's included:**
```bash
jar tf your-plugin.jar | sort | uniq -c | sort -rn | head -20
```

---

### Relocation Conflicts

**Problem:** Multiple plugins conflict with different IonAPI versions.

**Solution:** Always relocate:

```kotlin
tasks.shadowJar {
    // Unique package per plugin
    relocate("com.ionapi", "com.yourname.yourplugin.libs.ionapi")
}
```

---

## 📊 Performance Issues

### High Memory Usage

**Problem:** Plugin consumes excessive memory.

**Solutions:**

1. **Clean up resources in `onDisable()`:**
```java
@Override
public void onDisable() {
    scoreboards.values().forEach(IonScoreboard::destroy);
    bossBars.values().forEach(IonBossBar::hideAll);
    database.disconnect();
}
```

2. **Use caching wisely:**
```java
@Table("players")
@Cacheable(ttl = 60)  // Cache for 60 seconds, not forever
public class PlayerData { }
```

3. **Don't store full player objects:**
```java
// ❌ Wrong - Keeps player in memory
Map<Player, Data> data = new HashMap<>();

// ✅ Correct - Use UUID
Map<UUID, Data> data = new HashMap<>();
```

---

### Slow Database Queries

**Problem:** Database operations are slow.

**Solutions:**

1. **Use async operations:**
```java
db.findAsync(PlayerData.class, uuid)
    .thenAccept(data -> process(data));
```

2. **Batch operations:**
```java
db.batch(PlayerData.class)
    .insertAll(playerList)
    .execute();
```

3. **Add indexes in annotations:**
```java
@Column(index = true)
private String name;
```

---

## 🆘 Getting More Help

If your issue isn't listed here:

1. **Check the logs** - Full stack traces are essential
2. **Search existing issues**: https://github.com/mattbaconz/IonAPI/issues
3. **Join Discord**: https://discord.com/invite/VQjTVKjs46
4. **Open an issue** with:
   - IonAPI version
   - Server software & version
   - Full error message/stack trace
   - Minimal code to reproduce

---

**Still stuck?** Join our [Discord](https://discord.com/invite/VQjTVKjs46) for live support!
