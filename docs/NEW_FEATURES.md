# IonAPI - New Features Documentation

This document provides comprehensive examples and usage guides for the new features added to IonAPI.

## Table of Contents

1. [Item Builder](#item-builder)
2. [GUI System](#gui-system)
3. [Scoreboard API](#scoreboard-api)
4. [BossBar API](#bossbar-api)
5. [Task Chains](#task-chains)
6. [Database Layer](#database-layer)

---

## Item Builder

The Item Builder provides a fluent API for creating and modifying ItemStacks with clean, readable code.

### Basic Usage

```java
import com.ionapi.item.IonItem;

// Simple item creation
ItemStack diamond = IonItem.builder(Material.DIAMOND)
    .name("<gradient:aqua:blue>Rare Diamond")
    .lore(
        "A precious gem",
        "",
        "<gray>Rarity: <gold>Legendary"
    )
    .glow()
    .build();
```

### Advanced Features

```java
// Complex item with enchantments
ItemStack sword = IonItem.builder(Material.DIAMOND_SWORD)
    .name("<gradient:red:dark_red><bold>Flame Reaver")
    .lore(
        "<gray>Forged in dragon fire",
        "",
        "<red>+ 15 Damage",
        "<gold>+ 10% Critical Strike",
        "",
        "<dark_gray>Legendary Weapon"
    )
    .enchant(Enchantment.SHARPNESS, 5)
    .enchant(Enchantment.FIRE_ASPECT, 2)
    .enchant(Enchantment.UNBREAKING, 3)
    .unbreakable()
    .hideAll()  // Hide all attributes
    .customModelData(1001)
    .build();

// Modify existing items
ItemStack modified = IonItem.modify(existingItem, builder -> {
    builder.name("<green>Enhanced " + originalName)
           .addLore("<gold>+10 Power")
           .glow();
});

// Quick creation methods
ItemStack simpleItem = IonItem.of(Material.STICK, "<yellow>Magic Wand");
ItemStack withLore = IonItem.of(Material.BOOK, 
    "<gold>Spell Book", 
    "<gray>Contains ancient magic",
    "<dark_purple>Right click to cast"
);
```

### Builder Methods Reference

```java
// Display
.name(String)              // Set name (MiniMessage format)
.lore(String...)           // Set lore lines
.addLore(String...)        // Add lore lines

// Properties
.amount(int)               // Stack size (1-64)
.material(Material)        // Change material type
.damage(int)               // Set durability damage
.customModelData(int)      // Custom model for resource packs

// Enchantments
.enchant(Enchantment, level)      // Add enchantment
.enchantUnsafe(Enchantment, level) // Bypass level limits
.removeEnchant(Enchantment)       // Remove enchantment
.clearEnchants()                  // Remove all enchantments

// Flags
.flag(ItemFlag)            // Add item flag
.flags(ItemFlag...)        // Add multiple flags
.hideAll()                 // Hide all attributes
.glow()                    // Add enchantment glow
.unbreakable()             // Make unbreakable

// Utility
.clone()                   // Clone the builder
.customData(Consumer)      // Apply custom NBT modifications
```

---

## GUI System

Create interactive inventory GUIs with click handlers and automatic management.

### Basic GUI Creation

```java
import com.ionapi.gui.IonGui;
import com.ionapi.item.IonItem;

// Create a simple shop GUI
IonGui shopGui = IonGui.builder()
    .title("<gold><bold>Item Shop")
    .rows(3)
    .item(10, IonItem.of(Material.DIAMOND, "<aqua>Diamond", "<gray>Price: $100"), 
        click -> {
            Player player = click.getPlayer();
            if (hasEnoughMoney(player, 100)) {
                takeMoney(player, 100);
                player.getInventory().addItem(new ItemStack(Material.DIAMOND));
                player.sendMessage("<green>Purchased diamond!");
                click.close();
            } else {
                player.sendMessage("<red>Not enough money!");
            }
        })
    .item(12, IonItem.of(Material.GOLD_INGOT, "<yellow>Gold Ingot", "<gray>Price: $50"),
        click -> buyItem(click.getPlayer(), Material.GOLD_INGOT, 50))
    .item(14, IonItem.of(Material.IRON_INGOT, "<white>Iron Ingot", "<gray>Price: $25"),
        click -> buyItem(click.getPlayer(), Material.IRON_INGOT, 25))
    .fillBorderBuilder(IonItem.of(Material.GRAY_STAINED_GLASS_PANE, " "))
    .build();

// Open for player
shopGui.open(player);
```

### Advanced GUI Features

```java
// Paginated GUI
IonGui paginatedGui = IonGui.builder("<gold>Player List - Page 1", 6)
    .item(45, IonItem.of(Material.ARROW, "<yellow>Previous Page"), 
        click -> openPage(click.getPlayer(), currentPage - 1))
    .item(53, IonItem.of(Material.ARROW, "<yellow>Next Page"),
        click -> openPage(click.getPlayer(), currentPage + 1))
    .onClick(click -> {
        // Global click handler
        getLogger().info(player.getName() + " clicked slot " + click.getSlot());
    })
    .onCloseHandler(close -> {
        savePlayerPreferences(close.getPlayer());
    })
    .build();

// Dynamic GUI that updates
IonGui statsGui = IonGui.builder("<green>Server Stats", 3)
    .item(13, getStatsItem())
    .autoUpdate(true, 20L)  // Update every second
    .build();

// Update GUI manually
gui.setItem(13, newItem);
gui.update();  // Refresh for all viewers

// Row and column positioning
gui.setItem(1, 4, item);  // Row 1, Column 4 (center of second row)

// Fill patterns (use after build)
gui.fill(fillerItem);                    // Fill entire GUI
gui.fillBorder(borderItem);              // Fill border only
gui.fillRect(0, 8, topRowItem);         // Fill specific area

// Fill patterns (use in builder)
IonGui gui2 = IonGui.builder()
    .fillBuilder(fillerItem)             // Fill entire GUI
    .fillBorderBuilder(borderItem)       // Fill border only
    .fillRectBuilder(0, 8, topRowItem)   // Fill specific area
    .build();
```

### GUI Event Handling

```java
IonGui gui = IonGui.builder("Custom GUI", 3)
    .onOpenHandler(event -> {
        Player player = event.getPlayer();
        player.sendMessage("Welcome to the GUI!");
        
        // Can cancel opening
        if (!player.hasPermission("gui.use")) {
            event.setCancelled(true);
            player.sendMessage("No permission!");
        }
    })
    .onCloseHandler(event -> {
        Player player = event.getPlayer();
        CloseReason reason = event.getReason();
        
        if (reason == CloseReason.PLAYER) {
            player.sendMessage("Thanks for visiting!");
        }
    })
    .onClickHandler(event -> {
        // Access click details
        int slot = event.getSlot();
        ClickType clickType = event.getClickType();
        ItemStack clicked = event.getClickedItem();
        
        if (event.isRightClick()) {
            // Handle right click
        }
        
        if (event.isShiftClick()) {
            // Handle shift click
        }
        
        // Control event cancellation
        event.setCancelled(true);  // Prevent taking items
    })
    .allowTake(false)   // Prevent taking items
    .allowPlace(false)  // Prevent placing items
    .build();
```

---

## Scoreboard API

Create and manage player scoreboards with dynamic content.

### Basic Scoreboard

```java
import com.ionapi.ui.IonScoreboard;

// Create a simple scoreboard
IonScoreboard board = IonScoreboard.create(player)
    .title("<gold><bold>My Server")
    .line("<gray>━━━━━━━━━━━━━━")
    .line("<yellow>Players: <white>" + Bukkit.getOnlinePlayers().size())
    .line("<yellow>Rank: <green>VIP")
    .line("")
    .line("<aqua>example.com")
    .line("<gray>━━━━━━━━━━━━━━")
    .show();

// Update specific line
board.updateLine(1, "<yellow>Players: <white>" + newCount);

// Update all lines
board.lines(
    "<gray>━━━━━━━━━━━━━━",
    "<yellow>Coins: <gold>" + coins,
    "<yellow>Level: <green>" + level,
    "",
    "<aqua>example.com"
);
```

### Dynamic Scoreboards

```java
// Create scoreboard with dynamic content
IonScoreboard board = IonScoreboard.create(player)
    .title("<rainbow>Dynamic Board")
    .line("<gray>━━━━━━━━━━━━━━")
    .line("") // Placeholder for dynamic line 1
    .line("") // Placeholder for dynamic line 2
    .line("") // Placeholder for dynamic line 3
    .line("<gray>━━━━━━━━━━━━━━")
    .dynamicLine(1, p -> "<yellow>Health: <red>" + p.getHealth())
    .dynamicLine(2, p -> "<yellow>Food: <green>" + p.getFoodLevel())
    .dynamicLine(3, p -> "<yellow>XP: <aqua>" + p.getLevel())
    .autoUpdate(20L)  // Update every second
    .show();

// Dynamic title
board.dynamicTitle(p -> {
    int hour = LocalTime.now().getHour();
    if (hour < 12) return "<yellow>Good Morning!";
    else if (hour < 18) return "<gold>Good Afternoon!";
    else return "<blue>Good Evening!";
});

// Manual control
board.update();           // Update all dynamic content
board.stopAutoUpdate();   // Stop automatic updates
board.hide();            // Hide from player
board.show();            // Show again
board.destroy();         // Clean up resources
```

### Scoreboard Management

```java
// Manage multiple scoreboards per player
Map<UUID, IonScoreboard> playerBoards = new HashMap<>();

public void showStatsBoard(Player player) {
    // Remove old board if exists
    IonScoreboard oldBoard = playerBoards.get(player.getUniqueId());
    if (oldBoard != null) {
        oldBoard.destroy();
    }
    
    // Create new board
    IonScoreboard board = IonScoreboard.create(player)
        .title("<green>Your Stats")
        .lines(
            "<gray>━━━━━━━━━━━━━━",
            "<yellow>Kills: <white>" + getKills(player),
            "<yellow>Deaths: <white>" + getDeaths(player),
            "<yellow>K/D: <white>" + getKD(player),
            "<gray>━━━━━━━━━━━━━━"
        )
        .show();
    
    playerBoards.put(player.getUniqueId(), board);
}

// Clean up on disable
@Override
public void onDisable() {
    playerBoards.values().forEach(IonScoreboard::destroy);
    playerBoards.clear();
}
```

---

## BossBar API

Create and manage boss bars for visual feedback and progress indicators.

### Basic BossBar

```java
import com.ionapi.ui.IonBossBar;
import net.kyori.adventure.bossbar.BossBar;

// Simple progress bar
IonBossBar bar = IonBossBar.create()
    .title("<red>Boss Health")
    .progress(0.75f)  // 75%
    .color(BossBar.Color.RED)
    .overlay(BossBar.Overlay.PROGRESS)
    .show(player);

// Update progress
bar.progress(0.5f);
bar.progressPercent(50);  // Same as above

// Hide after delay
scheduler.runLater(() -> bar.hide(player), 5, TimeUnit.SECONDS);
```

### Advanced BossBar Features

```java
// Multiple players
IonBossBar bar = IonBossBar.create("<yellow>Event Starting")
    .progress(1.0f)
    .color(BossBar.Color.YELLOW)
    .show(Bukkit.getOnlinePlayers());

// Countdown timer
IonBossBar countdown = IonBossBar.create()
    .title("<red><bold>Game Starting in 10...")
    .progress(1.0f)
    .color(BossBar.Color.RED)
    .overlay(BossBar.Overlay.NOTCHED_10)
    .show(players);

// Update with timer
scheduler.runTimer(() -> {
    timeLeft--;
    countdown.title("<red><bold>Game Starting in " + timeLeft + "...");
    countdown.progressPercent((int) (timeLeft / 10.0 * 100));
    
    if (timeLeft <= 0) {
        countdown.hideAll();
        startGame();
    }
}, 0, 1, TimeUnit.SECONDS);

// Dynamic boss bar
IonBossBar bossHealth = IonBossBar.create()
    .dynamicTitle(bar -> {
        return "<red>Boss: " + getBoss().getName() + 
               " <white>" + getBoss().getHealth() + "❤";
    })
    .dynamicProgress(bar -> {
        return (float) (getBoss().getHealth() / getBoss().getMaxHealth());
    })
    .color(BossBar.Color.RED)
    .autoUpdate(5L)  // Update every 5 ticks
    .show(playersInArena);

// Flags
bar.addFlag(BossBar.Flag.CREATE_FOG);
bar.addFlag(BossBar.Flag.DARKEN_SCREEN);
bar.addFlag(BossBar.Flag.PLAY_BOSS_MUSIC);

// Colors: PINK, BLUE, RED, GREEN, YELLOW, PURPLE, WHITE
// Overlays: PROGRESS, NOTCHED_6, NOTCHED_10, NOTCHED_12, NOTCHED_20
```

---

## Task Chains

Build complex async/sync workflows with clean, readable code.

### Basic Task Chain

```java
import com.ionapi.tasks.TaskChain;

// Simple async -> sync chain
TaskChain.create(plugin)
    .async(() -> {
        // Heavy operation off main thread
        return fetchPlayerDataFromDatabase(uuid);
    })
    .syncAt(player, data -> {
        // Apply data on player's region thread (Folia-safe)
        player.setHealth(data.health);
        player.setLevel(data.level);
        player.sendMessage("<green>Data loaded!");
    })
    .execute();
```

### Complex Workflows

```java
// Multi-step workflow with error handling
TaskChain.create(plugin)
    .async(() -> {
        // Step 1: Fetch from database
        return database.find(PlayerStats.class, playerUuid);
    })
    .async(stats -> {
        // Step 2: Calculate rankings (still async)
        return calculateGlobalRanking(stats);
    })
    .syncAt(player, ranking -> {
        // Step 3: Update player on their thread
        player.sendMessage("<gold>Your rank: <yellow>#" + ranking);
        updateScoreboard(player, ranking);
    })
    .delay(2, TimeUnit.SECONDS)
    .sync(() -> {
        // Step 4: Broadcast after delay
        Bukkit.broadcast(Component.text(player.getName() + " checked their rank!"));
    })
    .exceptionally(ex -> {
        // Handle errors
        plugin.getLogger().severe("Failed to load player stats: " + ex.getMessage());
        player.sendMessage("<red>Failed to load stats!");
    })
    .finallyDo(() -> {
        // Always runs, regardless of success/failure
        cleanupResources();
    })
    .execute();
```

### Advanced Task Chain Features

```java
// Conditional execution
TaskChain.create(plugin)
    .async(() -> fetchUserData(uuid))
    .syncIf(() -> player.hasPermission("premium"), data -> {
        // Only runs if condition is true
        applyPremiumBenefits(player, data);
    })
    .execute();

// Value transformation
TaskChain.<PlayerData>create(plugin, initialData)
    .async(data -> {
        // Transform data
        data.coins += 100;
        return data;
    })
    .async(data -> {
        // Save to database
        database.save(data);
        return data;
    })
    .syncAt(player, data -> {
        // Notify player
        player.sendMessage("<gold>+100 coins! Total: " + data.coins);
    })
    .execute();

// Recovery from errors
TaskChain.create(plugin)
    .async(() -> riskyOperation())
    .recover(ex -> {
        // Provide fallback value on error
        plugin.getLogger().warning("Using fallback: " + ex.getMessage());
        return fallbackValue;
    })
    .sync(value -> {
        // Continue with either real or fallback value
        processValue(value);
    })
    .execute();

// Wait for result
try {
    PlayerData data = TaskChain.create(plugin)
        .async(() -> database.find(PlayerData.class, uuid))
        .executeAndWait();  // Blocks until complete
    
    // Use data
} catch (Exception e) {
    handleError(e);
}

// Multiple entity operations (Folia-aware)
TaskChain.create(plugin)
    .syncAt(player, () -> {
        player.sendMessage("Healing nearby entities...");
    })
    .async(() -> {
        // Find nearby entities
        return player.getNearbyEntities(10, 10, 10);
    })
    .sync(entities -> {
        // Heal each entity on their own thread
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity living) {
                scheduler.runAt(living, () -> {
                    living.setHealth(living.getMaxHealth());
                });
            }
        }
    })
    .execute();
```

---

## Database Layer

Simple ORM for database operations with async support.

### Setup and Connection

```java
import com.ionapi.database.*;
import com.ionapi.database.annotations.*;

// MySQL connection
IonDatabase db = IonDatabase.builder()
    .type(DatabaseType.MYSQL)
    .host("localhost")
    .port(3306)
    .database("myserver")
    .username("root")
    .password("password")
    .poolSize(10)  // Connection pool size
    .build();

// SQLite connection (file-based)
IonDatabase db = IonDatabase.sqlite("plugins/MyPlugin/data.db");

// Connect
try {
    db.connect();
    plugin.getLogger().info("Database connected!");
} catch (DatabaseException e) {
    plugin.getLogger().severe("Failed to connect: " + e.getMessage());
}
```

### Define Entities

```java
import com.ionapi.database.annotations.*;
import java.util.UUID;

@Table("players")
public class PlayerData {
    
    @PrimaryKey
    private UUID uuid;
    
    @Column(nullable = false)
    private String name;
    
    @Column(defaultValue = "1")
    private int level;
    
    @Column(defaultValue = "0.0")
    private double balance;
    
    @Column(name = "last_login")
    private long lastLogin;
    
    @Column(length = 500, nullable = true)
    private String metadata;
    
    // Constructors
    public PlayerData() {}
    
    public PlayerData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.level = 1;
        this.balance = 0.0;
        this.lastLogin = System.currentTimeMillis();
    }
    
    // Getters and setters
    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    
    public long getLastLogin() { return lastLogin; }
    public void setLastLogin(long lastLogin) { this.lastLogin = lastLogin; }
}
```

### CRUD Operations

```java
// Create table
db.createTable(PlayerData.class);

// Insert new entity
PlayerData player = new PlayerData(playerUuid, playerName);
player.setLevel(5);
player.setBalance(100.0);
db.insert(player);

// Find by primary key
PlayerData data = db.find(PlayerData.class, playerUuid);
if (data != null) {
    plugin.getLogger().info("Found player: " + data.getName());
}

// Update
data.setBalance(data.getBalance() + 50);
db.update(data);

// Save (insert or update)
db.save(data);

// Delete
db.delete(data);

// Delete by ID
boolean deleted = db.deleteById(PlayerData.class, playerUuid);

// Find all
List<PlayerData> allPlayers = db.findAll(PlayerData.class);
```

### Async Database Operations

```java
// Async find
db.findAsync(PlayerData.class, playerUuid)
    .thenAccept(optionalData -> {
        optionalData.ifPresent(data -> {
            plugin.getLogger().info("Loaded: " + data.getName());
        });
    })
    .exceptionally(ex -> {
        plugin.getLogger().severe("Error: " + ex.getMessage());
        return null;
    });

// Async save
db.saveAsync(playerData)
    .thenRun(() -> {
        plugin.getLogger().info("Saved successfully!");
    });

// Combined with TaskChain
TaskChain.create(plugin)
    .async(() -> db.find(PlayerData.class, uuid))
    .syncAt(player, data -> {
        if (data != null) {
            applyPlayerData(player, data);
        } else {
            player.sendMessage("<red>No data found!");
        }
    })
    .execute();
```

### Raw SQL Queries

```java
// Execute update
int rowsAffected = db.execute(
    "UPDATE players SET balance = balance + ? WHERE level > ?",
    100.0, 10
);

// Execute query
ResultSet rs = db.query(
    "SELECT * FROM players WHERE name LIKE ?",
    "%" + searchTerm + "%"
);

while (rs.next()) {
    String name = rs.getString("name");
    int level = rs.getInt("level");
    // Process results
}

// Async raw queries
db.executeAsync("UPDATE players SET last_login = ?", System.currentTimeMillis())
    .thenAccept(rows -> {
        plugin.getLogger().info("Updated " + rows + " rows");
    });
```

### Transactions

```java
// Simple transaction
db.transaction(database -> {
    PlayerData sender = database.find(PlayerData.class, senderUuid);
    PlayerData receiver = database.find(PlayerData.class, receiverUuid);
    
    sender.setBalance(sender.getBalance() - amount);
    receiver.setBalance(receiver.getBalance() + amount);
    
    database.save(sender);
    database.save(receiver);
});

// Transaction with return value
boolean success = db.transactionWithResult(database -> {
    PlayerData data = database.find(PlayerData.class, uuid);
    
    if (data.getBalance() >= cost) {
        data.setBalance(data.getBalance() - cost);
        database.save(data);
        return true;
    }
    
    return false;
});

// Async transaction
db.transactionAsync(database -> {
    // Multiple operations in transaction
    List<PlayerData> players = database.findAll(PlayerData.class);
    for (PlayerData player : players) {
        player.setLevel(player.getLevel() + 1);
        database.save(player);
    }
}).thenRun(() -> {
    plugin.getLogger().info("Bulk update complete!");
});

// Manual transaction control
Transaction tx = db.beginTransaction();
try {
    // Perform operations
    db.save(entity1);
    db.save(entity2);
    
    tx.commit();
} catch (Exception e) {
    tx.rollback();
    throw e;
}
```

### Complete Plugin Example

```java
public class MyPlugin implements IonPlugin {
    
    private IonDatabase database;
    private final Map<UUID, IonScoreboard> scoreboards = new HashMap<>();
    
    @Override
    public void onEnable() {
        // Initialize database
        database = IonDatabase.builder()
            .type(DatabaseType.SQLITE)
            .database("plugins/MyPlugin/data.db")
            .build();
        
        try {
            database.connect();
            database.createTable(PlayerData.class);
        } catch (DatabaseException e) {
            getLogger().severe("Database error: " + e.getMessage());
        }
        
        // Register events
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
    }
    
    private class PlayerJoinListener implements Listener {
        
        @EventHandler
        public void onJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            
            // Load player data with task chain
            TaskChain.create(MyPlugin.this)
                .async(() -> {
                    // Load from database
                    PlayerData data = database.find(PlayerData.class, player.getUniqueId());
                    
                    if (data == null) {
                        // Create new player data
                        data = new PlayerData(player.getUniqueId(), player.getName());
                        database.insert(data);
                    } else {
                        // Update last login
                        data.setLastLogin(System.currentTimeMillis());
                        database.update(data);
                    }
                    
                    return data;
                })
                .syncAt(player, data -> {
                    // Apply data to player
                    player.setLevel(data.getLevel());
                    
                    // Show scoreboard
                    IonScoreboard board = IonScoreboard.create(player)
                        .title("<gold><bold>My Server")
                        .line("<gray>━━━━━━━━━━━━━━")
                        .line("")
                        .line("")
                        .line("<gray>━━━━━━━━━━━━━━")
                        .dynamicLine(1, p -> "<yellow>Level: <white>" + data.getLevel())
                        .dynamicLine(2, p -> "<yellow>Balance: <green>$" + data.getBalance())
                        .autoUpdate(20L)
                        .show();
                    
                    scoreboards.put(player.getUniqueId(), board);
                    
                    // Show welcome GUI
                    IonGui welcomeGui = IonGui.builder("<gold>Welcome!", 3)
                        .item(13, IonItem.builder(Material.PLAYER_HEAD)
                            .name("<green>Your Profile")
                            .lore(
                                "<gray>Name: <white>" + player.getName(),
                                "<gray>Level: <yellow>" + data.getLevel(),
                                "<gray>Balance: <green>$" + data.getBalance()
                            )
                            .build())
                        .fillBorder(IonItem.of(Material.BLUE_STAINED_GLASS_PANE, " "))
                        .build();
                    
                    welcomeGui.open(player);
                })
                .exceptionally(ex -> {
                    player.sendMessage("<red>Failed to load your data!");
                    getLogger().severe("Error loading player data: " + ex.getMessage());
                })
                .execute();
        }
    }
    
    @Override
    public void onDisable() {
        // Clean up scoreboards
        scoreboards.values().forEach(IonScoreboard::destroy);
        scoreboards.clear();
        
        // Disconnect database
        if (database != null) {
            database.disconnect();
        }
    }
}
```

---

## Best Practices

### Performance

1. **Use async operations** for database queries and heavy computations
2. **Batch database operations** within transactions
3. **Cache frequently accessed data** instead of querying repeatedly
4. **Use auto-update sparingly** - update only when necessary
5. **Clean up resources** - destroy GUIs, scoreboards, and boss bars when done

### Thread Safety (Folia)

1. **Use `syncAt(entity)`** when modifying entities
2. **Use `syncAt(location)`** when modifying worlds
3. **Avoid global state** - prefer entity/location-specific operations
4. **Use TaskChains** for complex async/sync workflows

### Error Handling

```java
// Always handle database errors
try {
    db.save(data);
} catch (DatabaseException e) {
    plugin.getLogger().severe("Save failed: " + e.getMessage());
    player.sendMessage("<red>Failed to save data!");
}

// Use error handlers in task chains
TaskChain.create(plugin)
    .async(() -> riskyOperation())
    .exceptionally(ex -> handleError(ex))
    .finallyDo(() -> cleanup())
    .execute();
```

### Resource Cleanup

```java
@Override
public void onDisable() {
    // Close all GUIs
    openGuis.forEach(IonGui::destroy);
    
    // Remove all scoreboards
    scoreboards.values().forEach(IonScoreboard::destroy);
    
    // Hide all boss bars
    bossBars.values().forEach(IonBossBar::hideAll);
    
    // Disconnect database
    database.disconnect();
    
    // Cancel task chains
    activeChains.forEach(TaskChain::cancel);
}
```

---

## Module Dependencies

To use these features, add the appropriate modules to your `build.gradle.kts`:

```kotlin
dependencies {
    // Core API (required)
    compileOnly("com.ionapi:ion-api:1.0.0")
    
    // Optional feature modules
    implementation("com.ionapi:ion-item:1.0.0")
    implementation("com.ionapi:ion-gui:1.0.0")
    implementation("com.ionapi:ion-ui:1.0.0")
    implementation("com.ionapi:ion-tasks:1.0.0")
    implementation("com.ionapi:ion-database:1.0.0")
}
```

Or use all features:

```kotlin
dependencies {
    implementation("com.ionapi:ion-all:1.0.0")
}
```

---

## Additional Resources

- [API Reference](API_REFERENCE.md) - Complete API documentation
- [Examples](EXAMPLES.md) - More real-world examples
- [Folia Guide](FOLIA_GUIDE.md) - Folia compatibility guide
- [Contributing](../CONTRIBUTING.md) - How to contribute

## Support

- 📖 [Documentation](https://github.com/yourrepo/IonAPI/wiki)
- 💬 [GitHub Issues](https://github.com/yourrepo/IonAPI/issues)
- 🌟 Star the repo if you find it useful!