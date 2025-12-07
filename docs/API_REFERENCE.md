# API Reference

## Core Interfaces

### IonPlugin

The main entry point for all Ion plugins.

```java
public interface IonPlugin {
    void onEnable();
    void onDisable();
    String getName();
    Logger getLogger();
    IonScheduler getScheduler();
    CommandRegistry getCommandRegistry();
    ConfigurationProvider getConfigProvider();
    EventBus getEventBus();
    File getDataFolder();
    String getPlatform();
}
```

**Methods:**

- `onEnable()` - Called when the plugin is enabled
- `onDisable()` - Called when the plugin is disabled  
- `getName()` - Returns the plugin name
- `getLogger()` - Returns the plugin logger
- `getScheduler()` - Returns the unified scheduler
- `getCommandRegistry()` - Returns the command registry
- `getConfigProvider()` - Returns the configuration provider
- `getEventBus()` - Returns the event bus
- `getDataFolder()` - Returns the plugin's data folder
- `getPlatform()` - Returns the platform name ("paper" or "folia")

---

## Scheduler API

### IonScheduler

Unified thread-safe scheduler for Paper and Folia.

```java
public interface IonScheduler {
    IonTask run(Runnable task);
    IonTask runAsync(Runnable task);
    IonTask runLater(Runnable task, long delay, TimeUnit unit);
    IonTask runLaterAsync(Runnable task, long delay, TimeUnit unit);
    IonTask runTimer(Runnable task, long delay, long period, TimeUnit unit);
    IonTask runTimerAsync(Runnable task, long delay, long period, TimeUnit unit);
    void cancelAll();
    boolean isMainThread();
}
```

**Examples:**

```java
// Run immediately on main thread
scheduler.run(() -> {
    // Your code here
});

// Run async (off main thread)
scheduler.runAsync(() -> {
    // Database query, API call, etc.
});

// Run after 5 seconds
scheduler.runLater(() -> {
    player.sendMessage("5 seconds passed!");
}, 5, TimeUnit.SECONDS);

// Run every second, starting after 0 seconds
IonTask task = scheduler.runTimer(() -> {
    // Repeating task
}, 0, 1, TimeUnit.SECONDS);

// Cancel the task later
task.cancel();
```

### IonTask

Represents a scheduled task.

```java
public interface IonTask {
    int getId();
    boolean isCancelled();
    void cancel();
    boolean isRunning();
    Object getOwner();
}
```

---

## Command API

### IonCommand

Base interface for all commands.

```java
public interface IonCommand {
    boolean execute(CommandContext context);
    String getName();
    String getDescription();
    String getUsage();
    String getPermission();
}
```

**Example Implementation:**

```java
public class HelloCommand implements IonCommand {
    
    @Override
    public boolean execute(CommandContext ctx) {
        if (!ctx.hasPermission(getPermission())) {
            ctx.reply("§cNo permission!");
            return false;
        }
        
        String name = ctx.getArg(0, "World");
        ctx.reply("§aHello, " + name + "!");
        return true;
    }
    
    @Override
    public String getName() {
        return "hello";
    }
    
    @Override
    public String getDescription() {
        return "Greets a player";
    }
    
    @Override
    public String getUsage() {
        return "/hello [name]";
    }
    
    @Override
    public String getPermission() {
        return "myplugin.hello";
    }
}
```

### CommandContext

Provides context for command execution.

```java
public interface CommandContext {
    Object getSender();
    List<String> getArgs();
    String getArg(int index);
    String getArg(int index, String defaultValue);
    int getArgCount();
    void reply(String message);
    boolean hasPermission(String permission);
}
```

### CommandRegistry

Manages command registration.

```java
public interface CommandRegistry {
    void register(IonCommand command);
    boolean unregister(String name);
    IonCommand getCommand(String name);
    List<IonCommand> getCommands();
    void unregisterAll();
}
```

**Example:**

```java
@Override
public void onEnable() {
    CommandRegistry registry = getCommandRegistry();
    registry.register(new HelloCommand());
    registry.register(new GiveItemCommand());
}
```

---

## Configuration API

### IonConfig

Type-safe configuration interface.

```java
public interface IonConfig {
    Object get(String path);
    Object get(String path, Object defaultValue);
    String getString(String path);
    String getString(String path, String defaultValue);
    int getInt(String path);
    int getInt(String path, int defaultValue);
    double getDouble(String path);
    double getDouble(String path, double defaultValue);
    boolean getBoolean(String path);
    boolean getBoolean(String path, boolean defaultValue);
    List<?> getList(String path);
    List<String> getStringList(String path);
    void set(String path, Object value);
    boolean contains(String path);
    Set<String> getKeys(boolean deep);
    Map<String, Object> getSection(String path);
    void save();
    void reload();
}
```

**Example config.yml:**

```yaml
database:
  host: localhost
  port: 3306
  name: mydb
  
messages:
  welcome: "§aWelcome to the server!"
  goodbye: "§cSee you later!"
  
features:
  - anti-cheat
  - economy
  - shops
```

**Example Usage:**

```java
IonConfig config = getConfigProvider().getConfig();

// Get values
String host = config.getString("database.host", "localhost");
int port = config.getInt("database.port", 3306);
boolean enabled = config.getBoolean("enabled", true);

// Get lists
List<String> features = config.getStringList("features");

// Set values
config.set("last-updated", System.currentTimeMillis());
config.save();

// Reload from disk
config.reload();
```

### ConfigurationProvider

Manages multiple config files.

```java
public interface ConfigurationProvider {
    IonConfig loadConfig(String fileName);
    IonConfig createConfig(String fileName, IonConfig defaults);
    IonConfig getConfig();
    void saveAll();
    void reloadAll();
}
```

**Example:**

```java
// Get main config
IonConfig config = getConfigProvider().getConfig();

// Load custom config
IonConfig messages = getConfigProvider().loadConfig("messages.yml");

// Save all configs
getConfigProvider().saveAll();
```

---

## Event API

### IonEvent

Base interface for custom events.

```java
public interface IonEvent {
    String getEventName();
    boolean isCancelled();
    void setCancelled(boolean cancelled);
    boolean isCancellable();
}
```

**Example Custom Event:**

```java
public class PlayerBalanceChangeEvent implements IonEvent {
    private final Player player;
    private double oldBalance;
    private double newBalance;
    private boolean cancelled = false;
    
    public PlayerBalanceChangeEvent(Player player, double oldBalance, double newBalance) {
        this.player = player;
        this.oldBalance = oldBalance;
        this.newBalance = newBalance;
    }
    
    public Player getPlayer() { return player; }
    public double getOldBalance() { return oldBalance; }
    public double getNewBalance() { return newBalance; }
    public void setNewBalance(double balance) { this.newBalance = balance; }
    
    @Override
    public String getEventName() { return "PlayerBalanceChange"; }
    
    @Override
    public boolean isCancelled() { return cancelled; }
    
    @Override
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
    
    @Override
    public boolean isCancellable() { return true; }
}
```

### EventBus

Event dispatcher with priority support.

```java
public interface EventBus {
    <T extends IonEvent> ListenerHandle subscribe(Class<T> eventClass, Consumer<T> listener);
    <T extends IonEvent> ListenerHandle subscribe(Class<T> eventClass, EventPriority priority, Consumer<T> listener);
    <T extends IonEvent> T fire(T event);
    void unsubscribeAll();
}
```

**Example:**

```java
// Subscribe to event
EventBus eventBus = getEventBus();

eventBus.subscribe(PlayerBalanceChangeEvent.class, event -> {
    Player player = event.getPlayer();
    double newBalance = event.getNewBalance();
    
    if (newBalance < 0) {
        event.setCancelled(true);
        player.sendMessage("§cCannot have negative balance!");
    }
});

// Fire event
PlayerBalanceChangeEvent event = new PlayerBalanceChangeEvent(player, 100.0, 150.0);
eventBus.fire(event);

if (!event.isCancelled()) {
    // Apply the change
}
```

### EventPriority

Priority levels for event handlers.

```java
public enum EventPriority {
    LOWEST,   // Executed first
    LOW,
    NORMAL,   // Default
    HIGH,
    HIGHEST,  // Executed last
    MONITOR   // Read-only, observe final state
}
```

**Example with Priority:**

```java
// High priority - runs late
eventBus.subscribe(PlayerBalanceChangeEvent.class, EventPriority.HIGH, event -> {
    getLogger().info("Balance changed for " + event.getPlayer().getName());
});

// Monitor - observe final state
eventBus.subscribe(PlayerBalanceChangeEvent.class, EventPriority.MONITOR, event -> {
    if (!event.isCancelled()) {
        // Log the change
        logToDatabase(event);
    }
});
```

---

## Utility API

### TextUtil

Adventure API text formatting utilities.

```java
public final class TextUtil {
    static Component parse(String message);
    static Component legacyColor(String message);
    static Component colored(String message, NamedTextColor color);
    static Component bold(String message);
    static Component italic(String message);
    static Component underlined(String message);
    static Component strikethrough(String message);
    static String stripColor(String message);
}
```

**Examples:**

```java
import static com.ionapi.api.util.TextUtil.*;

// MiniMessage format
Component msg = parse("<green>Hello <bold>World</bold>!");

// Legacy color codes
Component legacy = legacyColor("&aGreen &c&lBold Red");

// Simple coloring
Component colored = colored("Error!", NamedTextColor.RED);

// Decorations
Component bold = bold("Important!");
Component italic = italic("Note:");
Component underlined = underlined("Click here");

// Strip colors
String plain = stripColor("§aColored text"); // Returns "Colored text"
```

---

## Platform Detection

Check which platform your plugin is running on:

```java
String platform = getPlatform();

if (platform.equals("folia")) {
    // Folia-specific logic
    getLogger().info("Running on Folia!");
} else {
    // Paper or Spigot
    getLogger().info("Running on Paper/Spigot!");
}
```

---

## Best Practices

### Thread Safety

**✅ DO:**
- Use `runAsync()` for database operations, API calls, file I/O
- Use `run()` or `runLater()` for world/entity modifications
- Check `isMainThread()` when unsure

**❌ DON'T:**
- Access Bukkit API from async tasks
- Perform blocking operations on main thread

### Resource Cleanup

```java
@Override
public void onDisable() {
    // Cancel all scheduled tasks
    getScheduler().cancelAll();
    
    // Unregister all commands
    getCommandRegistry().unregisterAll();
    
    // Unsubscribe all event listeners
    getEventBus().unsubscribeAll();
    
    // Save configs
    getConfigProvider().saveAll();
}
```

### Error Handling

```java
scheduler.runAsync(() -> {
    try {
        // Potentially failing operation
        performDatabaseQuery();
    } catch (Exception e) {
        getLogger().severe("Database error: " + e.getMessage());
        e.printStackTrace();
    }
});
```


---

## 📚 More Resources

- [Getting Started](GETTING_STARTED.md) - Complete tutorial
- [Examples](EXAMPLES.md) - Code examples
- [Quick Reference](QUICK_REFERENCE.md) - Cheatsheet
- [Migration Guide](MIGRATION_GUIDE.md) - Migrate from Bukkit
- [Folia Guide](FOLIA_GUIDE.md) - Folia compatibility

---

## 💬 Community & Support

[![Discord](https://img.shields.io/badge/Discord-Join%20Server-5865F2?style=flat-square&logo=discord&logoColor=white)](https://discord.com/invite/VQjTVKjs46)
[![GitHub](https://img.shields.io/badge/GitHub-mattbaconz-181717?style=flat-square&logo=github)](https://github.com/mattbaconz)

**Need help?** Join our [Discord](https://discord.com/invite/VQjTVKjs46)!

**Support the project:**
- ☕ [Ko-fi](https://ko-fi.com/mbczishim/tip)
- 💰 [PayPal](https://www.paypal.com/paypalme/MatthewWatuna)


---

## v1.2.0 Utilities

### CooldownManager

Thread-safe cooldown management for player actions.

```java
CooldownManager cooldowns = CooldownManager.create("teleport");

// Check cooldown
if (cooldowns.isOnCooldown(player.getUniqueId())) {
    long remaining = cooldowns.getRemainingTime(player.getUniqueId(), TimeUnit.SECONDS);
    player.sendMessage("Wait " + remaining + "s!");
    return;
}

// Set cooldown
cooldowns.setCooldown(player.getUniqueId(), 30, TimeUnit.SECONDS);

// Remove cooldown
cooldowns.removeCooldown(player.getUniqueId());

// Cleanup expired
int removed = cooldowns.cleanup();
```

**Methods:**
- `create(String name)` - Creates or gets a named cooldown manager
- `setCooldown(UUID, long, TimeUnit)` - Sets a cooldown
- `isOnCooldown(UUID)` - Checks if on cooldown
- `getRemainingTime(UUID, TimeUnit)` - Gets remaining time
- `removeCooldown(UUID)` - Removes cooldown
- `clearAll()` - Clears all cooldowns
- `cleanup()` - Removes expired cooldowns

---

### RateLimiter

Sliding window rate limiting for spam prevention.

```java
// Allow 5 requests per 10 seconds
RateLimiter limiter = RateLimiter.create("chat", 5, 10, TimeUnit.SECONDS);

// Try to acquire permit
if (!limiter.tryAcquire(player.getUniqueId())) {
    int remaining = limiter.getRemainingPermits(player.getUniqueId());
    player.sendMessage("Rate limited! " + remaining + " permits left");
    return;
}

// Get reset time
long resetTime = limiter.getResetTime(player.getUniqueId(), TimeUnit.SECONDS);
```

**Methods:**
- `create(String, int, long, TimeUnit)` - Creates rate limiter
- `tryAcquire(UUID)` - Attempts to acquire permit
- `getRemainingPermits(UUID)` - Gets remaining permits
- `getResetTime(UUID, TimeUnit)` - Gets time until reset
- `reset(UUID)` - Resets for player
- `clearAll()` - Clears all limits

---

### MessageBuilder

Fluent MiniMessage builder with templates.

```java
// Simple message
MessageBuilder.of("<green>Hello, <player>!")
    .placeholder("player", player.getName())
    .send(player);

// Title
MessageBuilder.of("<gold><bold>LEVEL UP!")
    .subtitle("<gray>You are now level <level>")
    .placeholder("level", "10")
    .timing(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
    .sendTitle(player);

// Action bar
MessageBuilder.of("<red>❤ <health>/<max>")
    .placeholder("health", "15")
    .placeholder("max", "20")
    .sendActionBar(player);

// Templates
MessageBuilder.registerTemplate("welcome", "<gradient:gold:yellow>Welcome to <server>!");
MessageBuilder.template("welcome")
    .placeholder("server", "My Server")
    .broadcast();
```

**Methods:**
- `of(String)` - Creates builder with message
- `template(String)` - Creates from template
- `placeholder(String, String)` - Adds placeholder
- `subtitle(String)` - Sets subtitle
- `timing(Duration, Duration, Duration)` - Sets title timing
- `send(Player)` - Sends message
- `sendTitle(Player)` - Sends as title
- `sendActionBar(Player)` - Sends as action bar
- `broadcast()` - Broadcasts to all players

---

### IonScoreboard

Easy scoreboard creation with MiniMessage.

```java
IonScoreboard board = IonScoreboard.builder()
    .title("<gradient:gold:yellow><bold>My Server")
    .line(15, "<gray>Welcome, <white>{player}")
    .line(14, "")
    .line(13, "<gold>Coins: <yellow>{coins}")
    .line(12, "<green>Online: <white>{online}")
    .placeholder("player", p -> p.getName())
    .placeholder("coins", p -> String.valueOf(getCoins(p)))
    .placeholder("online", p -> String.valueOf(Bukkit.getOnlinePlayers().size()))
    .build();

board.show(player);
board.update(player); // Refresh
board.hide(player);
```

**Methods:**
- `builder()` - Creates new builder
- `title(String)` - Sets title
- `line(int, String)` - Adds line at score
- `lines(int, String...)` - Adds multiple lines
- `placeholder(String, Function<Player, String>)` - Adds placeholder
- `show(Player)` - Shows to player
- `update(Player)` - Updates for player
- `hide(Player)` - Hides from player
- `destroy()` - Destroys scoreboard

---

### IonBossBar

Boss bar management with MiniMessage.

```java
IonBossBar bar = IonBossBar.builder()
    .name("event-progress")
    .title("<gradient:red:orange>Event: {progress}%")
    .color(BossBar.Color.RED)
    .style(BossBar.Overlay.PROGRESS)
    .progress(0.5f)
    .placeholder("progress", p -> "50")
    .build();

bar.show(player);
bar.setProgress(0.75f);
bar.setTitle("<gradient:green:yellow>Almost done!");
bar.hide(player);
```

**Methods:**
- `builder()` - Creates new builder
- `name(String)` - Sets unique name
- `title(String)` - Sets title
- `color(BossBar.Color)` - Sets color
- `style(BossBar.Overlay)` - Sets style
- `progress(float)` - Sets progress (0.0-1.0)
- `placeholder(String, Function)` - Adds placeholder
- `show(Player)` - Shows to player
- `setProgress(float)` - Updates progress
- `setTitle(String)` - Updates title
- `hide(Player)` - Hides from player

---

### Metrics

Lightweight performance monitoring.

```java
// Counters
Metrics.increment("player.join");
Metrics.increment("blocks.broken", 5);
long joins = Metrics.getCount("player.join");

// Gauges
Metrics.gauge("players.online", Bukkit.getOnlinePlayers().size());
long online = Metrics.getGauge("players.online");

// Timing
Metrics.time("database.query", () -> {
    // operation
});

String result = Metrics.time("api.call", () -> {
    return callApi();
});

// Statistics
double avgTime = Metrics.getAverageTime("database.query");
Metrics.TimingStats stats = Metrics.getTimingStats("database.query");
System.out.println("Min: " + stats.getMinMs());
System.out.println("Max: " + stats.getMaxMs());
System.out.println("Avg: " + stats.getAverageMs());
```

**Methods:**
- `increment(String)` - Increments counter
- `increment(String, long)` - Increments by amount
- `getCount(String)` - Gets counter value
- `gauge(String, long)` - Sets gauge value
- `getGauge(String)` - Gets gauge value
- `time(String, Runnable)` - Times operation
- `time(String, Supplier<T>)` - Times and returns result
- `getAverageTime(String)` - Gets average time in ms
- `getTimingStats(String)` - Gets detailed stats
- `reset()` - Resets all metrics

---

### BatchOperation

Efficient bulk database operations (10-50x faster).

```java
List<PlayerStats> stats = new ArrayList<>();
// ... populate list

// Batch insert
BatchOperation.BatchResult result = database.batch(PlayerStats.class)
    .insertAll(stats)
    .batchSize(500)
    .execute();

System.out.println("Inserted: " + result.insertedCount());
System.out.println("Time: " + result.executionTimeMs() + "ms");

// Batch update
database.batch(PlayerStats.class)
    .updateAll(stats)
    .executeAsync()
    .thenAccept(r -> {
        System.out.println("Updated: " + r.updatedCount());
    });

// Mixed operations
database.batch(PlayerStats.class)
    .insertAll(newStats)
    .updateAll(existingStats)
    .deleteAll(oldStats)
    .batchSize(1000)
    .execute();
```

**Methods:**
- `insert(T)` - Adds entity for insertion
- `insertAll(List<T>)` - Adds multiple for insertion
- `update(T)` - Adds entity for update
- `updateAll(List<T>)` - Adds multiple for update
- `delete(T)` - Adds entity for deletion
- `deleteAll(List<T>)` - Adds multiple for deletion
- `batchSize(int)` - Sets batch size (default: 1000)
- `execute()` - Executes synchronously
- `executeAsync()` - Executes asynchronously

**BatchResult:**
- `insertedCount()` - Number of inserts
- `updatedCount()` - Number of updates
- `deletedCount()` - Number of deletes
- `totalAffected()` - Total affected rows
- `executionTimeMs()` - Execution time in milliseconds

---

## Support

- **Discord**: https://discord.com/invite/VQjTVKjs46
- **GitHub**: https://github.com/mattbaconz/IonAPI
- **Ko-fi**: https://ko-fi.com/mbczishim/tip
- **PayPal**: https://www.paypal.com/paypalme/MatthewWatuna
