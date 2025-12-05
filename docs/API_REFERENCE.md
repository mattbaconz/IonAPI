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
