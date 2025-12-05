# IonAPI - AI IDE Integration Guide

> **Purpose**: This guide helps AI-powered IDEs (like Cursor, GitHub Copilot, Windsurf, etc.) understand and effectively use IonAPI for Minecraft plugin development.

## What is IonAPI?

IonAPI is a modern, multi-platform Minecraft plugin API that provides a unified interface for Paper and Folia servers. It dramatically reduces boilerplate code while maintaining type safety and performance.

**Key Benefits:**
- 50-80% less code compared to raw Bukkit API
- Unified scheduler that works on both Paper and Folia
- Modern fluent APIs with builder patterns
- Built-in async/sync task chaining
- Type-safe configuration and event systems

---

## Quick Start

### 1. Add Dependency

```kotlin
// build.gradle.kts
dependencies {
    compileOnly("com.ionapi:ion-api:1.0.0-SNAPSHOT")
    
    // Optional feature modules
    implementation("com.ionapi:ion-item:1.0.0-SNAPSHOT")
    implementation("com.ionapi:ion-gui:1.0.0-SNAPSHOT")
    implementation("com.ionapi:ion-ui:1.0.0-SNAPSHOT")
    implementation("com.ionapi:ion-tasks:1.0.0-SNAPSHOT")
}
```

### 2. Create Plugin

```java
import com.ionapi.api.IonPlugin;

public class MyPlugin implements IonPlugin {
    @Override
    public void onEnable() {
        getLogger().info("Plugin enabled!");
    }
    
    @Override
    public void onDisable() {
        getScheduler().cancelAll();
    }
    
    @Override
    public String getName() {
        return "MyPlugin";
    }
}
```

---

## Core API Patterns

### Scheduler (Thread-Safe, Folia-Compatible)

```java
// Run on main thread
getScheduler().run(() -> {
    // Your code
});

// Run async (for I/O operations)
getScheduler().runAsync(() -> {
    String data = database.query();
    getScheduler().run(() -> applyData(data));
});

// Delayed execution
getScheduler().runLater(() -> {
    player.sendMessage("5 seconds passed!");
}, 5, TimeUnit.SECONDS);

// Repeating task
IonTask task = getScheduler().runTimer(() -> {
    // Runs every second
}, 0, 1, TimeUnit.SECONDS);
task.cancel(); // Cancel when done

// FOLIA-AWARE: Entity-specific scheduling
getScheduler().runAt(player, () -> {
    player.damage(5.0); // Safe on Folia
});

// FOLIA-AWARE: Location-specific scheduling
getScheduler().runAt(location, () -> {
    world.spawnParticle(Particle.FLAME, location, 10);
});
```

### Commands

```java
public class HelloCommand implements IonCommand {
    @Override
    public boolean execute(CommandContext ctx) {
        String name = ctx.getArg(0, "World");
        ctx.reply("<green>Hello, " + name + "!");
        return true;
    }
    
    @Override
    public String getName() { return "hello"; }
    @Override
    public String getDescription() { return "Greets a player"; }
    @Override
    public String getUsage() { return "/hello [name]"; }
    @Override
    public String getPermission() { return "myplugin.hello"; }
}

// Register
getCommandRegistry().register(new HelloCommand());
```

### Configuration

```java
// config.yml
IonConfig config = getConfigProvider().getConfig();

// Read values
String host = config.getString("database.host", "localhost");
int port = config.getInt("database.port", 3306);
boolean enabled = config.getBoolean("enabled", true);
List<String> items = config.getStringList("items");

// Write values
config.set("last-updated", System.currentTimeMillis());
config.save();
```

### Events (Internal Plugin Events)

```java
// Define custom event
public class PlayerLevelUpEvent implements IonEvent {
    private final Player player;
    private int newLevel;
    private boolean cancelled = false;
    
    public PlayerLevelUpEvent(Player player, int newLevel) {
        this.player = player;
        this.newLevel = newLevel;
    }
    
    public Player getPlayer() { return player; }
    public int getNewLevel() { return newLevel; }
    public void setNewLevel(int level) { this.newLevel = level; }
    
    @Override
    public String getEventName() { return "PlayerLevelUp"; }
    @Override
    public boolean isCancelled() { return cancelled; }
    @Override
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
    @Override
    public boolean isCancellable() { return true; }
}

// Subscribe to event
getEventBus().subscribe(PlayerLevelUpEvent.class, event -> {
    Player player = event.getPlayer();
    player.sendMessage("<gold>Level up! New level: " + event.getNewLevel());
});

// Fire event
PlayerLevelUpEvent event = new PlayerLevelUpEvent(player, 5);
getEventBus().fire(event);
if (!event.isCancelled()) {
    applyLevelUp(player, event.getNewLevel());
}
```

---

## Extended Features

### Item Builder

```java
import com.ionapi.item.IonItem;

// Create custom item
ItemStack sword = IonItem.builder(Material.DIAMOND_SWORD)
    .name("<gradient:red:blue>Legendary Sword")
    .lore(
        "<gray>Forged in dragon fire",
        "",
        "<gold>Legendary Weapon"
    )
    .enchant(Enchantment.SHARPNESS, 5)
    .enchant(Enchantment.FIRE_ASPECT, 2)
    .unbreakable()
    .glow()
    .hideAll()
    .build();

// Quick creation
ItemStack simple = IonItem.of(Material.STICK, "<yellow>Magic Wand");
ItemStack withLore = IonItem.of(Material.BOOK, "Title", "Lore line 1", "Lore line 2");

// Modify existing
ItemStack modified = IonItem.modify(existingItem, builder -> 
    builder.name("New Name").addLore("<gold>Enhanced!"));
```

### GUI System

```java
import com.ionapi.gui.IonGui;

// Create interactive GUI
IonGui.builder()
    .title("<gold>Shop")
    .rows(3)
    .item(10, IonItem.of(Material.DIAMOND, "<aqua>Diamond - $100"), 
        click -> {
            Player player = click.getPlayer();
            if (buyItem(player, 100)) {
                player.sendMessage("<green>Purchased!");
                click.close();
            } else {
                player.sendMessage("<red>Not enough money!");
            }
        })
    .item(12, IonItem.of(Material.GOLD_INGOT, "<yellow>Gold - $50"),
        click -> buyItem(click.getPlayer(), 50))
    .fillBorderBuilder(IonItem.of(Material.GRAY_STAINED_GLASS_PANE, " "))
    .build()
    .open(player);
```

### Scoreboard

```java
import com.ionapi.ui.IonScoreboard;

// Create dynamic scoreboard
IonScoreboard board = IonScoreboard.create(player)
    .title("<gold>Server Stats")
    .line("<gray>━━━━━━━━━━━━━━")
    .line("") // Placeholder
    .line("") // Placeholder
    .line("<gray>━━━━━━━━━━━━━━")
    .dynamicLine(1, p -> "<yellow>Players: <white>" + Bukkit.getOnlinePlayers().size())
    .dynamicLine(2, p -> "<green>Health: <white>" + (int) p.getHealth())
    .autoUpdate(20L) // Update every second
    .show();

// Update manually
board.updateLine(1, "<yellow>New text");
board.update();

// Cleanup
board.destroy();
```

### BossBar

```java
import com.ionapi.ui.IonBossBar;
import net.kyori.adventure.bossbar.BossBar;

// Create progress bar
IonBossBar bar = IonBossBar.create()
    .title("<red>Boss Health")
    .progress(0.75f) // 75%
    .color(BossBar.Color.RED)
    .overlay(BossBar.Overlay.PROGRESS)
    .show(player);

// Update
bar.progress(0.5f);
bar.title("<red>Boss: 50% HP");

// Dynamic updates
IonBossBar dynamic = IonBossBar.create()
    .dynamicTitle(b -> "Health: " + boss.getHealth())
    .dynamicProgress(b -> (float) (boss.getHealth() / boss.getMaxHealth()))
    .autoUpdate(5L)
    .show(player);

// Cleanup
bar.hideAll();
bar.destroy();
```

### Task Chains (Async/Sync Workflows)

```java
import com.ionapi.tasks.TaskChain;

// Complex workflow
TaskChain.create(plugin)
    .async(() -> {
        // Heavy I/O operation
        return database.loadPlayerData(uuid);
    })
    .syncAt(player, data -> {
        // Apply on player's thread (Folia-safe)
        player.setHealth(data.health);
        player.setLevel(data.level);
    })
    .delay(2, TimeUnit.SECONDS)
    .syncAt(player, () -> {
        player.sendMessage("<green>Welcome back!");
    })
    .exceptionally(ex -> {
        player.sendMessage("<red>Failed to load data!");
        getLogger().severe("Error: " + ex.getMessage());
    })
    .finallyDo(() -> {
        cleanup();
    })
    .execute();
```

---

## Common Patterns

### Pattern: Load Player Data on Join

```java
@EventHandler
public void onJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    
    TaskChain.create(plugin)
        .async(() -> database.find(PlayerData.class, player.getUniqueId()))
        .syncAt(player, data -> {
            if (data != null) {
                applyData(player, data);
            } else {
                createNewPlayer(player);
            }
        })
        .exceptionally(ex -> {
            player.sendMessage("<red>Failed to load data!");
        })
        .execute();
}
```

### Pattern: Shop GUI with Purchase Logic

```java
public void openShop(Player player) {
    IonGui.builder()
        .title("<gold>Item Shop")
        .rows(3)
        .item(11, createShopItem(Material.DIAMOND, "Diamond", 100),
            click -> purchaseItem(click.getPlayer(), Material.DIAMOND, 100))
        .item(13, createShopItem(Material.GOLD_INGOT, "Gold", 50),
            click -> purchaseItem(click.getPlayer(), Material.GOLD_INGOT, 50))
        .item(15, createShopItem(Material.IRON_INGOT, "Iron", 25),
            click -> purchaseItem(click.getPlayer(), Material.IRON_INGOT, 25))
        .fillBorderBuilder(IonItem.of(Material.GRAY_STAINED_GLASS_PANE, " "))
        .build()
        .open(player);
}

private ItemStack createShopItem(Material material, String name, int price) {
    return IonItem.builder(material)
        .name("<yellow>" + name)
        .lore("<gray>Price: <gold>$" + price)
        .build();
}

private void purchaseItem(Player player, Material item, int cost) {
    TaskChain.create(plugin)
        .async(() -> economy.getBalance(player.getUniqueId()))
        .syncAt(player, balance -> {
            if (balance >= cost) {
                economy.withdraw(player.getUniqueId(), cost);
                player.getInventory().addItem(new ItemStack(item));
                player.sendMessage("<green>Purchase successful!");
            } else {
                player.sendMessage("<red>Not enough money!");
            }
        })
        .execute();
}
```

### Pattern: Dynamic Stats Scoreboard

```java
private final Map<UUID, IonScoreboard> scoreboards = new HashMap<>();

public void showStatsBoard(Player player) {
    IonScoreboard board = IonScoreboard.create(player)
        .title("<gold><bold>Your Stats")
        .line("<gray>━━━━━━━━━━━━━━")
        .line("")
        .line("")
        .line("")
        .line("<gray>━━━━━━━━━━━━━━")
        .dynamicLine(1, p -> "<yellow>Level: <white>" + getLevel(p))
        .dynamicLine(2, p -> "<green>Coins: <white>" + getCoins(p))
        .dynamicLine(3, p -> "<aqua>Rank: <white>" + getRank(p))
        .autoUpdate(20L)
        .show();
    
    scoreboards.put(player.getUniqueId(), board);
}

@Override
public void onDisable() {
    scoreboards.values().forEach(IonScoreboard::destroy);
    scoreboards.clear();
}
```

---

## Important Rules

### Thread Safety (Folia Compatibility)

```java
// ❌ WRONG - May crash on Folia
getScheduler().run(() -> {
    player.damage(5.0); // Unsafe if player is in different region
});

// ✅ CORRECT - Folia-safe
getScheduler().runAt(player, () -> {
    player.damage(5.0); // Always runs on player's region thread
});

// ❌ WRONG - Blocking main thread
getScheduler().run(() -> {
    String data = database.query(); // Blocks server!
});

// ✅ CORRECT - Async I/O
getScheduler().runAsync(() -> {
    String data = database.query();
    getScheduler().run(() -> applyData(data));
});
```

### Resource Cleanup

```java
@Override
public void onDisable() {
    // Cancel all tasks
    getScheduler().cancelAll();
    
    // Destroy GUIs
    openGuis.forEach(IonGui::destroy);
    
    // Destroy scoreboards
    scoreboards.values().forEach(IonScoreboard::destroy);
    
    // Hide boss bars
    bossBars.values().forEach(IonBossBar::hideAll);
    
    // Save configs
    getConfigProvider().saveAll();
}
```

### Event Bus vs Bukkit Events

```java
// Use BUKKIT events for server-wide events
@EventHandler
public void onPlayerJoin(PlayerJoinEvent event) {
    // Handle Bukkit event
}

// Use ION EventBus for internal plugin events
getEventBus().subscribe(CustomInternalEvent.class, event -> {
    // Handle internal plugin logic
});
```

---

## Code Generation Tips for AI

### When generating IonAPI code:

1. **Always use fluent builders** for items, GUIs, scoreboards, boss bars
2. **Use TaskChain** for any async/sync workflows
3. **Use `runAt(entity/location)`** when modifying game objects (Folia-safe)
4. **Use MiniMessage format** for text: `<green>Text` not `§aText`
5. **Always clean up resources** in `onDisable()`
6. **Handle errors** in task chains with `.exceptionally()`
7. **Use IonEventBus** for internal plugin events only
8. **Cache scoreboards/boss bars** per player, don't recreate

### Common Mistakes to Avoid:

- ❌ Don't use `new ItemStack()` and `ItemMeta` - use `IonItem.builder()`
- ❌ Don't manually track inventories - use `IonGui.builder()`
- ❌ Don't use Bukkit Scoreboard API - use `IonScoreboard.create()`
- ❌ Don't nest async callbacks - use `TaskChain`
- ❌ Don't forget to call `.build()` on builders
- ❌ Don't forget to call `.execute()` on task chains
- ❌ Don't perform I/O on main thread - use `.runAsync()`

---

## Module Dependencies

```kotlin
dependencies {
    // Core (required)
    compileOnly("com.ionapi:ion-api:1.0.0-SNAPSHOT")
    
    // Item Builder
    implementation("com.ionapi:ion-item:1.0.0-SNAPSHOT")
    
    // GUI System (includes SignInput & InputGui)
    implementation("com.ionapi:ion-gui:1.0.0-SNAPSHOT")
    
    // Scoreboard & BossBar
    implementation("com.ionapi:ion-ui:1.0.0-SNAPSHOT")
    
    // Task Chains
    implementation("com.ionapi:ion-tasks:1.0.0-SNAPSHOT")
    
    // Database ORM (optional)
    implementation("com.ionapi:ion-database:1.0.0-SNAPSHOT")
}
```

---

## Performance Tips

1. **Cache frequently used objects** (scoreboards, boss bars)
2. **Use async for I/O** (database, file operations, HTTP requests)
3. **Batch operations** when possible
4. **Use `runAt()` methods** to leverage Folia's parallelism
5. **Clean up resources** to prevent memory leaks
6. **Use auto-update sparingly** - update only when needed

---

## Example: Complete Plugin

```java
public class ExamplePlugin implements IonPlugin {
    
    private final Map<UUID, IonScoreboard> scoreboards = new HashMap<>();
    
    @Override
    public void onEnable() {
        // Register commands
        getCommandRegistry().register(new ShopCommand(this));
        
        // Register Bukkit events
        Bukkit.getPluginManager().registerEvents(new JoinListener(this), this);
        
        getLogger().info("Plugin enabled!");
    }
    
    @Override
    public void onDisable() {
        scoreboards.values().forEach(IonScoreboard::destroy);
        scoreboards.clear();
        getScheduler().cancelAll();
    }
    
    @Override
    public String getName() {
        return "ExamplePlugin";
    }
    
    private class JoinListener implements Listener {
        private final ExamplePlugin plugin;
        
        public JoinListener(ExamplePlugin plugin) {
            this.plugin = plugin;
        }
        
        @EventHandler
        public void onJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            
            // Show scoreboard
            IonScoreboard board = IonScoreboard.create(player)
                .title("<gold>Welcome!")
                .line("")
                .dynamicLine(0, p -> "<yellow>Players: " + Bukkit.getOnlinePlayers().size())
                .autoUpdate(20L)
                .show();
            
            scoreboards.put(player.getUniqueId(), board);
        }
    }
    
    private class ShopCommand implements IonCommand {
        private final ExamplePlugin plugin;
        
        public ShopCommand(ExamplePlugin plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean execute(CommandContext ctx) {
            Player player = (Player) ctx.getSender();
            
            IonGui.builder()
                .title("<gold>Shop")
                .rows(3)
                .item(13, IonItem.of(Material.DIAMOND, "<aqua>Diamond - $100"),
                    click -> {
                        click.getPlayer().sendMessage("<green>Purchased!");
                        click.close();
                    })
                .fillBorderBuilder(IonItem.of(Material.GRAY_STAINED_GLASS_PANE, " "))
                .build()
                .open(player);
            
            return true;
        }
        
        @Override
        public String getName() { return "shop"; }
        @Override
        public String getDescription() { return "Open shop"; }
        @Override
        public String getUsage() { return "/shop"; }
        @Override
        public String getPermission() { return "example.shop"; }
    }
}
```

---

## Additional Resources

- **Full Documentation**: See `docs/` folder
- **API Reference**: `docs/API_REFERENCE.md`
- **Examples**: `examples/` folder
- **Migration Guide**: `docs/MIGRATION_GUIDE.md`
- **Quick Reference**: `docs/QUICK_REFERENCE.md`

---

## Version Information

- **Current Version**: 1.0.0-SNAPSHOT
- **Java Version**: 21+
- **Supported Platforms**: Paper, Folia
- **Minecraft Versions**: 1.20+

---

**Last Updated**: 2024-12-05
